package at.naurandir.discord.clem.bot.service.push;

import at.naurandir.discord.clem.bot.service.WarframeState;
import discord4j.common.util.Snowflake;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.discordjson.json.MessageData;
import discord4j.rest.entity.RestMessage;
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
    abstract void doUpdatePush(RestMessage message, WarframeState warframeState);
    abstract List<String> getInterestingChannels();
    abstract boolean isOwnMessage(MessageData messageData);
    abstract boolean isSticky();
    
    public void init(GatewayDiscordClient client) {
        try {
          for (String channelId : getInterestingChannels()) { 
            Snowflake channelSnowflakeId = Snowflake.of(channelId);
            List<MessageData> messageDataList = client.getRestClient()
                    .getChannelById(channelSnowflakeId).getPinnedMessages()
                    .toStream()
                    .filter(message -> isOwnMessage(message))
                    .collect(Collectors.toList());
            
            for (MessageData messageData : messageDataList) {
                log.info("init: found message with id [{}] as existing pinned message from bot, adding to mapping",
                        messageData.id().asString());
                channelMessageMapping.put(channelSnowflakeId, Snowflake.of(messageData.id().asString()));
            }
        }  
        } catch (Exception ex) {
            log.error("init: push [{}] throwed exception: ", this.getClass(), ex);
        }
    }
    
    public void handleOwnEvent(MessageCreateEvent event) {
        if (!isSticky()) {
            return; // nothing to do as own message can be ignored
        }
        
        if (!isOwnMessage(event.getMessage().getData())) {
            return; // not part of given push impl, can be ignored
        }
        
        Snowflake channelId = event.getMessage().getChannelId();
        Snowflake messageId = event.getMessage().getId();
        channelMessageMapping.putIfAbsent(channelId, messageId);
        
        if (!channelMessageMapping.get(channelId).equals(messageId)) {
            log.warn("handleOwnEvent: current messageId [{}] not the same as in mapping [{}], using now as new one",
                    messageId, channelMessageMapping.get(channelId));
            channelMessageMapping.put(channelId, messageId);
        }
        
        event.getMessage().pin().subscribe();
    }
    
    public void push(GatewayDiscordClient client, WarframeState warframeState) {
        try {
            for (String channelId : getInterestingChannels()) {
                Snowflake channelSnowflake = Snowflake.of(channelId);
                if (!isSticky()) {
                    log.info("push: execute new push as [{}] is not sticky.", this.getClass().getSimpleName());
                    doNewPush(client, warframeState, channelSnowflake);
                } else if (channelMessageMapping.get(channelSnowflake) == null) {
                    log.info("push: execute new push in channel [{}] as [{}] is sticky but no message was found", 
                            channelId, this.getClass().getSimpleName());
                    doNewPush(client, warframeState, channelSnowflake);
                } else {
                    RestMessage message = getMessageById(client, channelSnowflake, channelMessageMapping.get(channelSnowflake));
                    if (message == null) {
                        log.warn("push: execute new push in channel [{}] as [{}] is sticky but expected older message was not found", 
                                channelId, this.getClass().getSimpleName());
                        doNewPush(client, warframeState, channelSnowflake);
                    } else {
                        log.info("push: execute update in channel [{}] as [{}] is sticky and expected older message [{}] was found", 
                                channelId, this.getClass().getSimpleName(), message.getId());
                        doUpdatePush(message, warframeState);
                    }
                }
            }
        } catch (Exception ex) {
            log.error("push: something at push went wrong: ", ex);
        }
    }

    private RestMessage getMessageById(GatewayDiscordClient client, Snowflake channelId, Snowflake messageId) {
        try {
            client.getRestClient().getMessageById(channelId, messageId).getData().block();
            return client.getRestClient().getMessageById(channelId, messageId);
        } catch (Exception ex) {
            log.warn("getMessageById: could not obtain message, error: ", ex.getMessage());
        }
        return null;
    }
}
