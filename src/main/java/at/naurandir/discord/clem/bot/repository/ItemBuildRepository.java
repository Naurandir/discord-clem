package at.naurandir.discord.clem.bot.repository;

import at.naurandir.discord.clem.bot.model.item.ItemBuild;
import at.naurandir.discord.clem.bot.model.item.Warframe;
import at.naurandir.discord.clem.bot.model.item.Weapon;
import java.util.Set;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author Naurandir
 */
public interface ItemBuildRepository extends CrudRepository<ItemBuild, Long> {
    
    Set<ItemBuild> findByWarframeAndEndDateIsNull(Warframe warframe);
    
    Set<ItemBuild> findByWeaponAndEndDateIsNull(Weapon weapon);
    
}
