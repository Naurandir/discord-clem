package at.naurandir.discord.clem.bot.service.command;

import at.naurandir.discord.clem.bot.model.WarframeState;
import at.naurandir.discord.clem.bot.service.client.dto.droptable.MissionDropDTO;
import at.naurandir.discord.clem.bot.service.client.dto.droptable.RelicDropDTO;
import at.naurandir.discord.clem.bot.service.client.dto.droptable.RewardDropDTO;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class RelicDropCommand implements Command {
    
    private static final String RELIC_SEARCH_TITLE = "Relics with '{item}' Drop";
    private static final String RELIC_SEARCH_DESCRIPTION = "Relics that drop desired item, note some can be vaulted.";
    private static final String MISSION_SEARCH_TITLE = "Missions with Relic Drop for '{item}'";
    
    private static final String MISSION_DATA = "***{title}*** - {info}\n";
    private static final String MISSION_SPECIFIC_INFO = "{item} ({dropChance}%) {rotation}";

    @Override
    public String getCommandWord() {
        return "relic-drop";
    }
    
    @Override
    public String getDescription() {
        return "Shows all Relics that drop a specific item.\n"
                + "It also shows related missions where to farm that relics.\n"
                + "Usage: *<bot-prefix> relic-drop <item>*.";
    }

    @Override
    public Mono<Void> handle(MessageCreateEvent event, WarframeState warframeState) {
        String item = getItem(event.getMessage().getContent());
        
        log.info("handle: searching in [{}] relics with input [{}]", 
                warframeState.getRelics().size(), item);
        
        Set<RelicDropDTO> relicsWithItem = getRelicsWithItem(item, warframeState);
        log.debug("handle: found [{}] relics", relicsWithItem.size());
        
        Map<String, String> relicMessages = getRelicMessages(relicsWithItem, item);
        Map<String, String> missionMessages = getMissionMessages(relicsWithItem, warframeState);
        
        return event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage(generateEmbedRelics(item, relicMessages), 
                                                          generateEmbedMissions(item, missionMessages)))
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
            relicMessages.putIfAbsent(relic.getTier() + " " + relic.getRelicName(), drop.getItemName());
        }
        return relicMessages;
    }

    private Map<String, String> getMissionMessages(Set<RelicDropDTO> relicsWithItem, WarframeState warframeState) {
        Map<String, String> missionMessages = new HashMap<>();
        List<String> relicNames = relicsWithItem.stream()
                .map(relic -> relic.getTier() + " " + relic.getRelicName() + " Relic")
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
        
        log.debug("getMissionMessages: got [{}] messages", missionMessages.size());
        return missionMessages;
    }
    
    private void addMissionData(Map<String, String> nodeMissionMessage, String planet, String node, RewardDropDTO rewardDrop, String rotation) {
        String title = node + " (" + planet + ")";
        String message = MISSION_SPECIFIC_INFO.replace("{item}", rewardDrop.getItemName())
                .replace("{dropChance}", String.valueOf(rewardDrop.getChance()))
                .replace("{rotation}",(rotation != null ? "Rot " + rotation : ""));
        nodeMissionMessage.put(title, message);
    }
    
    private EmbedCreateSpec generateEmbedRelics(String item, Map<String, String> relicMessages) {
        return generateEmbed(RELIC_SEARCH_TITLE.replace("{item}", item), 
                RELIC_SEARCH_DESCRIPTION, 
                Color.ORANGE, 
                "https://static.wikia.nocookie.net/warframe/images/0/0e/VoidProjectionsGoldD.png/revision/latest?cb=20160709035734", 
                relicMessages);
    }
    
    private EmbedCreateSpec generateEmbedMissions(String item, Map<String, String> missionMessages) {
        StringBuilder descriptionBuilder = new StringBuilder("");
        missionMessages.entrySet().stream().limit(20).forEach(entry -> 
                descriptionBuilder.append(MISSION_DATA.replace("{title}",entry.getKey())
                        .replace("{info}", entry.getValue())));
        
        return generateEmbed(MISSION_SEARCH_TITLE.replace("{item}", item),
                descriptionBuilder.toString(),
                Color.TAHITI_GOLD,
                "https://preview.redd.it/99m1fk0q9x8z.jpg?width=640&crop=smart&auto=webp&s=625d5cc7a395ecff11d9b58df20eb949ae92ac7d",
                Map.of());
    }
    
    private EmbedCreateSpec generateEmbed(String title, String description, Color color, String thumbnail, Map<String, String> fieldData) {         
        EmbedCreateSpec.Builder embedBuilder = EmbedCreateSpec.builder()
                .color(color)
                .title(title)
                .description(description)
                .thumbnail(thumbnail)
                .timestamp(Instant.now());
        
        fieldData.entrySet().forEach(entry -> embedBuilder.addField(entry.getKey(), entry.getValue(), true));
        
        return embedBuilder.build();
    }
}
