package at.naurandir.discord.clem.bot.model.item;

import at.naurandir.discord.clem.bot.model.enums.WeaponType;
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
public interface WeaponMapper extends ItemMapper {

    @Mappings({
        @Mapping(target="modifyDate", expression="java(java.time.LocalDateTime.now())"),
        @Mapping(target="startDate", expression="java(java.time.LocalDateTime.now())"),
        
        @Mapping(source= "name", target="overframeUrlName", qualifiedByName="getOverframeUrlName"),
        @Mapping(source= "marketCost", target="marketCost", qualifiedByName = "getMarketCost"),
        @Mapping(source= "category", target="type", qualifiedByName = "getWeaponType"),
        
        @Mapping(target = "id", ignore = true)
    })
    Weapon fromDtoToWeapon(WeaponDTO weapon);
    
    @Mappings({
        @Mapping(target="modifyDate", expression="java(java.time.LocalDateTime.now())"),   
        
        @Mapping(source= "name", target="overframeUrlName", qualifiedByName="getOverframeUrlName"),
        @Mapping(source= "marketCost", target="marketCost", qualifiedByName = "getMarketCost"),
        @Mapping(source= "category", target="type", qualifiedByName = "getWeaponType"),
        
        @Mapping(target = "id", ignore = true)
    })
    void updateWeapon(@MappingTarget Weapon toUpdateWarframe, WeaponDTO dto);
    
    @Named("getWeaponType")
    default WeaponType getWeaponType(String category) {
        if (StringUtils.isEmpty(category)) {
            return null;
        }
        
        return WeaponType.valueOf(category.toUpperCase().replace("-", "_"));
    }
}
