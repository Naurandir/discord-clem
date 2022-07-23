package at.naurandir.discord.clem.bot.repository;

import at.naurandir.discord.clem.bot.model.fissure.VoidFissure;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Naurandir
 */
@Repository
public interface VoidFissureRepository extends CrudRepository<VoidFissure, Long> {
    
    List<VoidFissure> findByEndDateIsNull();
    
    List<VoidFissure> findByEndDateIsNullAndIsStorm(boolean isStorm);
}
