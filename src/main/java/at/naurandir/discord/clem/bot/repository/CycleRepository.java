package at.naurandir.discord.clem.bot.repository;

import at.naurandir.discord.clem.bot.model.cycle.Cycle;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Naurandir
 */
@Repository
public interface CycleRepository extends CrudRepository<Cycle, Long> {
    
    @Override
    List<Cycle> findAll();
}
