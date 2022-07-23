package at.naurandir.discord.clem.bot.repository;

import at.naurandir.discord.clem.bot.model.mission.Mission;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Naurandir
 */
@Repository
public interface MissionRepository extends CrudRepository<Mission, Long> {
    
    List<Mission> findByEndDateIsNull();
}
