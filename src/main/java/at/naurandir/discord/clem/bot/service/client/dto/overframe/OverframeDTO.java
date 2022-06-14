package at.naurandir.discord.clem.bot.service.client.dto.overframe;

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
public class OverframeDTO {
    
    private List<OverframeItemDTO> warframes;
    private List<OverframeItemDTO> primaryWeapons;
    private List<OverframeItemDTO> secondaryWeapons;
    private List<OverframeItemDTO> meeleWeapons;
}
