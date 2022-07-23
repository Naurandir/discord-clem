package at.naurandir.discord.clem.bot.model.item;

import at.naurandir.discord.clem.bot.service.client.dto.worldstate.WarframeDTO;
import at.naurandir.discord.clem.bot.service.client.dto.worldstate.WeaponDTO;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.Named;

/**
 *
 * @author Naurandir
 */
@Mapper
public interface ItemMapper {

    @Mappings({
        @Mapping(target="modifyDate", expression="java(java.time.LocalDateTime.now())"),
        @Mapping(target="startDate", expression="java(java.time.LocalDateTime.now())"),
        
        @Mapping(source= "marketCost", target="marketCost", qualifiedByName = "getMarketCost"),
        
        @Mapping(target = "id", ignore = true)
    })
    Warframe fromDtoToWarframe(WarframeDTO warframes);
    
    @Mappings({
        @Mapping(target="modifyDate", expression="java(java.time.LocalDateTime.now())"),        
        @Mapping(target = "id", ignore = true)
    })
    void updateWarframe(@MappingTarget Warframe toUpdateWarframe, WarframeDTO dto);
    
    @Mappings({
        @Mapping(target="modifyDate", expression="java(java.time.LocalDateTime.now())"),
        @Mapping(target="startDate", expression="java(java.time.LocalDateTime.now())"),
        
        @Mapping(source= "marketCost", target="marketCost", qualifiedByName = "getMarketCost"),
        
        @Mapping(target = "id", ignore = true)
    })
    Weapon fromDtoToWeapon(WeaponDTO weapon);
    
    @Mappings({
        @Mapping(target="modifyDate", expression="java(java.time.LocalDateTime.now())"),        
        @Mapping(target = "id", ignore = true)
    })
    void updateWeapon(@MappingTarget Weapon toUpdateWarframe, WeaponDTO dto);
    
    @Named("getMarketCost")
    default Double getMarketCost(String marketCost) {
        if (StringUtils.isEmpty(marketCost)) {
            return null;
        }
        
        return Double.parseDouble(marketCost);
    }
}
