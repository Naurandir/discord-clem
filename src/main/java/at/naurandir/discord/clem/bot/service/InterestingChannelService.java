package at.naurandir.discord.clem.bot.service;

import at.naurandir.discord.clem.bot.model.channel.InterestingChannel;
import at.naurandir.discord.clem.bot.model.enums.PushType;
import at.naurandir.discord.clem.bot.repository.InterestingChannelRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Naurandir
 */
@Slf4j
@Service
public class InterestingChannelService {
    
    @Autowired
    private InterestingChannelRepository interestingChannelRepository;
    
    public InterestingChannel createOrUpdateChannel(Long channelId, PushType type, String creator) {
        InterestingChannel channel = interestingChannelRepository
                .findByChannelIdAndPushTypeAndEndDateIsNull(channelId, type)
                .orElse(null);
        
        if (channel == null) {
            channel = new InterestingChannel();
            channel.setName(type + " by " + creator);
            
            channel.setChannelId(channelId);
            channel.setPushType(type);
            
            channel.setStartDate(LocalDateTime.now());
        }
        
        channel.setCreator(creator);
        channel.setModifyDate(LocalDateTime.now());
        
        return interestingChannelRepository.save(channel);
    }
    
    public void deleteChannel(InterestingChannel channel) {
        channel.setModifyDate(LocalDateTime.now());
        channel.setEndDate(LocalDateTime.now());
        
        interestingChannelRepository.save(channel);
    }
    
    public Optional<InterestingChannel> getChannel(Long channelId, PushType type) {
        return interestingChannelRepository
                .findByChannelIdAndPushTypeAndEndDateIsNull(channelId, type);
    }
    
    public List<InterestingChannel> getAllChannels() {
        return interestingChannelRepository.findByEndDateIsNull();
    }
    
    public InterestingChannel addStickyMessageIdToChannel(Long stickyMessageId, InterestingChannel channel) {
        channel.setModifyDate(LocalDateTime.now());
        
        if (channel.getStickyMessageId() != null) {
            log.info("addStickyMessageIdToChannel: already having a sticky message id [{}], update with new [{}]",
                    channel.getStickyMessageId(), stickyMessageId);
        }
        channel.setStickyMessageId(stickyMessageId);
        
        return interestingChannelRepository.save(channel);
    }
    
    public InterestingChannel removeStickyMessageId(InterestingChannel channel) {
        channel.setModifyDate(LocalDateTime.now());
        
        if (channel.getStickyMessageId() == null) {
            log.info("removeStickyMessageId: already removed sticky message id from channel [{}]", channel);
            return channel;
        }
        
        channel.setStickyMessageId(null);
        return interestingChannelRepository.save(channel);
    }
    
    public List<InterestingChannel> findByPushType(PushType type) {
        return interestingChannelRepository.findByPushTypeAndEndDateIsNull(type);
    }
}
