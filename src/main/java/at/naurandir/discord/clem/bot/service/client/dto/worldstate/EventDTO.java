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
public class EventDTO {
    
    private String id;
    private String activation;
    private String expiry;
    
    private Boolean active;
    
    private String description;
    private String tooltip;
    
    private String victimNode;
    
    private Integer health;
    
}
