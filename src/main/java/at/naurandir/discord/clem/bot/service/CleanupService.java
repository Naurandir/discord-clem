package at.naurandir.discord.clem.bot.service;

import at.naurandir.discord.clem.bot.model.channel.InterestingChannel;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.rest.http.client.ClientException;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 *
 * @author Naurandir
 */
@Slf4j
@Service
public class CleanupService {
    
    @Autowired
    private BotService botService;
    
    @Autowired
    private InterestingChannelService interestingChannelService;
    
    @Scheduled(cron = "${discord.clem.cleanup}")
    public void removeNonExistendStickyPushMessages() {
        List<InterestingChannel> channels = interestingChannelService.getAllChannels();
        
        for(InterestingChannel channel : channels) {
            if (channel.getStickyMessageId() == null) {
                continue;
            }  
            
            try {
                botService.getClient().getMessageById(Snowflake.of(channel.getChannelId()), 
                        Snowflake.of(channel.getStickyMessageId())).block();
            } catch (ClientException ex) {
                log.error("removeNonExistendStickyPushMessages: did not receive message [{}], reason: {}", 
                        channel.getStickyMessageId(), ex.getMessage());
                log.info("removeNonExistendStickyPushMessages: removing sticky message id from channel [{}]", channel);
                interestingChannelService.removeStickyMessageId(channel);
            }
        }
    }
}
