package at.naurandir.discord.clem.bot.repository;

import at.naurandir.discord.clem.bot.model.item.Weapon;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Naurandir
 */
@Repository
public interface WeaponRepository extends CrudRepository<Weapon, Long> {  
    List<Weapon> findByEndDateIsNull();
}
