package at.naurandir.discord.clem.bot.model.mission;

import at.naurandir.discord.clem.bot.model.enums.Rotation;
import at.naurandir.discord.clem.bot.service.client.dto.droptable.MissionDropTableDTO;
import at.naurandir.discord.clem.bot.service.client.dto.droptable.RewardDTO;
import at.naurandir.discord.clem.bot.service.client.dto.worldstate.EmbeddableMissionDTO;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Naurandir
 */
@Mapper
public abstract class MissionMapper {
    
    @Autowired
    private MissionRewardMapper missionRewardMapper;
    
    @Mappings({
        @Mapping(target="modifyDate", expression="java(java.time.LocalDateTime.now())"),
        @Mapping(target="startDate", expression="java(java.time.LocalDateTime.now())"),
        
        @Mapping(source= "dto", target="allRewards", qualifiedByName = "getRewards"),
        
        @Mapping(target = "id", ignore = true)
    })
    public abstract Mission missionDtoToMission(MissionDropTableDTO dto);
    
    @Mappings({
        @Mapping(target="modifyDate", expression="java(java.time.LocalDateTime.now())"),
        
        @Mapping(source= "dto", target="allRewards", qualifiedByName = "getRewards"),
        
        @Mapping(target = "id", ignore = true)
    })
    public abstract void updateMission(@MappingTarget Mission missionToUpdate, MissionDropTableDTO dto);
    
    @Named("getRewards")
    public Set<MissionReward> getRewards(MissionDropTableDTO dto) {
        Set<MissionReward> rewards = new HashSet<>();
        
        dto.getRewardsRotationGeneral().forEach(reward -> 
                rewards.add(missionRewardMapper.fromDtoToMissionReward(reward, Rotation.GENERAL)));
        
        dto.getRewardsRotationA().forEach(reward -> 
                rewards.add(missionRewardMapper.fromDtoToMissionReward(reward, Rotation.A)));
        
        dto.getRewardsRotationB().forEach(reward -> 
                rewards.add(missionRewardMapper.fromDtoToMissionReward(reward, Rotation.B)));
        
        dto.getRewardsRotationC().forEach(reward -> 
                rewards.add(missionRewardMapper.fromDtoToMissionReward(reward, Rotation.C)));
        
        return rewards;
    }
    
    @Mappings({
        @Mapping(source= "node", target="node"),
        @Mapping(source= "missionType", target="type"),
        @Mapping(source= "faction", target="faction"),
        
        @Mapping(target="minLevelEnemy", expression="java(0)"),
        @Mapping(target="maxLevelEnemy", expression="java(999)"),
    })
    public abstract EmbeddableMission toEmbeddableMission(String node, String missionType, String faction);
    
    public abstract EmbeddableMission missionDtoToEmbeddableMission(EmbeddableMissionDTO dto);
}
