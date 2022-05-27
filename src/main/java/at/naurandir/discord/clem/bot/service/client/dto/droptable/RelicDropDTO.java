package at.naurandir.discord.clem.bot.service.client.dto.droptable;

import at.naurandir.discord.clem.bot.model.enums.RelicState;
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
public class RelicDropDTO {
    
    private String tier;
    private String relicName;
    private RelicState state;
    private List<RewardDropDTO> rewards;
}
