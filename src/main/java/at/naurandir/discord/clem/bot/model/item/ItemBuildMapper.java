package at.naurandir.discord.clem.bot.model.item;

import at.naurandir.discord.clem.bot.service.client.dto.overframe.OverframeBuildDTO;
import at.naurandir.discord.clem.bot.service.client.dto.worldstate.WarframeDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;

/**
 *
 * @author Naurandir
 */
@Mapper
public interface ItemBuildMapper {

    @Mappings({
        @Mapping(target="modifyDate", expression="java(java.time.LocalDateTime.now())"),
        @Mapping(target="startDate", expression="java(java.time.LocalDateTime.now())"),
        
        @Mapping(target = "id", ignore = true)
    })
    ItemBuild fromDtoToBuild(OverframeBuildDTO dto);
    
    @Mappings({
        @Mapping(target="modifyDate", expression="java(java.time.LocalDateTime.now())"),   
        
        @Mapping(target = "id", ignore = true)
    })
    void updateWeapon(@MappingTarget ItemBuild toUpdateBuild, OverframeBuildDTO dto);
}
