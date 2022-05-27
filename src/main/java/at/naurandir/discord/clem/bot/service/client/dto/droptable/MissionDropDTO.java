package at.naurandir.discord.clem.bot.service.client.dto.droptable;

import com.google.gson.annotations.SerializedName;
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
    private MissionRewardDropDTO missionRewards;
    
    @SerializedName("rewards")
    private Object rewardsObject;
}
