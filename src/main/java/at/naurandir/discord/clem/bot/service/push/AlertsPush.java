package at.naurandir.discord.clem.bot.service.push;

import at.naurandir.discord.clem.bot.model.alert.Alert;
import at.naurandir.discord.clem.bot.model.enums.PushType;
import at.naurandir.discord.clem.bot.service.AlertService;
import at.naurandir.discord.clem.bot.utils.LocalDateTimeUtil;
import discord4j.common.util.Snowflake;
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
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 *
 * @author Naurandir
 */
@Slf4j
@Component
public class AlertsPush extends Push {
    
    @Autowired
    private AlertService alertService;
    
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
    void doNewPush(Snowflake channelId) {
        EmbedCreateSpec embed = generateEmbed(alertService.getActiveAlerts());

        getClient().rest().getChannelById(channelId)
                    .createMessage(embed.asRequest())
                    .subscribe();
    }

    @Override
    void doUpdatePush(RestMessage message) {
        MessageEditRequest editRequest = MessageEditRequest.builder()
                .embedOrNull(generateEmbed(alertService.getActiveAlerts()).asRequest())
                .build();
        
        message.edit(editRequest).subscribe();
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
    
    @Override
    PushType getPushType() {
        return PushType.ALERT_PUSH;
    }
    
    private EmbedCreateSpec generateEmbed(List<Alert> activeAlerts) {
        String description = activeAlerts.isEmpty() ? DESCRIPTION_NO_ALERTS : DESCRIPTION;
        
        Builder embedBuilder = EmbedCreateSpec.builder()
                .color(Color.RED)
                .title(TITLE)
                .description(description)
                .thumbnail("https://cutewallpaper.org/21/warframe-desktop-icon/Warframe-Alerts-7.2.1-Download-APK-for-Android-Aptoide.png")
                .timestamp(Instant.now());
        
        for (Alert alert : activeAlerts) {
            
            Duration diffTime = LocalDateTimeUtil.getDiffTime(LocalDateTime.now(), alert.getExpiry());
            if (diffTime.isNegative()) {
                log.debug("generateEmbed: found expired [{}] alert [{}]", diffTime, alert);
                continue;
            }
            
            Integer minLevel = Objects.requireNonNullElse(alert.getAlertMission().getMinLevelEnemy(), 1);
            Integer maxLevel = Objects.requireNonNullElse(alert.getAlertMission().getMaxLevelEnemy(), 999);
            
            embedBuilder.addField(alert.getAlertMission().getNode(), 
                    ALERT_DESCRIPTION.replace("{type}", alert.getAlertMission().getType())
                                     .replace("{reward}", alert.getRewards())
                                     .replace("{enemyType}", alert.getAlertMission().getFaction())
                                     .replace("{minLevel}", String.valueOf(minLevel))
                                     .replace("{maxLevel}", String.valueOf(maxLevel))
                                     .replace("{days}", String.valueOf(diffTime.toDays()))
                                     .replace("{hours}", String.valueOf(diffTime.toHoursPart()))
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
