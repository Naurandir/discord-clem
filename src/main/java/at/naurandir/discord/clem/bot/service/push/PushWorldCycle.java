package at.naurandir.discord.clem.bot.service.push;

import at.naurandir.discord.clem.bot.service.WarframeState;
import at.naurandir.discord.clem.bot.utils.LocalDateTimeUtil;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.discordjson.json.MessageData;
import discord4j.discordjson.json.MessageEditRequest;
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
    public void doNewPush(GatewayDiscordClient client, WarframeState warframeState, Snowflake channelId) {
        log.info("doNewPush: adding push message for world cycle to channel [{}] ...", channelId);
        String message = getMessage(warframeState);

        client.rest().getChannelById(channelId)
                    .createMessage(message)
                    .subscribe();
        
        log.info("doNewPush: adding push message for world cycle to channel [{}] done", channelId);
    }
    
    @Override
    void doUpdatePush(GatewayDiscordClient client, WarframeState warframeState, Snowflake channelId, Snowflake messageId) {
        log.info("doUpdatePush: update push message for world cycle on channel [{}] ...", channelId);
        MessageEditRequest editRequest = MessageEditRequest.builder()
                .contentOrNull(getMessage(warframeState))
                .build();
        client.getRestClient().getMessageById(channelId, messageId).edit(editRequest).subscribe();
        log.info("doUpdatePush: update push message for world cycle on channel [{}] done", channelId);
    }
    
    @Override
    List<String> getInterestingChannels() {
        return interestingChannels;
    }
    
    @Override
    public boolean isOwnMessage(MessageData messageData) {
        if (messageData != null) {
            return messageData.content().startsWith("Cetus:") &&
                   messageData.content().contains("Vallis:");
        }
        return false;
    }

    @Override
    public boolean isSticky() {
        return true;
    }
    
    private String getMessage(WarframeState warframeState) {
        Duration cetusDiffTime = LocalDateTimeUtil.getDiffTime(LocalDateTime.now(), warframeState.getCetusCycle().getExpiry());
        Duration vallisDiffTime = LocalDateTimeUtil.getDiffTime(LocalDateTime.now(), warframeState.getVallisCycle().getExpiry());
        return MESSAGE
                .replace("{stateCetus}", warframeState.getCetusCycle().getState())
                .replace("{hoursCetus}", String.valueOf(cetusDiffTime.toHours()))
                .replace("{minutesCetus}", String.valueOf(cetusDiffTime.toMinutesPart()))
                .replace("{stateVallis}", warframeState.getVallisCycle().getState())
                .replace("{hoursVallis}", String.valueOf(vallisDiffTime.toHours()))
                .replace("{minutesVallis}", String.valueOf(vallisDiffTime.toMinutesPart()));
    }
    
}
