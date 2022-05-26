package at.naurandir.discord.clem.bot.service.client.dto.droptable;

import java.util.List;
import java.util.Map;
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
public class MissionDropDTO {

    private String gameMode;
    private Boolean isEvent;
    private Map<String, List<RewardDropDTO>> rewards;
    
}
