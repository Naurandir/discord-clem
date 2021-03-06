package at.naurandir.discord.clem.bot.service.client.dto.worldstate;

import com.google.gson.annotations.SerializedName;
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
public class EmbeddableMissionDTO {
    
    private String node;
    private String faction;
    private Integer minEnemyLevel;
    private Integer maxEnemyLevel;
    private String type;
    private String enemySpec;
    private String levelOverride;
    private String description;
    
    @SerializedName("reward")
    private RewardDTO rewardDTO;
    
}
