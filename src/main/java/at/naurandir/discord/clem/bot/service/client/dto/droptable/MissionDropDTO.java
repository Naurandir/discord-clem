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
public class MissionDropDTO {
    
    private String node;
    private String missionType;
    private Boolean isEvent;
    private List<RewardDropDTO> rewardsRotationA;
    private List<RewardDropDTO> rewardsRotationB;
    private List<RewardDropDTO> rewardsRotationC;
}
