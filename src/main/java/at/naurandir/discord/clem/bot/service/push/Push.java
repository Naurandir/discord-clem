package at.naurandir.discord.clem.bot.service.push;

import at.naurandir.discord.clem.bot.service.WarframeState;
import discord4j.common.util.Snowflake;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.discordjson.json.MessageData;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
    abstract boolean isOwnMessage(Message message, MessageData messageData);
    abstract boolean isSticky();
    
    public void init(GatewayDiscordClient client) {
        for (String channelId : getInterestingChannels()) { 
            Snowflake channelSnowflakeId = Snowflake.of(channelId);
            List<MessageData> messageDataList = client.getRestClient()
                    .getChannelById(channelSnowflakeId).getPinnedMessages()
                    .toStream()
                    .filter(message -> isOwnMessage(null, message))
                    .collect(Collectors.toList());
            
            for (MessageData messageData : messageDataList) {
                log.info("init: found message with id [{}] as existing pinned message from bot, adding to mapping",
                        messageData.id().asString());
                channelMessageMapping.put(channelSnowflakeId, Snowflake.of(messageData.id().asString()));
            }
        }
    }
    
    public void handleOwnEvent(MessageCreateEvent event) {
        if (!isSticky()) {
            return; // nothing to do as own message can be ignored
        }
        
        if (!isOwnMessage(event.getMessage(), null)) {
            return; // not part of given push impl, can be ignored
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
