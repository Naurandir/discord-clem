package at.naurandir.discord.clem.bot.model.trader;

import at.naurandir.discord.clem.bot.service.client.dto.worldstate.VoidTraderDTO;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
public abstract class VoidTraderMapper {
    
    @Autowired
    private VoidTraderItemMapper voidTraderItemMapper;

    @Mappings({
        @Mapping(source="id", target="externalId"),
        @Mapping(source="activation", target="activation", dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"),
        @Mapping(source="expiry", target="expiry", dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"),
        @Mapping(target="modifyDate", expression="java(LocalDateTime.now())"),
        @Mapping(target="startDate", expression="java(LocalDateTime.now())"),
        
        @Mapping(source= "inventory", target="inventory", qualifiedByName = "getInventory"),
        
        @Mapping(target = "id", ignore = true)
    })
    public abstract VoidTrader voidTraderDtoToVoidTrader(VoidTraderDTO dto);
    
    @Named("getInventory")
    public Set<VoidTraderItem> voidTraderItemDtoToVoidTraderItem(List<VoidTraderDTO.VoidTraderInventoryDTO> inventoryDto) {
        Set<VoidTraderItem> inventory = new HashSet<>();
        
        inventoryDto.forEach(dto -> inventory.add(voidTraderItemMapper.voidTraderItemDtoToVoidTraderItem(dto)));
        
        return inventory;
    }
    
}
