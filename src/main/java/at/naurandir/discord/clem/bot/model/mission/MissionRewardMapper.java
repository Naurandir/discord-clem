package at.naurandir.discord.clem.bot.model.mission;

import at.naurandir.discord.clem.bot.model.enums.Rotation;
import at.naurandir.discord.clem.bot.service.client.dto.droptable.RewardDTO;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;

/**
 *
 * @author Naurandir
 */
@Mapper
public interface MissionRewardMapper {

    @Mappings({
        @Mapping(source="dto.reward", target="name"),
        @Mapping(source="rotation", target="rotation"),
        
        @Mapping(target="modifyDate", expression="java(java.time.LocalDateTime.now())"),
        @Mapping(target="startDate", expression="java(java.time.LocalDateTime.now())"),
        
        @Mapping(source= "dto.chance", target="chance", qualifiedByName = "getChance"),
        
        @Mapping(target = "id", ignore = true)
    })
    MissionReward fromDtoToMissionReward(RewardDTO dto, Rotation rotation);
    
    @Named("getChance")
    default Double getChance(List<Double> chances) {
        return chances.get(0);
    }
}
