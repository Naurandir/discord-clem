package at.naurandir.discord.clem.bot.service.client.dto.market;

import at.naurandir.discord.clem.bot.model.enums.OrderType;
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
public class MarketOrderDTO {
    
    private Integer platinum;
    private Integer quality;
    
    @SerializedName("order_type")
    private OrderType orderType;
    
    private MarketUserDTO user;
}
