package at.naurandir.discord.clem.bot.service.client.dto.worldstate;

import java.util.List;
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
public class WarframeDTO extends ItemDTO {

    private Integer armor;
    private Integer health;
    private Integer shield;
    private Integer masteryReq;
    private String passiveDescription;
    private Integer power;
    private String sex;
    private Double sprint;
    private Double sprintSpeed;
    private Integer stamina;
    private List<WarframeAbilitiesDTO> abilities;
    
}
