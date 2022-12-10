package at.naurandir.discord.clem.bot.service.command;

import at.naurandir.discord.clem.bot.model.mission.Mission;
import at.naurandir.discord.clem.bot.model.mission.MissionReward;
import at.naurandir.discord.clem.bot.service.MissionService;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.spec.EmbedCreateFields;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.legacy.LegacyMessageCreateSpec;
import discord4j.rest.util.Color;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 *
 * @author Naurandir
 */
@Slf4j
@Component
public class NodeDropCommand implements Command {
    
    @Autowired
    private MissionService missionService;

    private static final String TITLE = "Found Nodes dropping with '{node}'";
    private static final String DESCRIPTION = "Nodes with given input are shown with all Rewards, if multiple smilar nodes exists note that only first 3 found are shown.\n\n";
    
    private static final String MISSION_MESSAGE = "***{node}*** - {type} {faction} ({minLevelEnemy} - {maxLevelEnemy})\n";
    private static final String GENERAL_REWARD_MESSAGE = "*{item}* - {rarity}, {chance}% drop chance\n";
    private static final String ROTATION_REWARD_MESSAGE = "*{item}* - {rotation}, {rarity}, {chance}% drop chance\n";
    
    @Override
    public String getCommandWord() {
        return "node-drop";
    }

    @Override
    public String getDescription() {
        return "Shows all Drops from the Drop Table of a specific Node.\n"
                + "Usage: *<bot-prefix> node-drop <node>*.";
    }

    @Override
    public Mono<Void> handle(MessageCreateEvent event) {
        String node = getNode(event.getMessage().getContent());
        
        if (node.length() < 3) {
            return event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage("The given input [" + node + "] is too short. Please provide a longer name to search"))
                .then();
        }
        
        List<Mission> missionsWithNode = getMissionsWithNode(node);
        log.debug("handle: found [{}] missions for [{}]", missionsWithNode.size(), node);
        
        return event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage(generateEmbed(node, missionsWithNode)))
                .then();
    }
    
    private String getNode(String content) {
        String[] splitted = content.split(" ");
        String[] words = Arrays.copyOfRange(splitted, 2, splitted.length);
        return StringUtils.join(words, " ").trim();
    }
    
    private List<Mission> getMissionsWithNode(String node) {
        List<Mission> allMissions = missionService.getMissions();
        List<Mission> missionsWithNode = new ArrayList<>();

        for (Mission mission : allMissions) {
            if (isEmpty(mission.getNode())) {
                continue; // special mission without node, not interesting here
            }
            
            if (mission.getNode().toLowerCase().contains(node.toLowerCase())) {
                missionsWithNode.add(mission);
            }
        }
        return missionsWithNode;
    }

    private EmbedCreateSpec generateEmbed(String node, List<Mission> missionsWithNode) {
        StringBuilder descriptionBuilder = new StringBuilder(DESCRIPTION.replace("{node}", node));
        
        List<EmbedCreateFields.Field> fields = getNodeRewardMessages(missionsWithNode);
        
        if (fields.isEmpty()) {
            descriptionBuilder.append("Nothing found in the Search. Maybe the Node was misspelled?");
        }
        
        EmbedCreateSpec.Builder embedBuilder = EmbedCreateSpec.builder()
                .color(Color.BLUE)
                .title(TITLE.replace("{node}", node))
                .description(descriptionBuilder.toString())
                .thumbnail("https://preview.redd.it/99m1fk0q9x8z.jpg?width=640&crop=smart&auto=webp&s=625d5cc7a395ecff11d9b58df20eb949ae92ac7d")
                .timestamp(Instant.now());
        
        fields.forEach(field -> embedBuilder.addFields(field));        
        
        return embedBuilder.build();
    }

    private List<EmbedCreateFields.Field> getNodeRewardMessages(List<Mission> missionsWithNode) {
        List<EmbedCreateFields.Field> fields = new ArrayList<>();
        
        for (Mission mission : missionsWithNode) {
            StringBuilder rewards = new StringBuilder("");
            for (MissionReward reward : mission.getGeneralRewards()) {
                rewards.append(GENERAL_REWARD_MESSAGE
                    .replace("{item}", reward.getName())
                    .replace("{rarity}", reward.getRarity().name())
                    .replace("{chance}", String.valueOf(reward.getChance())));
            }
            
            for (MissionReward reward : mission.getRotationARewards()) {
                rewards.append(ROTATION_REWARD_MESSAGE
                    .replace("{item}", reward.getName())
                    .replace("{rotation}", "Rotation A")
                    .replace("{rarity}", reward.getRarity().name())
                    .replace("{chance}", String.valueOf(reward.getChance())));
            }
            
            for (MissionReward reward : mission.getRotationBRewards()) {
                rewards.append(ROTATION_REWARD_MESSAGE
                    .replace("{item}", reward.getName())
                    .replace("{rotation}", "Rotation B")
                    .replace("{rarity}", reward.getRarity().name())
                    .replace("{chance}", String.valueOf(reward.getChance())));
            }
            
            for (MissionReward reward : mission.getRotationCRewards()) {
                rewards.append(ROTATION_REWARD_MESSAGE
                    .replace("{item}", reward.getName())
                    .replace("{rotation}", "Rotation C")
                    .replace("{rarity}", reward.getRarity().name())
                    .replace("{chance}", String.valueOf(reward.getChance())));
            }
            
            fields.add(EmbedCreateFields.Field.of(MISSION_MESSAGE
                    .replace("{node}", mission.getNode())
                    .replace("{type}", mission.getType())
                    .replace("{faction}", mission.getFaction())
                    .replace("minLevelEnemy", String.valueOf(Objects.requireNonNullElseGet(mission.getMinLevelEnemy(), () -> "-")))
                    .replace("maxLevelEnemy", String.valueOf(Objects.requireNonNullElseGet(mission.getMaxLevelEnemy(), () -> "-"))), 
                    rewards.toString(), 
                    false));
        }
        
        return fields;
    }
}
