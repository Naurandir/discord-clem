package at.naurandir.discord.clem.bot.model.item;

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
public interface WarframeMapper extends ItemMapper {

    @Mappings({
        @Mapping(target="modifyDate", expression="java(java.time.LocalDateTime.now())"),
        @Mapping(target="startDate", expression="java(java.time.LocalDateTime.now())"),
        
        @Mapping(target = "firstAbilityName", expression = "java(warframes.getAbilities().get(0).getName())"),
        @Mapping(target = "secondAbilityName", expression = "java(warframes.getAbilities().get(1).getName())"),
        @Mapping(target = "thirdAbilityName", expression = "java(warframes.getAbilities().get(2).getName())"),
        @Mapping(target = "fourthAbilityName", expression = "java(warframes.getAbilities().get(3).getName())"),
        
        @Mapping(target = "firstAbilityDescription", expression = "java(warframes.getAbilities().get(0).getDescription())"),
        @Mapping(target = "secondAbilityDescription", expression = "java(warframes.getAbilities().get(1).getDescription())"),
        @Mapping(target = "thirdAbilityDescription", expression = "java(warframes.getAbilities().get(2).getDescription())"),
        @Mapping(target = "fourthAbilityDescription", expression = "java(warframes.getAbilities().get(3).getDescription())"),
        
        @Mapping(source= "name", target="overframeUrlName", qualifiedByName="getOverframeUrlName"),
        @Mapping(source= "marketCost", target="marketCost", qualifiedByName = "getMarketCost"),
        
        @Mapping(target = "id", ignore = true)
    })
    Warframe fromDtoToWarframe(WarframeDTO warframes);
    
    @Mappings({
        @Mapping(target="modifyDate", expression="java(java.time.LocalDateTime.now())"),  
        
        @Mapping(source= "name", target="overframeUrlName", qualifiedByName="getOverframeUrlName"),
        @Mapping(source= "marketCost", target="marketCost", qualifiedByName = "getMarketCost"),
        
        @Mapping(target = "id", ignore = true)
    })
    void updateWarframe(@MappingTarget Warframe toUpdateWarframe, WarframeDTO dto);
}
