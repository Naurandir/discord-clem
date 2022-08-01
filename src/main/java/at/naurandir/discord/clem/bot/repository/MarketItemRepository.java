package at.naurandir.discord.clem.bot.repository;

import at.naurandir.discord.clem.bot.model.market.MarketItem;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Naurandir
 */
@Repository
public interface MarketItemRepository extends CrudRepository<MarketItem, Long> {
    
    List<MarketItem> findByEndDateIsNull();
    
    List<MarketItem> findByNameContainingIgnoreCaseAndEndDateIsNull(String name);
    
}
