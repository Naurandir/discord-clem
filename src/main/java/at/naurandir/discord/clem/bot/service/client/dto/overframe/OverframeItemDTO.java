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
public class OverframeItemDTO {
    
    private long id;
    private String name;
    private String pictureUrl;
    
    private List<OverframeBuildDTO> builds;
}
