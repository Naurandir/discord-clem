package at.naurandir.discord.clem.bot.repository;

import at.naurandir.discord.clem.bot.model.InterestingChannel;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Naurandir
 */
@Repository
public interface InterestingChannelRepository extends CrudRepository<InterestingChannel, Long> {
    //
}
