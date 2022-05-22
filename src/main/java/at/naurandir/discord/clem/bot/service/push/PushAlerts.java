package at.naurandir.discord.clem.bot.service.push;

import at.naurandir.discord.clem.bot.service.WarframeState;
import at.naurandir.discord.clem.bot.service.client.dto.AlertDTO;
import at.naurandir.discord.clem.bot.utils.LocalDateTimeUtil;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.EmbedCreateSpec.Builder;
import discord4j.discordjson.json.MessageData;
import discord4j.discordjson.json.MessageEditRequest;
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
public class PushAlerts extends Push {
    
    @Value("#{'${discord.clem.push.channels.alerts}'.split(',')}")
    private List<String> interestingChannels;
    
    private static final String TITLE = "Current Alerts";
    private static final String DESCRIPTION = "Currently active alerts are presented here.";
    private static final String ALERT_DESCRIPTION = " type: {type}\n reward: {rewardType}\n expires in: {days}d {hours}h {minutes}m";

    @Override
    void doNewPush(GatewayDiscordClient client, WarframeState warframeState, Snowflake channelId) {
        log.info("doNewPush: adding push message for alerts to channel [{}] ...", channelId);
        EmbedCreateSpec embed = generateEmbed(warframeState);

        client.rest().getChannelById(channelId)
                    .createMessage(embed.asRequest())
                    .subscribe();
        
        log.info("doNewPush: adding push message for alerts to channel [{}] done", channelId);
    }

    @Override
    void doUpdatePush(GatewayDiscordClient client, WarframeState warframeState, Snowflake channelId, Snowflake messageId) {
        log.info("doUpdatePush: update push message for alerts on channel [{}] ...", channelId);
        MessageEditRequest editRequest = MessageEditRequest.builder()
                .embedOrNull(generateEmbed(warframeState).asRequest())
                .build();
        client.getRestClient().getMessageById(channelId, messageId).edit(editRequest).subscribe();
        log.info("doUpdatePush: update push message for alerts on channel [{}] done", channelId);
    }

    @Override
    List<String> getInterestingChannels() {
        return interestingChannels;
    }

    @Override
    boolean isOwnMessage(MessageData messageData) {
        return messageData.embeds() != null && 
                !messageData.embeds().get(0).title().isAbsent() &&
                messageData.embeds().get(0).title().get().equals(TITLE) &&
                !messageData.embeds().get(0).description().isAbsent() &&
                messageData.embeds().get(0).description().get().equals(DESCRIPTION);
    }

    @Override
    boolean isSticky() {
        return true;
    }
    
    private EmbedCreateSpec generateEmbed(WarframeState warframeState) {         
        Builder embedBuilder = EmbedCreateSpec.builder()
                .color(Color.RED)
                .title(TITLE)
                .author("Clem Bot", "https://naurandir.net", "https://cutewallpaper.org/21/warframe-desktop-icon/Warframe-Alerts-7.2.1-Download-APK-for-Android-Aptoide.png")
                .description(DESCRIPTION)
                .thumbnail("https://cutewallpaper.org/21/warframe-desktop-icon/Warframe-Alerts-7.2.1-Download-APK-for-Android-Aptoide.png")
                //.image("https://i.imgur.com/F9BhEoz.png")
                .timestamp(Instant.now())
                .footer("footer", "https://cutewallpaper.org/21/warframe-desktop-icon/Warframe-Alerts-7.2.1-Download-APK-for-Android-Aptoide.png");
        
        for (AlertDTO alert : warframeState.getAlerts()) {
            
            if (!alert.getActive()) {
                log.debug("generateEmbed: found expired alert [{}]", alert);
                continue;
            }
            
            Duration diffTime = LocalDateTimeUtil.getDiffTime(LocalDateTime.now(), alert.getExpiry());
            
            embedBuilder.addField(alert.getMission().getNode(), 
                    ALERT_DESCRIPTION.replace("{type}", alert.getMission().getType())
                                     .replace("{rewardType}", StringUtils.join(alert.getRewardTypes(), ", "))
                                     .replace("{days}", String.valueOf(diffTime.toDays()))
                                     .replace("{hours}", String.valueOf(diffTime.toHoursPart()))
                                     .replace("{minutes}", String.valueOf(diffTime.toMinutesPart())), 
                    true);
        }
        
        return embedBuilder.build();
    }
}
