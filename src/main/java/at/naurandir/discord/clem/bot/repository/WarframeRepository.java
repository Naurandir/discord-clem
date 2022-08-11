package at.naurandir.discord.clem.bot.repository;

import at.naurandir.discord.clem.bot.model.item.MarketType;
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
    
    List<Warframe> findByMarketTypeAndEndDateIsNull(MarketType marketType);
    
    List<Warframe> findByNameContainingIgnoreCaseAndEndDateIsNull(String name);
    
    List<Warframe> findByNameContainingIgnoreCaseAndMarketTypeAndEndDateIsNull(String name, MarketType marketType);
}
