package at.naurandir.discord.clem.bot.repository;

import at.naurandir.discord.clem.bot.model.item.ItemBuild;
import at.naurandir.discord.clem.bot.model.item.Warframe;
import at.naurandir.discord.clem.bot.model.item.Weapon;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Naurandir
 */
@Repository
public interface ItemBuildRepository extends CrudRepository<ItemBuild, Long> {
    
    Set<ItemBuild> findByWarframeAndEndDateIsNull(Warframe warframe);
    
    Set<ItemBuild> findByWeaponAndEndDateIsNull(Weapon weapon);
    
    Set<ItemBuild> findByModifyDateBefore(LocalDateTime expiryDate);
    
    List<ItemBuild> findByEndDateIsNotNull();
}
