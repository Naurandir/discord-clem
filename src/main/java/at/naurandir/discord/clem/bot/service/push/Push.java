package at.naurandir.discord.clem.bot.service.push;

import at.naurandir.discord.clem.bot.model.WarframeState;
import discord4j.common.util.Snowflake;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.MessageDeleteEvent;
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
    
    private final Map<Snowflake, Long> channelMessageIdMapping = new HashMap<>();
    private GatewayDiscordClient client;
    
    abstract MessageData doNewPush(Snowflake channelId);
    abstract void doUpdatePush(RestMessage message);
    abstract List<String> getInterestingChannels();
    abstract boolean isOwnMessage(MessageData messageData);
    abstract boolean isSticky();
    abstract void refresh();
    
    public void init(GatewayDiscordClient client) {
        this.client = client;
        
        try {
          for (String channelId : getInterestingChannels()) { 
            Snowflake channelSnowflakeId = Snowflake.of(channelId);
            List<MessageData> messageDataList = client.getRestClient()
                    .getChannelById(channelSnowflakeId).getPinnedMessages()
                    .toStream()
                    .filter(message -> isOwnMessage(message))
                    .collect(Collectors.toList());
            
            for (MessageData messageData : messageDataList) {
                log.info("init: found message id [{}] as existing pinned message from bot, adding to mapping",
                        messageData.id().asString());
                channelMessageIdMapping.put(channelSnowflakeId, messageData.id().asLong());
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
        Long messageId = event.getMessage().getData().id().asLong();
        channelMessageIdMapping.putIfAbsent(channelId, messageId);
        
        if (!channelMessageIdMapping.get(channelId).equals(messageId)) {
            log.warn("handleOwnEvent: current messageId [{}] not the same as in mapping [{}], using now as new one",
                    messageId, channelMessageIdMapping.get(channelId));
            channelMessageIdMapping.put(channelId, messageId);
        }
        
        event.getMessage().pin().subscribe();
    }
    
    public void handleDeleteOwnEvent(MessageDeleteEvent event) {
        if (!isSticky()) {
            return;
        }
        
        if (!isOwnMessage(event.getMessage().get().getData())) {
            return;
        }
        
        log.info("handleDeleteOwnEvent: sticky bot message was deleted, removing from in memory data.");
        channelMessageIdMapping.remove(event.getMessage().get().getChannelId());
    }
    
    public void push() {
        try {
            for (String channelId : getInterestingChannels()) {
                Snowflake channelSnowflake = Snowflake.of(channelId);
                if (!isSticky()) {
                    log.debug("push: new push as [{}] is not sticky.", this.getClass().getSimpleName());
                    doNewPush(channelSnowflake);
                } else if (channelMessageIdMapping.get(channelSnowflake) == null) {
                    log.debug("push: new push in channel [{}] as [{}] is sticky and no message found", 
                            channelId, this.getClass().getSimpleName());
                    MessageData message = doNewPush(channelSnowflake);
                    channelMessageIdMapping.put(channelSnowflake, message.id().asLong());
                } else {
                    Snowflake messageSnowflake = Snowflake.of(channelMessageIdMapping.get(channelSnowflake));
                    log.info("push: update channel [{}] as [{}] sticky, expected older message [{}] found", 
                                channelId, this.getClass().getSimpleName(), messageSnowflake);
                    doUpdatePush(RestMessage.create(client.getRestClient(), channelSnowflake, messageSnowflake));
                }
                log.debug("push: finished push for [{}]", this.getClass());
            }
        } catch (Exception ex) {
            log.error("push: something at push went wrong: ", ex);
        }
    }
    
    GatewayDiscordClient getClient() {
        return client;
    }
}
