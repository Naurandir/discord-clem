package at.naurandir.discord.clem.bot.repository;

import at.naurandir.discord.clem.bot.model.relic.Relic;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Naurandir
 */
@Repository
public interface RelicRepository extends CrudRepository<Relic, Long> {
    
    @Override
    List<Relic> findAll();
    
    List<Relic> findByEndDateIsNull();
}
