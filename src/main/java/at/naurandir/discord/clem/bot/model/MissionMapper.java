package at.naurandir.discord.clem.bot.model;

import at.naurandir.discord.clem.bot.service.client.dto.worldstate.MissionDTO;
import org.mapstruct.Mapper;

/**
 *
 * @author Naurandir
 */
@Mapper
public interface MissionMapper {

    Mission missionDtoToMission(MissionDTO dto);
    
    default Mission toMission(String node, String missionType, String faction) {
        Mission mission = new Mission();
        mission.setFaction(faction);
        mission.setNode(node);
        mission.setMinEnemyLevel(0);
        mission.setMaxEnemyLevel(0);
        mission.setType(missionType);
        
        return mission;
    }
}
