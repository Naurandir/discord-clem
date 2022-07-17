package at.naurandir.discord.clem.bot.model.fissure;

import at.naurandir.discord.clem.bot.model.Mission;
import at.naurandir.discord.clem.bot.model.MissionMapper;
import at.naurandir.discord.clem.bot.service.client.dto.worldstate.VoidFissureDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Naurandir
 */
@Mapper
public abstract class VoidFissureMapper {
    
    @Autowired
    private MissionMapper missionMapper;

    @Mappings({
        @Mapping(source="id", target="externalId"),
        @Mapping(source="activation", target="activation", dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"),
        @Mapping(source="expiry", target="expiry", dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"),
        @Mapping(target="modifyDate", expression="java(LocalDateTime.now())"),
        @Mapping(target="startDate", expression="java(LocalDateTime.now())"),
        
        @Mapping(source= "dto", target="fissureMission", qualifiedByName = "getMission"),
        
        @Mapping(target = "id", ignore = true)
    })
    public abstract VoidFissure voidFissureDtoToVoidFissure(VoidFissureDTO dto);
    
    @Named("getMission")
    public Mission getMission(VoidFissureDTO dto) {
        return missionMapper.toMission(dto.getNode(), dto.getMissionType(), dto.getEnemy());
    }
}
