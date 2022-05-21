package at.naurandir.discord.clem.bot.service.push;

import at.naurandir.discord.clem.bot.service.WarframeState;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Naurandir
 */
@Slf4j
public abstract class Push {
    
    private final Map<Snowflake, Snowflake> channelMessageMapping = new HashMap<>();
    
    abstract void doNewPush(GatewayDiscordClient client, WarframeState warframeState, Snowflake channelId);
    abstract void doUpdatePush(GatewayDiscordClient client, WarframeState warframeState, Snowflake channelId, Snowflake messageId);
    abstract List<String> getInterestingChannels();
    abstract boolean isOwnMessage(MessageCreateEvent event);
    abstract boolean isSticky();
    
    public void handleOwnEvent(MessageCreateEvent event) {
        if (!isSticky()) {
            return; // nothing to do as own message can be ignored
        }
        
        Snowflake channelId = event.getMessage().getChannelId();
        Snowflake messageId = event.getMessage().getId();
        channelMessageMapping.putIfAbsent(channelId, messageId);
        
        if (!channelMessageMapping.get(channelId).equals(messageId)) {
            log.warn("handleOwnEvent: current messageId [{}] not the same as in mapping [{}]",
                    messageId, channelMessageMapping.get(channelId));
        }
        
        event.getMessage().pin().subscribe();
    }
    
    public void push(GatewayDiscordClient client, WarframeState warframeState) {
        for (String channelId : getInterestingChannels()) {
            Snowflake channelSnowflake = Snowflake.of(channelId);
            if (!isSticky()) {
                doNewPush(client, warframeState, channelSnowflake);
            } else if(channelMessageMapping.get(channelSnowflake) == null) {
                doNewPush(client, warframeState, channelSnowflake);
            } else {
                doUpdatePush(client, warframeState, channelSnowflake, channelMessageMapping.get(channelSnowflake));
            }
        }
    }
}
