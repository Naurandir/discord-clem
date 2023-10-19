package at.naurandir.discord.clem.bot.service.push;

import at.naurandir.discord.clem.bot.model.channel.InterestingChannel;
import at.naurandir.discord.clem.bot.model.enums.PushType;
import at.naurandir.discord.clem.bot.service.InterestingChannelService;
import discord4j.common.util.Snowflake;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.MessageDeleteEvent;
import discord4j.discordjson.json.MessageData;
import discord4j.rest.entity.RestMessage;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Naurandir
 */
@Slf4j
public abstract class Push {
    
    @Autowired
    private InterestingChannelService interestingChannelService;
    
    private GatewayDiscordClient client;
    
    abstract void doNewPush(Snowflake channelId);
    abstract void doUpdatePush(RestMessage message);
    abstract boolean isOwnMessage(MessageData messageData);
    abstract boolean isSticky();
    abstract PushType getPushType();
    abstract void refresh();
    
    public void init(GatewayDiscordClient client) {
        this.client = client;
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
        
        Optional<InterestingChannel> interestingChannel = interestingChannelService.getChannel(channelId.asLong(), getPushType());
        if (interestingChannel.isEmpty()) {
            log.error("handleOwnEvent: no existing interestingChannel found where to set the messageId, aborting.");
            return;
        }
        
        interestingChannelService.addStickyMessageIdToChannel(messageId, interestingChannel.get());
        event.getMessage().pin().subscribe();
    }
    
    public void handleDeleteOwnEvent(MessageDeleteEvent event) {
        if (!isSticky()) {
            return;
        }
        
        if (!isOwnMessage(event.getMessage().get().getData())) {
            return;
        }
        
        Snowflake channelId = event.getMessage().get().getChannelId();
        
        Optional<InterestingChannel> interestingChannel = interestingChannelService.getChannel(channelId.asLong(), getPushType());
        if (interestingChannel.isEmpty()) {
            log.warn("handleDeleteOwnEvent: no existing interestingChannel found to delete, ignoring.");
            return;
        }
        
        log.info("handleDeleteOwnEvent: sticky bot message was deleted, removing.");
        interestingChannelService.deleteChannel(interestingChannel.get());
    }
    
    public void push() {
        try {
            for (InterestingChannel channel : getInterestingChannels()) {
                Snowflake channelSnowflake = Snowflake.of(channel.getChannelId());
                Optional<InterestingChannel> interestingChannel = interestingChannelService.getChannel(
                        channel.getChannelId(), getPushType());
                
                if (interestingChannel.isEmpty()) {
                    log.error("push: no interesting channel found for [{}], this should not happen, aborting", channelSnowflake);
                    return;
                }
                
                if (!isSticky()) {
                    log.debug("push: new push as [{}] is not sticky.", this.getClass().getSimpleName());
                    doNewPush(channelSnowflake);
                } else if (interestingChannel.get().getStickyMessageId() == null) {
                    log.debug("push: new push in channel [{}] as [{}] is sticky and no message found", 
                            channel.getChannelId(), this.getClass().getSimpleName());
                    doNewPush(channelSnowflake);
                } else {
                    Snowflake messageSnowflake = Snowflake.of(interestingChannel.get().getStickyMessageId());
                    log.info("push: update channel [{}] as [{}] sticky, expected older message [{}] found", 
                                channel.getChannelId(), this.getClass().getSimpleName(), messageSnowflake);
                    doUpdatePush(RestMessage.create(client.getRestClient(), channelSnowflake, messageSnowflake));
                }
                log.debug("push: finished push for [{}]", this.getClass());
            }
        } catch (Exception ex) {
            log.error("push: something at push went wrong: ", ex);
        }
    }
    
    List<InterestingChannel> getInterestingChannels() {
        return interestingChannelService.findByPushType(getPushType());
    }
    
    GatewayDiscordClient getClient() {
        return client;
    }
}
