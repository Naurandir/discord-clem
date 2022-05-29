package at.naurandir.discord.clem.bot.service.client.dto.droptable;

import java.util.ArrayList;
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
public class RelicDTO {
    
    private String name;
    private String tier;
    
    private List<RewardDTO> drops = new ArrayList<>();
}
