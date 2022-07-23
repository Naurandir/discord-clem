package at.naurandir.discord.clem.bot.repository;

import at.naurandir.discord.clem.bot.model.relic.Relic;
import at.naurandir.discord.clem.bot.model.relic.RelicDrop;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Naurandir
 */
@Repository
public interface RelicDropRepository extends CrudRepository<RelicDrop, Long> {

    List<RelicDrop> findByRelic(Relic relic);
    
    void deleteByRelic(Relic relic);
}
