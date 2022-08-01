package at.naurandir.discord.clem.bot.service.client.dto.overframe;

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
public class OverframeBuildDTO {
    
    private String title;
    private String url;
    private Integer votes;
    private String formasUsed;
    private String guideLength;
}
