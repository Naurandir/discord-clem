package at.naurandir.discord.clem.bot.service.push;

import at.naurandir.discord.clem.bot.model.WarframeState;
import at.naurandir.discord.clem.bot.service.client.dto.worldstate.AlertDTO;
import at.naurandir.discord.clem.bot.utils.LocalDateTimeUtil;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.EmbedCreateSpec.Builder;
import discord4j.discordjson.json.MessageData;
import discord4j.discordjson.json.MessageEditRequest;
import discord4j.rest.entity.RestMessage;
import discord4j.rest.util.Color;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author Naurandir
 */
@Slf4j
@Component
public class AlertsPush extends Push {
    
    @Value("#{'${discord.clem.push.channels.alerts}'.split(',')}")
    private List<String> interestingChannels;
    
    private static final String TITLE = "Current Alerts";
    private static final String DESCRIPTION = "Currently active alerts.";
    private static final String DESCRIPTION_NO_ALERTS = "Currently no active alerts in Warframe.";
    private static final String ALERT_DESCRIPTION = " *type:* {type}\n"
            + " *enemies:* {enemyType} ({minLevel} - {maxLevel})\n"
            + " *reward:* {reward}\n"
            + " *expires in:* {days}d {hours}h {minutes}m";

    @Override
    void doNewPush(GatewayDiscordClient client, WarframeState warframeState, Snowflake channelId) {
        EmbedCreateSpec embed = generateEmbed(warframeState);

        client.rest().getChannelById(channelId)
                    .createMessage(embed.asRequest())
                    .subscribe();
    }

    @Override
    void doUpdatePush(RestMessage message, WarframeState warframeState) {
        MessageEditRequest editRequest = MessageEditRequest.builder()
                .embedOrNull(generateEmbed(warframeState).asRequest())
                .build();
        
        message.edit(editRequest).subscribe();
    }

    @Override
    List<String> getInterestingChannels() {
        return interestingChannels;
    }

    @Override
    boolean isOwnMessage(MessageData messageData) {
        return messageData.embeds() != null &&
                messageData.embeds().size() > 0 &&
                !messageData.embeds().get(0).title().isAbsent() &&
                messageData.embeds().get(0).title().get().equals(TITLE) &&
                !messageData.embeds().get(0).description().isAbsent() &&
                (messageData.embeds().get(0).description().get().equals(DESCRIPTION) ||
                 messageData.embeds().get(0).description().get().equals(DESCRIPTION_NO_ALERTS));
    }

    @Override
    boolean isSticky() {
        return true;
    }
    
    private EmbedCreateSpec generateEmbed(WarframeState warframeState) {  
        String description = warframeState.getAlerts().stream().anyMatch(alert -> alert.getActive()) ? DESCRIPTION : DESCRIPTION_NO_ALERTS;
        
        Builder embedBuilder = EmbedCreateSpec.builder()
                .color(Color.RED)
                .title(TITLE)
                .description(description)
                .thumbnail("https://cutewallpaper.org/21/warframe-desktop-icon/Warframe-Alerts-7.2.1-Download-APK-for-Android-Aptoide.png")
                .timestamp(Instant.now());
        
        for (AlertDTO alert : warframeState.getAlerts()) {
            
            if (!alert.getActive()) {
                log.debug("generateEmbed: found expired alert [{}]", alert);
                continue;
            }
            
            Duration diffTime = LocalDateTimeUtil.getDiffTime(LocalDateTime.now(), alert.getExpiry());
            Integer minLevel = alert.getMission().getMinEnemyLevel() == null ? 1 : alert.getMission().getMinEnemyLevel();
            Integer maxLevel = alert.getMission().getMaxEnemyLevel() == null ? 999 : alert.getMission().getMaxEnemyLevel();
            
            embedBuilder.addField(alert.getMission().getNode(), 
                    ALERT_DESCRIPTION.replace("{type}", alert.getMission().getType())
                                     .replace("{reward}", StringUtils.join(alert.getMission().getRewartDTO().getAsString(), ", "))
                                     .replace("{enemyType}", alert.getMission().getFaction())
                                     .replace("{minLevel}", String.valueOf(minLevel))
                                     .replace("{maxLevel}", String.valueOf(maxLevel))
                                     .replace("{days}", String.valueOf(diffTime.toDays()))
                                     .replace("{hours}", String.valueOf(diffTime.toHoursPart()))
                                     .replace("{minutes}", String.valueOf(diffTime.toMinutesPart())), 
                    true);
        }
        
        return embedBuilder.build();
    }
}
