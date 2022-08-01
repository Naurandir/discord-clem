package at.naurandir.discord.clem.bot.model.market;

import at.naurandir.discord.clem.bot.service.client.dto.market.MarketItemDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;

/**
 *
 * @author Naurandir
 */
@Mapper
public interface MarketItemMapper {
    
    @Mappings({
        @Mapping(target="modifyDate", expression="java(java.time.LocalDateTime.now())"),
        @Mapping(target="startDate", expression="java(java.time.LocalDateTime.now())"),
        
        @Mapping(source= "itemName", target="name"),
        @Mapping(source= "id", target="externalId"),
        
        @Mapping(target = "id", ignore = true)
    })
    MarketItem fromDtoToMarketItem(MarketItemDTO dto);
    
    @Mappings({
        @Mapping(target="modifyDate", expression="java(java.time.LocalDateTime.now())"),
        
        @Mapping(source= "itemName", target="name"),
        @Mapping(source= "id", target="externalId"),
        
        @Mapping(target = "id", ignore = true)
    })
    void updateMarketItem(@MappingTarget MarketItem toUpdateMarketItem, MarketItemDTO dto);
}
