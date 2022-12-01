package at.naurandir.discord.clem.bot.model.news;

import at.naurandir.discord.clem.bot.service.client.dto.worldstate.NewsDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

/**
 *
 * @author Naurandir
 */
@Mapper
public interface NewsMapper {

    @Mappings({
        @Mapping(source="id", target="externalId"),
        @Mapping(source="date", target="startDate", dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"),
        @Mapping(target="modifyDate", expression="java(LocalDateTime.now())"),
        
        @Mapping(target = "id", ignore = true)
    })
    public News newsDtoToNews(NewsDTO newsDto);
    
}
