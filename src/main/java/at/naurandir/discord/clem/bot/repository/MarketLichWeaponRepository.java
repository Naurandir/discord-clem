package at.naurandir.discord.clem.bot.repository;

import at.naurandir.discord.clem.bot.model.market.MarketLichWeapon;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Naurandir
 */
@Repository
public interface MarketLichWeaponRepository extends CrudRepository<MarketLichWeapon, Long> {
    
    List<MarketLichWeapon> findByEndDateIsNull();
    
    List<MarketLichWeapon> findByNameContainingIgnoreCaseAndEndDateIsNull(String name);
    
}
