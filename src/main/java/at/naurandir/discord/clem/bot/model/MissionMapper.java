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
}
