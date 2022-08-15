package at.naurandir.discord.clem.bot.model.item;

import at.naurandir.discord.clem.bot.service.client.dto.market.MarketLichWeaponDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;

/**
 *
 * @author Naurandir
 */
@Mapper
public interface MarketLichWeaponMapper {
    
    @Mappings({
        @Mapping(target="modifyDate", expression="java(java.time.LocalDateTime.now())"),
        @Mapping(target="startDate", expression="java(java.time.LocalDateTime.now())"),
        
        @Mapping(source= "id", target="externalId"),
        @Mapping(source= "itemName", target="name"),
        
        @Mapping(target = "id", ignore = true)
    })
    MarketLichWeapon fromDtoToLichWeapon(MarketLichWeaponDTO marketItem);
    
    @Mappings({
        @Mapping(target="modifyDate", expression="java(java.time.LocalDateTime.now())"),   
        
        @Mapping(source= "id", target="externalId"),
        @Mapping(source= "itemName", target="name"),
        
        @Mapping(target = "id", ignore = true)
    })
    void updateLichWeapon(@MappingTarget MarketLichWeapon toUpdateWeapon, MarketLichWeaponDTO dto);
}
