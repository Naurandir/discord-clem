package at.naurandir.discord.clem.bot.service.client.dto.market;

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
public class MarketItemDTO {
    
    private String id;
    private String thumb;
    
    @SerializedName("url_name")
    private String urlName;
    
    @SerializedName("item_name")
    private String itemName;
}
