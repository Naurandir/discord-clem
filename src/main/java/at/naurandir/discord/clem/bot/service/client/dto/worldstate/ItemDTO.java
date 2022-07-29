package at.naurandir.discord.clem.bot.service.client.dto.worldstate;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author Naurandir
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class ItemDTO {

    private String name;
    private String uniqueName;
    private String wikiaUrl;
    private String wikiaThumbnail;
    private Boolean tradable;
    private String marketCost;
    private String description;
    private String category;
    
}
