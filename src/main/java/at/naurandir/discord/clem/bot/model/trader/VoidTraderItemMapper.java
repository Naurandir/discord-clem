package at.naurandir.discord.clem.bot.model.trader;

import at.naurandir.discord.clem.bot.service.client.dto.worldstate.VoidTraderDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

/**
 *
 * @author Naurandir
 */
@Mapper
public interface VoidTraderItemMapper {

    @Mappings({
        @Mapping(source="item", target="name"),
        @Mapping(source="item", target="item"),
        @Mapping(target="modifyDate", expression="java(java.time.LocalDateTime.now())"),
        @Mapping(target="startDate", expression="java(java.time.LocalDateTime.now())"),
        
        @Mapping(target = "id", ignore = true)
    })
    VoidTraderItem voidTraderItemDtoToVoidTraderItem(VoidTraderDTO.VoidTraderInventoryDTO itemDto);
}
