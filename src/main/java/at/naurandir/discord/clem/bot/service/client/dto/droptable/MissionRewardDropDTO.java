package at.naurandir.discord.clem.bot.service.client.dto.droptable;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
@NoArgsConstructor
@AllArgsConstructor
public class MissionRewardDropDTO {
    
    @SerializedName("A")
    private List<RewardDropDTO> a;
    
    @SerializedName("B")
    private List<RewardDropDTO> b;
    
    @SerializedName("C")
    private List<RewardDropDTO> c;
    
    private List<RewardDropDTO> generalRewards;
}
