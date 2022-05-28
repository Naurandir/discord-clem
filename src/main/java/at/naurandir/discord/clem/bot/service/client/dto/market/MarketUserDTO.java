package at.naurandir.discord.clem.bot.service.client.dto.market;

import at.naurandir.discord.clem.bot.model.enums.OnlineStatus;
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
public class MarketUserDTO {
    
    private String id;
    
    @SerializedName("ingame_name")
    private String name;
    
    private OnlineStatus status;
    
    private Integer reputation;
}
