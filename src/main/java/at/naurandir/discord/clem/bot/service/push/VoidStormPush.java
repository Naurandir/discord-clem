
package at.naurandir.discord.clem.bot.service.push;

import at.naurandir.discord.clem.bot.model.enums.PushType;
import at.naurandir.discord.clem.bot.model.fissure.VoidFissure;
import at.naurandir.discord.clem.bot.service.VoidFissureService;
import at.naurandir.discord.clem.bot.utils.LocalDateTimeUtil;
import discord4j.common.util.Snowflake;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.json.MessageData;
import discord4j.discordjson.json.MessageEditRequest;
import discord4j.rest.entity.RestMessage;
import discord4j.rest.util.Color;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 *
 * @author Naurandir
 */
@Slf4j
@Component
public class VoidStormPush extends Push {
    
    @Autowired
    private VoidFissureService voidFissureService;

    private static final String TITLE = "Current Void Storms";
    private static final String DESCRIPTION = "Currently active void storms.";
    private static final String STORM_DESCRIPTION = " *Tier:* {tier}\n"
            + " {missionType} ({enemyType})\n"
            + " *expire:* {hours}h {minutes}m";

    @Override
    void doNewPush(Snowflake channelId) {
        EmbedCreateSpec embed = generateEmbed(voidFissureService.getStormVoidFissures());

        getClient().rest().getChannelById(channelId)
                    .createMessage(embed.asRequest())
                    .timeout(Duration.ofSeconds(60)).subscribe();
    }

    @Override
    void doUpdatePush(RestMessage message) {
        MessageEditRequest editRequest = MessageEditRequest.builder()
                .embedOrNull(generateEmbed(voidFissureService.getStormVoidFissures()).asRequest())
                .build();

        message.edit(editRequest).timeout(Duration.ofSeconds(60)).subscribe();
    }

    @Override
    boolean isOwnMessage(MessageData messageData) {
        return messageData.embeds() != null &&
                messageData.embeds().size() > 0 &&
                !messageData.embeds().get(0).title().isAbsent() &&
                messageData.embeds().get(0).title().get().equals(TITLE) &&
                !messageData.embeds().get(0).description().isAbsent() &&
                messageData.embeds().get(0).description().get().equals(DESCRIPTION);
    }

    @Override
    boolean isSticky() {
        return true;
    }
    
    @Override
    PushType getPushType() {
        return PushType.VOID_STORM_PUSH;
    }

    private EmbedCreateSpec generateEmbed(List<VoidFissure> voidFissures) {  
        EmbedCreateSpec.Builder embedBuilder = EmbedCreateSpec.builder()
                .color(Color.YELLOW)
                .title(TITLE)
                .description(DESCRIPTION)
                .thumbnail("https://static.wikia.nocookie.net/warframe/images/0/0e/VoidProjectionsGoldD.png/revision/latest?cb=20160709035734")
                .timestamp(Instant.now());
        
        LocalDateTime now = LocalDateTime.now();

        for (VoidFissure fissure : voidFissures) {

            if (now.isBefore(fissure.getActivation()) || now.isAfter(fissure.getExpiry())) {
                continue;
            }

            Duration diffTime = LocalDateTimeUtil.getDiffTime(LocalDateTime.now(), fissure.getExpiry());

            embedBuilder.addField(fissure.getFissureMission().getNode(), 
                    STORM_DESCRIPTION.replace("{tier}", fissure.getTier())
                                     .replace("{enemyType}", fissure.getFissureMission().getFaction())
                                     .replace("{missionType}", fissure.getFissureMission().getType())
                                     .replace("{hours}", String.valueOf(diffTime.toHours()))
                                     .replace("{minutes}", String.valueOf(diffTime.toMinutesPart())), 
                    true);
        }

        return embedBuilder.build();
    }

    @Scheduled(initialDelay = 60 * 1_000, fixedRate = 60 * 1_000)
    @Override
    void refresh() {
        push();
    }
}
