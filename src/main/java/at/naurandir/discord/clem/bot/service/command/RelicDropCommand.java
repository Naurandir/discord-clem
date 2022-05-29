package at.naurandir.discord.clem.bot.service.command;

import at.naurandir.discord.clem.bot.model.WarframeState;
import at.naurandir.discord.clem.bot.service.client.dto.droptable.MissionDTO;
import at.naurandir.discord.clem.bot.service.client.dto.droptable.RelicDTO;
import at.naurandir.discord.clem.bot.service.client.dto.droptable.RewardDTO;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    
    private static final String MISSION_DATA = "***{title}*** - {item} {rotation} ({dropChance}%)\n";

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
        
        if (item.length() < 3) {
            return event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage("The given input [" + item + "] is too short. Please provide a longer name to search"))
                .then();
        }
        
        log.info("handle: searching in [{}] relics with input [{}]", 
                warframeState.getRelics().size(), item);
        
        List<RelicDTO> relicsWithItem = getRelicsWithItem(item, warframeState);
        List<MissionDTO> missionsWithRelics = getMissionsWithRelics(relicsWithItem, warframeState);
        log.debug("handle: found [{}] relics and [{}] missions", relicsWithItem.size(), missionsWithRelics.size());
        
        Map<String, String> relicMessages = getRelicMessages(relicsWithItem, item);
        List<String> missionMessages = getMissionMessages(missionsWithRelics, relicsWithItem);
        log.debug("handle: got [{}] mission messages", missionMessages.size());
        
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

    private List<RelicDTO> getRelicsWithItem(String item, WarframeState warframeState) {
        return warframeState.getRelics().stream()
                .filter(relic -> rewardContainsItem(relic.getDrops(), item))
                .collect(Collectors.toList());
    }

    private boolean rewardContainsItem(List<RewardDTO> rewards, String item) {
        return rewards.stream().anyMatch(reward -> reward.getReward().toLowerCase().contains(item.toLowerCase()));
    }
    
    private List<MissionDTO> getMissionsWithRelics(List<RelicDTO> relicsWithItem, WarframeState warframeState) {
        List<MissionDTO> missionsWithRelics = new ArrayList<>();
        List<String> relicItemNames = relicsWithItem.stream()
                .map(relic -> relic.getTier() + " " + relic.getName() + " Relic")
                .collect(Collectors.toList());
        
        for (MissionDTO mission : warframeState.getMissions()) {
            boolean isAnyRelicInMission = mission.getRewardsRotationA().stream()
                        .anyMatch(rotReward -> relicItemNames.contains(rotReward.getReward())) ||
                    mission.getRewardsRotationB().stream()
                        .anyMatch(rotReward -> relicItemNames.contains(rotReward.getReward())) ||
                    mission.getRewardsRotationC().stream()
                        .anyMatch(rotReward -> relicItemNames.contains(rotReward.getReward())) ||
                    mission.getRewardsRotationGeneral().stream()
                        .anyMatch(rotReward -> relicItemNames.contains(rotReward.getReward()));
            
            if (isAnyRelicInMission) {
                missionsWithRelics.add(mission);
            }
        }
        
        return missionsWithRelics;
    }
    
    private EmbedCreateSpec generateEmbedRelics(String item, Map<String, String> relicMessages) {
        return generateEmbed(RELIC_SEARCH_TITLE.replace("{item}", item), 
                RELIC_SEARCH_DESCRIPTION, 
                Color.ORANGE, 
                "https://static.wikia.nocookie.net/warframe/images/0/0e/VoidProjectionsGoldD.png/revision/latest?cb=20160709035734", 
                relicMessages);
    }
    
    private EmbedCreateSpec generateEmbedMissions(String item, List<String> missionMessages) {
        StringBuilder descriptionBuilder = new StringBuilder("");
        missionMessages.stream().limit(20).forEach(entry -> descriptionBuilder.append(entry));
        
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

    private Map<String, String> getRelicMessages(List<RelicDTO> relicsWithItem, String item) {
        Map<String, String> relicMessages = new HashMap<>();
        
        for (RelicDTO relic : relicsWithItem) {
            RewardDTO drop = relic.getDrops().stream()
                    .filter(reward -> reward.getReward().toLowerCase().contains(item.toLowerCase()))
                    .findFirst().get();
            relicMessages.putIfAbsent(relic.getTier() + " " + relic.getName(), drop.getReward() + " " + drop.getRarity());
        }
        return relicMessages;
    }

    private List<String> getMissionMessages(List<MissionDTO> missionsWithRelics, List<RelicDTO> relicsWithItem) {
        List<String> missionMessages = new ArrayList<>();
        List<String> relicItemNames = relicsWithItem.stream()
                .map(relic -> relic.getTier() + " " + relic.getName() + " Relic")
                .collect(Collectors.toList());
        
        for (MissionDTO mission : missionsWithRelics) {
            List<RewardDTO> rewardsWithItemGeneral = mission.getRewardsRotationGeneral().stream()
                    .filter(reward -> relicItemNames.contains(reward.getReward()))
                    .collect(Collectors.toList());
            
            List<RewardDTO> rewardsWithItemRotationA = mission.getRewardsRotationA().stream()
                    .filter(reward -> relicItemNames.contains(reward.getReward()))
                    .collect(Collectors.toList());
            
            List<RewardDTO> rewardsWithItemRotationB = mission.getRewardsRotationB().stream()
                    .filter(reward -> relicItemNames.contains(reward.getReward()))
                    .collect(Collectors.toList());
            
            List<RewardDTO> rewardsWithItemRotationC = mission.getRewardsRotationC().stream()
                    .filter(reward -> relicItemNames.contains(reward.getReward()))
                    .collect(Collectors.toList());
            
            rewardsWithItemGeneral.forEach(reward -> missionMessages.add(MISSION_DATA
                    .replace("{title}", mission.getName())
                    .replace("{item}", reward.getReward())
                    .replace(" {rotation}","")
                    .replace("{dropChance}", String.valueOf(reward.getChance().get(0)))));
            
            rewardsWithItemRotationA.forEach(reward -> missionMessages.add(MISSION_DATA
                    .replace("{title}", mission.getName())
                    .replace("{item}", reward.getReward())
                    .replace("{rotation}","Rotation A")
                    .replace("{dropChance}", String.valueOf(reward.getChance().get(0)))));
            
            rewardsWithItemRotationB.forEach(reward -> missionMessages.add(MISSION_DATA
                    .replace("{title}", mission.getName())
                    .replace("{item}", reward.getReward())
                    .replace("{rotation}","Rotation B")
                    .replace("{dropChance}", String.valueOf(reward.getChance().get(0)))));
            
            rewardsWithItemRotationC.forEach(reward -> missionMessages.add(MISSION_DATA
                    .replace("{title}", mission.getName())
                    .replace("{item}", reward.getReward())
                    .replace("{rotation}","Rotation C")
                    .replace("{dropChance}", String.valueOf(reward.getChance().get(0)))));
        }
        
        return missionMessages;
    }
}
