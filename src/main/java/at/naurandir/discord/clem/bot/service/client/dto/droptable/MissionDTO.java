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
public class MissionDTO {
    
    private String name;
    
    private List<RewardDTO> rewardsRotationA = new ArrayList<>();
    private List<RewardDTO> rewardsRotationB = new ArrayList<>();
    private List<RewardDTO> rewardsRotationC = new ArrayList<>();
    private List<RewardDTO> rewardsRotationGeneral = new ArrayList<>();
}
