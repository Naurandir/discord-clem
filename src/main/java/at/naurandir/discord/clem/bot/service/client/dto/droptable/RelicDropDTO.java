package at.naurandir.discord.clem.bot.service.client.dto.droptable;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Naurandir
 */
@Getter
@Setter
public class RelicDropDTO {
    
    String tier;
    String relicName;
    String state;
    private List<RewardDropDTO> rewards;
}
