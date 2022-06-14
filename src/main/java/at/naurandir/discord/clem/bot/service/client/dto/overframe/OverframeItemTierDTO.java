package at.naurandir.discord.clem.bot.service.client.dto.overframe;

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
public class OverframeItemTierDTO {
    
    @SerializedName("item_id")
    private long itemId;
    
    @SerializedName("avarageScore")
    private long avarageScore;
}
