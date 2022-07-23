package at.naurandir.discord.clem.bot.repository;

import at.naurandir.discord.clem.bot.model.mission.Mission;
import at.naurandir.discord.clem.bot.model.mission.MissionReward;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Naurandir
 */
@Repository
public interface MissionRewardRepository extends CrudRepository<MissionReward, Long> {
    
    List<MissionReward> findByMission(Mission mission);
    void deleteByMission(Mission mission);
}
