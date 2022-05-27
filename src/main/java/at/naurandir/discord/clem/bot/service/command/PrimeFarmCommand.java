package at.naurandir.discord.clem.bot.service.command;

import at.naurandir.discord.clem.bot.model.WarframeState;
import at.naurandir.discord.clem.bot.service.client.dto.droptable.MissionDropDTO;
import at.naurandir.discord.clem.bot.service.client.dto.droptable.RelicDropDTO;
import at.naurandir.discord.clem.bot.service.client.dto.droptable.RewardDropDTO;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 *
 * @author Naurandir
 */
@Slf4j
@Component
public class PrimeFarmCommand implements Command {

    @Override
    public String getCommandWord() {
        return "prime-farm";
    }

    @Override
    public Mono<Void> handle(MessageCreateEvent event, WarframeState warframeState) {
        Message message = event.getMessage();
        String item = getItem(event.getMessage().getContent());
        
        log.info("handle: searching for relics in [{}] relics with input [{}]", 
                warframeState.getRelics().size(), item);
        
        Set<RelicDropDTO> relicsWithItem = getRelicsWithItem(item, warframeState);
        log.debug("handle: found [{}] relics", relicsWithItem.size());
        
        Map<String, String> relicMessages = getRelicMessages(relicsWithItem, item);
        Map<String, String> missionMessages = getMissionMessages(relicsWithItem, warframeState);
        
        return event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage(generateEmbedRelics(message, relicMessages), 
                                                          generateEmbedMissions(message, missionMessages)))
                .then();
    }
    
    private String getItem(String content) {
        String[] splitted = content.split(" "); // 'prefix prime-farm <item>'
        String[] words = Arrays.copyOfRange(splitted, 2, splitted.length);
        return StringUtils.join(words, " ").trim();
    }

    private Set<RelicDropDTO> getRelicsWithItem(String item, WarframeState warframeState) {
        return warframeState.getRelics().stream()
                .filter(relic -> rewardContainsItem(relic.getRewards(), item))
                .collect(Collectors.toSet());
    }

    private boolean rewardContainsItem(List<RewardDropDTO> rewards, String item) {
        return rewards.stream().anyMatch(reward -> reward.getItemName().toLowerCase().contains(item.toLowerCase()));
    }
    
    private Map<String, String> getRelicMessages(Set<RelicDropDTO> relicsWithItem, String item) {
        Map<String, String> relicMessages = new HashMap<>();
        
        for (RelicDropDTO relic : relicsWithItem) {
            RewardDropDTO drop = relic.getRewards().stream()
                    .filter(reward -> reward.getItemName().toLowerCase().contains(item.toLowerCase()))
                    .findFirst().get();
            relicMessages.putIfAbsent(relic.getTier() + " " + relic.getRelicName(), drop.getItemName() + "(" + drop.getRarity() + ")");
        }
        return relicMessages;
    }

    private Map<String, String> getMissionMessages(Set<RelicDropDTO> relicsWithItem, WarframeState warframeState) {
        Map<String, String> missionMessages = new HashMap<>();
        List<String> relicNames = relicsWithItem.stream()
                .map(relic -> relic.getTier() + " " + relic.getRelicName())
                .collect(Collectors.toList());
        
        for (String planet : warframeState.getMissions().keySet()) {
            for (String node : warframeState.getMissions().get(planet).keySet()) {
                MissionDropDTO missionDrop = warframeState.getMissions().get(planet).get(node);
                
                missionDrop.getMissionRewards().getA().stream()
                        .filter(reward -> relicNames.contains(reward.getItemName()))
                        .forEach(reward -> addMissionData(missionMessages, planet, node, reward, "A"));

                missionDrop.getMissionRewards().getB().stream()
                        .filter(reward -> relicNames.contains(reward.getItemName()))
                        .forEach(reward -> addMissionData(missionMessages, planet, node, reward, "B")); 
                
                missionDrop.getMissionRewards().getC().stream()
                        .filter(reward -> relicNames.contains(reward.getItemName()))
                        .forEach(reward -> addMissionData(missionMessages, planet, node, reward, "C"));
                
                missionDrop.getMissionRewards().getGeneralRewards().stream()
                        .filter(reward -> relicNames.contains(reward.getItemName()))
                        .forEach(reward -> addMissionData(missionMessages, planet, node, reward, null)); 
            }
        }
        
        return missionMessages;
    }
    
    private void addMissionData(Map<String, String> nodeMissionMessage, String planet, String node, RewardDropDTO rewardDrop, String rotation) {
        String title = node + " (" + planet + ")";
        String message = rewardDrop.getItemName() + " - "
                + (rotation != null ? "Rotation " + rotation + " - " : null)
                + rewardDrop.getChance() + " (" 
                + rewardDrop.getRarity() + ")";
        nodeMissionMessage.put(title, message);
    }
    
    private EmbedCreateSpec generateEmbedRelics(Message message, Map<String, String> relicMessages) {
        return generateEmbed("Relics with given Item Search", 
                "The relics that are dropping items with given input.", 
                Color.ORANGE, 
                "https://static.wikia.nocookie.net/warframe/images/0/0e/VoidProjectionsGoldD.png/revision/latest?cb=20160709035734", 
                relicMessages);
    }
    
    private EmbedCreateSpec generateEmbedMissions(Message message, Map<String, String> missionMessages) {
        return generateEmbed("Missions with Relic Drop", 
                "Missions that drop the found relics in order to farm the relics for desired item. If some Relics are not listed it means they are vaulted.", 
                Color.TAHITI_GOLD, 
                "https://preview.redd.it/99m1fk0q9x8z.jpg?width=640&crop=smart&auto=webp&s=625d5cc7a395ecff11d9b58df20eb949ae92ac7d", 
                missionMessages);
    }
    
    private EmbedCreateSpec generateEmbed(String title, String description, Color color, String thumbnail, Map<String, String> fieldData) {         
        EmbedCreateSpec.Builder embedBuilder = EmbedCreateSpec.builder()
                .color(color)
                .title(title)
                .description(description)
                .thumbnail(thumbnail)
                .timestamp(Instant.now());
        
        for (Entry<String, String> entry : fieldData.entrySet()) {
            embedBuilder.addField(entry.getKey(), entry.getValue(), true);
        }
        
        return embedBuilder.build();
    }
}
