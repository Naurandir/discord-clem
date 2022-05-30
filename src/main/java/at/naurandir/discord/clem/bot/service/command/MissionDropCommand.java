package at.naurandir.discord.clem.bot.service.command;

import at.naurandir.discord.clem.bot.model.WarframeState;
import at.naurandir.discord.clem.bot.service.client.dto.droptable.MissionDTO;
import at.naurandir.discord.clem.bot.service.client.dto.droptable.RewardDTO;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
public class MissionDropCommand implements Command {
    
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
    public Mono<Void> handle(MessageCreateEvent event, WarframeState warframeState) {
        String item = getItem(event.getMessage().getContent());
        
        if (item.length() < 3) {
            return event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage("The given input [" + item + "] is too short. Please provide a longer name to search"))
                .then();
        }
        
        List<MissionDTO> missionsWithItem = getMissionsWithItem(item, warframeState);
        log.debug("handle: found [{}] missions for [{}]", missionsWithItem.size(), item);
        
        return event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage(generateEmbed(item, missionsWithItem)))
                .then();
    }
    
    private String getItem(String content) {
        String[] splitted = content.split(" ");
        String[] words = Arrays.copyOfRange(splitted, 2, splitted.length);
        return StringUtils.join(words, " ").trim();
    }

    private List<MissionDTO> getMissionsWithItem(String item, WarframeState warframeState) {
        List<MissionDTO> missionsWithItem = new ArrayList<>();
        
        for (MissionDTO mission : warframeState.getMissions()) {
            boolean isAnyItemInMission = mission.getRewardsRotationA().stream()
                        .anyMatch(rotReward -> rotReward.getReward().toLowerCase().contains(item.toLowerCase())) ||
                    mission.getRewardsRotationB().stream()
                        .anyMatch(rotReward -> rotReward.getReward().toLowerCase().contains(item.toLowerCase())) ||
                    mission.getRewardsRotationC().stream()
                        .anyMatch(rotReward -> rotReward.getReward().toLowerCase().contains(item.toLowerCase())) ||
                    mission.getRewardsRotationGeneral().stream()
                        .anyMatch(rotReward -> rotReward.getReward().toLowerCase().contains(item.toLowerCase()));
            
            if (isAnyItemInMission) {
                missionsWithItem.add(mission);
            }
        }
        
        return missionsWithItem;
    }

    private EmbedCreateSpec generateEmbed(String item, List<MissionDTO> missionsWithItem) {
        StringBuilder descriptionBuilder = new StringBuilder(DESCRIPTION.replace("{item}", item));
        
        for (int i=0; i<30; i++) {
            MissionDTO mission = missionsWithItem.get(i);
            
            List<RewardDTO> rewardsWithItemGeneral = mission.getRewardsRotationGeneral().stream()
                    .filter(reward -> reward.getReward().toLowerCase().contains(item.toLowerCase()))
                    .collect(Collectors.toList());
            
            List<RewardDTO> rewardsWithItemRotationA = mission.getRewardsRotationA().stream()
                    .filter(reward -> reward.getReward().toLowerCase().contains(item.toLowerCase()))
                    .collect(Collectors.toList());
            
            List<RewardDTO> rewardsWithItemRotationB = mission.getRewardsRotationB().stream()
                    .filter(reward -> reward.getReward().toLowerCase().contains(item.toLowerCase()))
                    .collect(Collectors.toList());
            
            List<RewardDTO> rewardsWithItemRotationC = mission.getRewardsRotationC().stream()
                    .filter(reward -> reward.getReward().toLowerCase().contains(item.toLowerCase()))
                    .collect(Collectors.toList());
            
            rewardsWithItemGeneral.forEach(reward -> descriptionBuilder.append(MISSION_MESSAGE
                    .replace("{title}", mission.getName())
                    .replace("{item}", reward.getReward())
                    .replace(" {rotation}","")
                    .replace("{dropChance}", String.valueOf(reward.getChance().get(0)))));
            
            rewardsWithItemRotationA.forEach(reward -> descriptionBuilder.append(MISSION_MESSAGE
                    .replace("{title}", mission.getName())
                    .replace("{item}", reward.getReward())
                    .replace("{rotation}","Rotation A")
                    .replace("{dropChance}", String.valueOf(reward.getChance().get(0)))));
            
            rewardsWithItemRotationB.forEach(reward -> descriptionBuilder.append(MISSION_MESSAGE
                    .replace("{title}", mission.getName())
                    .replace("{item}", reward.getReward())
                    .replace("{rotation}","Rotation B")
                    .replace("{dropChance}", String.valueOf(reward.getChance().get(0)))));
            
            rewardsWithItemRotationC.forEach(reward -> descriptionBuilder.append(MISSION_MESSAGE
                    .replace("{title}", mission.getName())
                    .replace("{item}", reward.getReward())
                    .replace("{rotation}","Rotation C")
                    .replace("{dropChance}", String.valueOf(reward.getChance().get(0)))));
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
