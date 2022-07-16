package at.naurandir.discord.clem.bot.model.cycle;

import at.naurandir.discord.clem.bot.service.client.dto.worldstate.CambionCycleDTO;
import at.naurandir.discord.clem.bot.service.client.dto.worldstate.CetusCycleDTO;
import at.naurandir.discord.clem.bot.service.client.dto.worldstate.EarthCycleDTO;
import at.naurandir.discord.clem.bot.service.client.dto.worldstate.VallisCycleDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;

/**
 *
 * @author Naurandir
 */
@Mapper
public interface CycleMapper {
    
    @Mappings({
        @Mapping(source="expiry", target="expiry", dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"),
        @Mapping(target="modifyDate", expression="java(LocalDateTime.now())"),
        @Mapping(target="startDate", expression="java(LocalDateTime.now())"),
        @Mapping(target="cycleType", expression = "java(getEarthType())"),
        @Mapping(target="name", expression = "java(\"Earth Cycle\")"),
        
        @Mapping(source= "dto", target="currentState", qualifiedByName = "getEarthState"),
        
        @Mapping(target = "id", ignore = true)
    })
    Cycle fromDtoToCycle(EarthCycleDTO dto);
    
    @Mappings({
        @Mapping(source="expiry", target="expiry", dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"),
        @Mapping(target="modifyDate", expression="java(LocalDateTime.now())"),
        @Mapping(target="startDate", expression="java(LocalDateTime.now())"),
        @Mapping(target="cycleType", expression = "java(getCetusType())"),
        @Mapping(target="name", expression = "java(\"Cetus Cycle\")"),
        
        @Mapping(source= "dto", target="currentState", qualifiedByName = "getCetusState"),
        
        @Mapping(target = "id", ignore = true)
    })
    Cycle fromDtoToCycle(CetusCycleDTO dto);
    
    @Mappings({
        @Mapping(source="expiry", target="expiry", dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"),
        @Mapping(target="modifyDate", expression="java(LocalDateTime.now())"),
        @Mapping(target="startDate", expression="java(LocalDateTime.now())"),
        @Mapping(target="cycleType", expression = "java(getVallisType())"),
        @Mapping(target="name", expression = "java(\"Vallis Cycle\")"),
        
        @Mapping(source= "dto", target="currentState", qualifiedByName = "getVallisState"),
        
        @Mapping(target = "id", ignore = true)
    })
    Cycle fromDtoToCycle(VallisCycleDTO dto);
    
    @Mappings({
        @Mapping(source="expiry", target="expiry", dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"),
        @Mapping(target="modifyDate", expression="java(LocalDateTime.now())"),
        @Mapping(target="startDate", expression="java(LocalDateTime.now())"),
        @Mapping(target="cycleType", expression = "java(getCambionType())"),
        @Mapping(target="name", expression = "java(\"Cambion Cycle\")"),
        
        @Mapping(source= "dto", target="currentState", qualifiedByName = "getCambionState"),
        
        @Mapping(target = "id", ignore = true)
    })
    Cycle fromDtoToCycle(CambionCycleDTO dto);
    
    @Named("getEarthState")
    default String getEarthState(EarthCycleDTO dto) {
        return dto.getIsDay() ? "Day" : "Night";
    }
    
    @Named("getEarthType")
    default CycleType getEarthType() {
        return CycleType.EARTH;
    }
    
    @Named("getCetusState")
    default String getEarthState(CetusCycleDTO dto) {
        return dto.getIsDay() ? "Day" : "Night";
    }
    
    @Named("getCetusType")
    default CycleType getCetusType() {
        return CycleType.CETUS;
    }
    
    @Named("getVallisState")
    default String getVallisState(VallisCycleDTO dto) {
        return "warm".equals(dto.getState()) ? "Warm" : "Cold";
    }
    
    @Named("getVallisType")
    default CycleType getVallisType() {
        return CycleType.VALLIS;
    }
    
    @Named("getCambionState")
    default String getEarthState(CambionCycleDTO dto) {
        return "fass".equals(dto.getActive()) ? "Fass" : "Vome";
    }
    
    @Named("getCambionType")
    default CycleType getCambionType() {
        return CycleType.CAMBION;
    }
}
