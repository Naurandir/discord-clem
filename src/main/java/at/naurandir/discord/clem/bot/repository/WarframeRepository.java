package at.naurandir.discord.clem.bot.repository;

import at.naurandir.discord.clem.bot.model.item.Warframe;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Naurandir
 */
@Repository
public interface WarframeRepository extends CrudRepository<Warframe, Long> {
    
    List<Warframe> findByEndDateIsNull();
    
    List<Warframe> findByNameContainingIgnoreCaseAndEndDateIsNull(String name);
}
