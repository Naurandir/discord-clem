package at.naurandir.discord.clem.bot.service.push;

import at.naurandir.discord.clem.bot.model.WarframeState;
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
    
    private final Map<Snowflake, MessageData> channelMessageMapping = new HashMap<>();
    
    abstract MessageData doNewPush(GatewayDiscordClient client, WarframeState warframeState, Snowflake channelId);
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
                log.info("init: found message id [{}] as existing pinned message from bot, adding to mapping",
                        messageData.id().asString());
                channelMessageMapping.put(channelSnowflakeId, messageData);
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
        MessageData data = event.getMessage().getData();
        channelMessageMapping.putIfAbsent(channelId, data);
        
        if (!channelMessageMapping.get(channelId).equals(data)) {
            log.warn("handleOwnEvent: current messageId [{}] not the same as in mapping [{}], using now as new one",
                    data.id(), channelMessageMapping.get(channelId));
            channelMessageMapping.put(channelId, data);
        }
        
        event.getMessage().pin().subscribe();
    }
    
    public void push(GatewayDiscordClient client, WarframeState warframeState) {
        try {
            for (String channelId : getInterestingChannels()) {
                Snowflake channelSnowflake = Snowflake.of(channelId);
                if (!isSticky()) {
                    log.debug("push: new push as [{}] is not sticky.", this.getClass().getSimpleName());
                    doNewPush(client, warframeState, channelSnowflake);
                } else if (channelMessageMapping.get(channelSnowflake) == null) {
                    log.debug("push: new push in channel [{}] as [{}] is sticky and no message found", 
                            channelId, this.getClass().getSimpleName());
                    MessageData message = doNewPush(client, warframeState, channelSnowflake);
                    channelMessageMapping.put(channelSnowflake, message);
                } else {
                    MessageData message = channelMessageMapping.get(channelSnowflake);
                    log.info("push: update channel [{}] as [{}] sticky, expected older message [{}] found", 
                                channelId, this.getClass().getSimpleName(), message.id());
                        doUpdatePush(RestMessage.create(client.getRestClient(), channelSnowflake, Snowflake.of(message.id().asLong())), warframeState);
                }
                log.debug("push: finished push for [{}]", this.getClass());
            }
        } catch (Exception ex) {
            log.error("push: something at push went wrong: ", ex);
        }
    }
}
