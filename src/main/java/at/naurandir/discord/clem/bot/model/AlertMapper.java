package at.naurandir.discord.clem.bot.model;

import at.naurandir.discord.clem.bot.service.client.dto.worldstate.AlertDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;

/**
 *
 * @author aspiel
 */
@Mapper
public interface AlertMapper {
    
    @Mappings({
        @Mapping(source="id", target="externalId"),
        @Mapping(source="mission", target="alertMission"),
        @Mapping(source="activation", target="alertStart", dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"),
        @Mapping(source= "dto", target="name", qualifiedByName = "getAlertName"),
        @Mapping(target="modifyDate", expression="java(LocalDateTime.now())"),
        @Mapping(target="startDate", expression="java(LocalDateTime.now())")
    })
    Alert alertDtoToAlert(AlertDTO dto);
    
    @Named("getAlertName")
    default String getAlertName(AlertDTO dto) {
        return dto.getId() + " - " + dto.getMission().getNode() + " - " + dto.getMission().getType();
    }
}
