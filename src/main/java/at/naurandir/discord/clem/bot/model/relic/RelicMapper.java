package at.naurandir.discord.clem.bot.model.relic;

import at.naurandir.discord.clem.bot.model.enums.RelicTier;
import at.naurandir.discord.clem.bot.service.client.dto.droptable.RelicDTO;
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
public interface RelicMapper {

    @Mappings({
        @Mapping(target="modifyDate", expression="java(java.time.LocalDateTime.now())"),
        @Mapping(target="startDate", expression="java(java.time.LocalDateTime.now())"),
        
        @Mapping(source= "tier", target="tier", qualifiedByName = "getTier"),
        
        @Mapping(target = "id", ignore = true)
    })
    Relic fromDtoToRelic(RelicDTO relic);
    
    
    @Mappings({
        @Mapping(target="modifyDate", expression="java(java.time.LocalDateTime.now())"),  
        
        @Mapping(source= "tier", target="tier", qualifiedByName = "getTier"),
        
        @Mapping(target = "id", ignore = true)
    })
    void updateRelic(@MappingTarget Relic toUpdateRelic, RelicDTO dto);
    
    @Named(value = "getTier")
    default RelicTier getTier(String tier) {
        return RelicTier.parse(tier);
    }
}
