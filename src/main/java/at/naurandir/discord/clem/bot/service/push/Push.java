package at.naurandir.discord.clem.bot.service.push;

import at.naurandir.discord.clem.bot.service.WarframeState;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Naurandir
 */
@Slf4j
public abstract class Push {
    
    private final Map<Snowflake, Snowflake> channelLastMessageMapping = new HashMap<>();
    
    public abstract void push(GatewayDiscordClient client, WarframeState warframeState);
    public abstract boolean isOwnMessage(MessageCreateEvent event);
    public abstract boolean isSticky();
    
    public void handleOwnEvent(MessageCreateEvent event, GatewayDiscordClient client) {
        if (!isOwnMessage(event)) {
            return;
        }
        
        Snowflake channelId = event.getMessage().getChannelId();
        Snowflake lastMessageId = channelLastMessageMapping.get(channelId);
        if (channelLastMessageMapping.get(channelId) != null) {
            log.info("handleOwnEvent: found an existing message [{}], deleting", lastMessageId);
            client.getRestClient()
                    .getMessageById(channelId, channelLastMessageMapping.get(channelId))
                    .delete("replace with newer information")
                    .subscribe();
        } else {
            log.info("handleOwnEvent: no lastMessageId found for channel [{}]", channelId);
        }
        
        channelLastMessageMapping.put(channelId, event.getMessage().getId());
        
        if (isSticky()) {
            event.getMessage().pin().subscribe();
        }
    }
}
