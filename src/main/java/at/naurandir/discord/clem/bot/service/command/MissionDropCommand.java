package at.naurandir.discord.clem.bot.service.command;

import at.naurandir.discord.clem.bot.model.mission.Mission;
import at.naurandir.discord.clem.bot.model.mission.MissionReward;
import at.naurandir.discord.clem.bot.service.MissionService;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
public class MissionDropCommand implements Command {
    
    @Autowired
    private MissionService missionService;
    
    private static final String TITLE = "Found Missions dropping '{item}'";
    private static final String DESCRIPTION = "Missions with possiblity to drop '{item}'. Note that Enemy Drops are not considered.\n\n";
    private static final String MISSION_MESSAGE = "***{title}*** - {item} {rotation} ({dropChance}%)\n";

    @Override
    public String getCommandWord() {
        return "mission-drop";
    }

    @Override
    public String getDescription() {
        return "Shows all Missions that drop a specific item.\n"
                + "Usage: *<bot-prefix> mission-drop <item>*.";
    }

    @Override
    public Mono<Void> handle(MessageCreateEvent event) {
        String item = getItem(event.getMessage().getContent());
        
        if (item.length() < 3) {
            return event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage("The given input [" + item + "] is too short. Please provide a longer name to search"))
                .timeout(Duration.ofSeconds(60)).then();
        }
        
        List<Mission> missionsWithItem = getMissionsWithItem(item);
        log.debug("handle: found [{}] missions for [{}]", missionsWithItem.size(), item);
        
        return event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage(generateEmbed(item, missionsWithItem)))
                .timeout(Duration.ofSeconds(60)).then();
    }
    
    private String getItem(String content) {
        String[] splitted = content.split(" ");
        String[] words = Arrays.copyOfRange(splitted, 2, splitted.length);
        return StringUtils.join(words, " ").trim();
    }

    private List<Mission> getMissionsWithItem(String item) {
        List<Mission> allMissions = missionService.getMissions();
        List<Mission> missionsWithItem = new ArrayList<>();
        
        
        for (Mission mission : allMissions) {
            
            boolean isAnyItemInMission = mission.getAllRewards().stream()
                    .anyMatch(reward -> reward.getName().toLowerCase().contains(item.toLowerCase()));
            
            if (isAnyItemInMission) {
                missionsWithItem.add(mission);
            }
        }
        
        return missionsWithItem;
    }
    
    private void addMissionMessages(Mission mission, String item, List<String> missionMessages) {
        List<MissionReward> rewardsWithItemGeneral = mission.getGeneralRewards().stream()
                .filter(reward -> reward.getName().toLowerCase().contains(item.toLowerCase()))
                .collect(Collectors.toList());
        
        List<MissionReward> rewardsWithItemRotationA = mission.getRotationARewards().stream()
                .filter(reward -> reward.getName().toLowerCase().contains(item.toLowerCase()))
                .collect(Collectors.toList());
        
        List<MissionReward> rewardsWithItemRotationB = mission.getRotationBRewards().stream()
                .filter(reward -> reward.getName().toLowerCase().contains(item.toLowerCase()))
                .collect(Collectors.toList());
        
        List<MissionReward> rewardsWithItemRotationC = mission.getRotationCRewards().stream()
                .filter(reward -> reward.getName().toLowerCase().contains(item.toLowerCase()))
                .collect(Collectors.toList());
        
        rewardsWithItemGeneral.forEach(reward -> missionMessages.add(MISSION_MESSAGE
                .replace("{title}", mission.getName())
                .replace("{item}", reward.getName())
                .replace(" {rotation}","")
                .replace("{dropChance}", String.valueOf(reward.getChance()))));
        
        rewardsWithItemRotationA.forEach(reward -> missionMessages.add(MISSION_MESSAGE
                .replace("{title}", mission.getName())
                .replace("{item}", reward.getName())
                .replace("{rotation}","Rotation A")
                .replace("{dropChance}", String.valueOf(reward.getChance()))));
        
        rewardsWithItemRotationB.forEach(reward -> missionMessages.add(MISSION_MESSAGE
                .replace("{title}", mission.getName())
                .replace("{item}", reward.getName())
                .replace("{rotation}","Rotation B")
                .replace("{dropChance}", String.valueOf(reward.getChance()))));
        
        rewardsWithItemRotationC.forEach(reward -> missionMessages.add(MISSION_MESSAGE
                .replace("{title}", mission.getName())
                .replace("{item}", reward.getName())
                .replace("{rotation}","Rotation C")
                .replace("{dropChance}", String.valueOf(reward.getChance()))));
    }

    private EmbedCreateSpec generateEmbed(String item, List<Mission> missionsWithItem) {
        StringBuilder descriptionBuilder = new StringBuilder(DESCRIPTION.replace("{item}", item));
        List<String> missionMessages = new ArrayList<>();
        
        missionsWithItem.stream().limit(20).forEach(mission -> addMissionMessages(mission, item, missionMessages));        
        
        if (missionMessages.isEmpty()) {
            descriptionBuilder.append("Nothing found in the Search. Maybe the Item was misspelled?");
        } else {
            missionMessages.stream().limit(20).forEach(message -> descriptionBuilder.append(message));
        }
        
        EmbedCreateSpec.Builder embedBuilder = EmbedCreateSpec.builder()
                .color(Color.DARK_GOLDENROD)
                .title(TITLE.replace("{item}", item))
                .description(descriptionBuilder.toString())
                .thumbnail("https://preview.redd.it/99m1fk0q9x8z.jpg?width=640&crop=smart&auto=webp&s=625d5cc7a395ecff11d9b58df20eb949ae92ac7d")
                .timestamp(Instant.now());
        
        return embedBuilder.build();
    }
}
