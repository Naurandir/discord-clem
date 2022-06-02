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
public class MarketLichAuctionDTO {
    
    private String id;
    private String note;
    private Boolean visible;
    private Boolean closed;
    private MarketLichWeaponDTO item;
    private MarketUserDTO owner;
    
    @SerializedName("buyout_price")
    private Integer buyoutPrice;
    
    @SerializedName("starting_price")
    private Integer startingPrice;
    
    @SerializedName("top_bid")
    private Integer topBid;
}
