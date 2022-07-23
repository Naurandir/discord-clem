package at.naurandir.discord.clem.bot.model.alert;

import at.naurandir.discord.clem.bot.model.mission.EmbeddableMission;
import at.naurandir.discord.clem.bot.model.mission.MissionMapper;
import at.naurandir.discord.clem.bot.service.client.dto.worldstate.AlertDTO;
import at.naurandir.discord.clem.bot.service.client.dto.worldstate.EmbeddableMissionDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author aspiel
 */
@Mapper
public abstract class AlertMapper {
    
    @Autowired
    private MissionMapper missionMapper;
    
    @Mappings({
        @Mapping(source="id", target="externalId"),
        @Mapping(source="activation", target="activation", dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"),
        @Mapping(source="expiry", target="expiry", dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"),
        @Mapping(target="modifyDate", expression="java(LocalDateTime.now())"),
        @Mapping(target="startDate", expression="java(LocalDateTime.now())"),
        
        @Mapping(source= "dto", target="name", qualifiedByName = "getAlertName"),
        @Mapping(source="dto", target="rewards", qualifiedByName = "getRewards"),
        @Mapping(source= "mission", target="alertMission", qualifiedByName = "getMission"),
        
        @Mapping(target = "id", ignore = true)
    })
    public abstract Alert alertDtoToAlert(AlertDTO dto);
    
    @Named("getAlertName")
    public String getAlertName(AlertDTO dto) {
        return dto.getMission().getNode() + " - " + dto.getMission().getType();
    }
    
    @Named("getRewards")
    public String getRewards(AlertDTO dto) {
        return dto.getMission().getRewardDTO().getAsString();
    }
    
    @Named("getMission")
    public EmbeddableMission getMission(EmbeddableMissionDTO missionDto) {
        return missionMapper.missionDtoToEmbeddableMission(missionDto);
    }
}
