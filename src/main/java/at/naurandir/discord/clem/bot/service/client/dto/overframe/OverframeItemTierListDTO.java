package at.naurandir.discord.clem.bot.service.client.dto.overframe;

import com.google.gson.annotations.SerializedName;
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
public class OverframeItemTierListDTO {
    
    @SerializedName("votes")
    private List<OverframeItemTierDTO> tierList;
}
