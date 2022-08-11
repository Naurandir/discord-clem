package at.naurandir.discord.clem.bot.repository;

import at.naurandir.discord.clem.bot.model.channel.InterestingChannel;
import at.naurandir.discord.clem.bot.model.enums.PushType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Naurandir
 */
@Repository
public interface InterestingChannelRepository extends CrudRepository<InterestingChannel, Long> {
    
    List<InterestingChannel> findByEndDateIsNull();
    
    List<InterestingChannel> findByPushTypeAndEndDateIsNull(PushType pushType);
    
    Optional<InterestingChannel> findByStickyMessageIdAndEndDateIsNull(Long stickyMessageId);
    
    Optional<InterestingChannel> findByChannelIdAndPushTypeAndEndDateIsNull(Long channelId, PushType pushType);
}
