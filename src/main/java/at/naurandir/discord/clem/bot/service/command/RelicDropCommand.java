package at.naurandir.discord.clem.bot.service.command;

import at.naurandir.discord.clem.bot.model.mission.Mission;
import at.naurandir.discord.clem.bot.model.mission.MissionReward;
import at.naurandir.discord.clem.bot.model.relic.Relic;
import at.naurandir.discord.clem.bot.model.relic.RelicDrop;
import at.naurandir.discord.clem.bot.service.MissionService;
import at.naurandir.discord.clem.bot.service.RelicService;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 *
 * @author Naurandir
 */
@Slf4j
@Component
public class RelicDropCommand implements Command {
    
    @Autowired
    private RelicService relicService;
    
    @Autowired
    private MissionService missionService;
    
    private static final String RELIC_SEARCH_TITLE = "Relics with '{item}' Drop";
    private static final String RELIC_SEARCH_DESCRIPTION = "Relics that drop desired item, note some can be vaulted.\n\n";
    private static final String MISSION_SEARCH_TITLE = "Missions with Relic Drop for '{item}'";
    
    private static final String MISSION_DESCRIPTION = "Note that only maximum 20 different mission drops are shown here. For better results precise the input.\n\n";
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
    public Mono<Void> handle(MessageCreateEvent event) {
        String item = getItem(event.getMessage().getContent());
        
        if (item.length() < 3) {
            return event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage("The given input [" + item + "] is too short. Please provide a longer name to search"))
                .then();
        }
        
        List<Relic> relics = relicService.getRelics();
        log.info("handle: searching in [{}] relics with input [{}]", 
                relics.size(), item);
        
        List<Relic> relicsWithItem = getRelicsWithItem(item, relics);
        List<Mission> missionsWithRelics = getMissionsWithRelics(relicsWithItem);
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
        String[] splitted = content.split(" "); // 'prefix relic-drop <item>'
        String[] words = Arrays.copyOfRange(splitted, 2, splitted.length);
        return StringUtils.join(words, " ").trim();
    }

    private List<Relic> getRelicsWithItem(String item, List<Relic> relics) {
        return relics.stream()
                .filter(relic -> rewardContainsItem(relic.getDrops(), item))
                .collect(Collectors.toList());
    }

    private boolean rewardContainsItem(Set<RelicDrop> drops, String item) {
        return drops.stream().anyMatch(drop -> drop.getReward().toLowerCase().contains(item.toLowerCase()));
    }
    
    private List<Mission> getMissionsWithRelics(List<Relic> relicsWithItem) {
        List<Mission> missionsWithRelics = new ArrayList<>();
        List<Mission> allMissions = missionService.getMissions();
        List<String> relicItemNames = relicsWithItem.stream()
                .map(relic -> relic.getTier().getTierString() + " " + relic.getName() + " Relic")
                .collect(Collectors.toList());
        
        for (Mission mission : allMissions) {           
            boolean isAnyRelicInMission = mission.getAllRewards().stream()
                    .anyMatch(reward -> relicItemNames.contains(reward.getName()));
            
            if (isAnyRelicInMission) {
                missionsWithRelics.add(mission);
            }
        }
        
        return missionsWithRelics;
    }
    
    private Map<String, String> getRelicMessages(List<Relic> relicsWithItem, String item) {
        Map<String, String> relicMessages = new HashMap<>();
        
        for (Relic relic : relicsWithItem) {
            RelicDrop drop = relic.getDrops().stream()
                    .filter(reward -> reward.getReward().toLowerCase().contains(item.toLowerCase()))
                    .findFirst().get();
            relicMessages.putIfAbsent(relic.getTier().getTierString() + " " + relic.getName(), drop.getReward() + " " + drop.getRarity());
        }
        return relicMessages;
    }

    private List<String> getMissionMessages(List<Mission> missionsWithRelics, List<Relic> relicsWithItem) {
        List<String> missionMessages = new ArrayList<>();
        List<String> relicItemNames = relicsWithItem.stream()
                .map(relic -> relic.getTier().getTierString() + " " + relic.getName() + " Relic")
                .collect(Collectors.toList());
        
        for (Mission mission : missionsWithRelics) {
            List<MissionReward> rewardsWithItemGeneral = mission.getGeneralRewards().stream()
                    .filter(reward -> relicItemNames.contains(reward.getName()))
                    .collect(Collectors.toList());
            
            List<MissionReward> rewardsWithItemRotationA = mission.getRotationARewards().stream()
                    .filter(reward -> relicItemNames.contains(reward.getName()))
                    .collect(Collectors.toList());
            
            List<MissionReward> rewardsWithItemRotationB = mission.getRotationBRewards().stream()
                    .filter(reward -> relicItemNames.contains(reward.getName()))
                    .collect(Collectors.toList());
            
            List<MissionReward> rewardsWithItemRotationC = mission.getRotationCRewards().stream()
                    .filter(reward -> relicItemNames.contains(reward.getName()))
                    .collect(Collectors.toList());
            
            rewardsWithItemGeneral.forEach(reward -> missionMessages.add(MISSION_DATA
                    .replace("{title}", mission.getName())
                    .replace("{item}", reward.getName())
                    .replace(" {rotation}","")
                    .replace("{dropChance}", String.valueOf(reward.getChance()))));
            
            rewardsWithItemRotationA.forEach(reward -> missionMessages.add(MISSION_DATA
                    .replace("{title}", mission.getName())
                    .replace("{item}", reward.getName())
                    .replace("{rotation}","Rotation A")
                    .replace("{dropChance}", String.valueOf(reward.getChance()))));
            
            rewardsWithItemRotationB.forEach(reward -> missionMessages.add(MISSION_DATA
                    .replace("{title}", mission.getName())
                    .replace("{item}", reward.getName())
                    .replace("{rotation}","Rotation B")
                    .replace("{dropChance}", String.valueOf(reward.getChance()))));
            
            rewardsWithItemRotationC.forEach(reward -> missionMessages.add(MISSION_DATA
                    .replace("{title}", mission.getName())
                    .replace("{item}", reward.getName())
                    .replace("{rotation}","Rotation C")
                    .replace("{dropChance}", String.valueOf(reward.getChance()))));
        }
        
        return missionMessages;
    }
    
    private EmbedCreateSpec generateEmbedRelics(String item, Map<String, String> relicMessages) {
        StringBuilder descriptionBuilder = new StringBuilder(RELIC_SEARCH_DESCRIPTION);
        
        if (relicMessages.isEmpty()) {
            descriptionBuilder.append("Nothing found in the Search. Maybe the Item was misspelled.");
        }
        
        return generateEmbed(RELIC_SEARCH_TITLE.replace("{item}", item), 
                descriptionBuilder.toString(), 
                Color.TAHITI_GOLD, 
                "https://static.wikia.nocookie.net/warframe/images/0/0e/VoidProjectionsGoldD.png/revision/latest?cb=20160709035734", 
                relicMessages);
    }
    
    private EmbedCreateSpec generateEmbedMissions(String item, List<String> missionMessages) {
        StringBuilder descriptionBuilder = new StringBuilder(MISSION_DESCRIPTION);
        
        if (missionMessages.isEmpty()) {
            descriptionBuilder.append("Nothing found in the Search. Maybe the Item was misspelled or the found Relics are vaulted.");
        } else {
            missionMessages.stream().limit(20).forEach(entry -> descriptionBuilder.append(entry));
        }
        
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
