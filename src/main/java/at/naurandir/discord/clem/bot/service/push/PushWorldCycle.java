package at.naurandir.discord.clem.bot.service.push;

import at.naurandir.discord.clem.bot.service.WarframeState;
import at.naurandir.discord.clem.bot.utils.LocalDateTimeUtil;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author Naurandir
 */
@Slf4j
@Component
public class PushWorldCycle extends Push {
    
    @Value("#{'${discord.clem.push.channels.worldcycle}'.split(',')}")
    private List<String> interestingChannels;
    
    private static final String MESSAGE = "Cetus: {stateCetus} for {hoursCetus}h {minutesCetus}m.\n"
                                               + "Vallis: {stateVallis} for {hoursVallis}h {minutesVallis}m.";

    @Override
    public void push(GatewayDiscordClient client, WarframeState warframeState) {
        log.info("push: adding push message for world cycle to all channels...");
        Duration cetusDiffTime = LocalDateTimeUtil.getDiffTime(LocalDateTime.now(), warframeState.getCetusCycle().getExpiry());
        Duration vallisDiffTime = LocalDateTimeUtil.getDiffTime(LocalDateTime.now(), warframeState.getVallisCycle().getExpiry());

        String message = MESSAGE
                .replace("{stateCetus}", warframeState.getCetusCycle().getState())
                .replace("{hoursCetus}", String.valueOf(cetusDiffTime.toHours()))
                .replace("{minutesCetus}", String.valueOf(cetusDiffTime.toMinutesPart()))
                .replace("{stateVallis}", warframeState.getVallisCycle().getState())
                .replace("{hoursVallis}", String.valueOf(vallisDiffTime.toHours()))
                .replace("{minutesVallis}", String.valueOf(vallisDiffTime.toMinutesPart()));

        sendMessageIntoChannels(client, message);
        log.info("push: adding push message for world cycle to all channels done");
    }
    
    private void sendMessageIntoChannels(GatewayDiscordClient client, String message) {
        for (String channelId : interestingChannels) {
            client.rest().getChannelById(Snowflake.of(channelId))
                    .createMessage(message)
                    .subscribe();
        }
    }

    @Override
    public boolean isOwnMessage(MessageCreateEvent event) {
        return event.getMessage().getContent().startsWith("Cetus:") &&
                event.getMessage().getContent().contains("Vallis:");
    }

    @Override
    public boolean isSticky() {
        return true;
    }
    
}
