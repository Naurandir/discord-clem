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
public class MarketLichWeaponDTO extends MarketItemDTO {
    
    private String element;
    private Integer damage;
    private String type;
    private String quirk;
    
    @SerializedName("having_ephemera")
    private Boolean hasEphemera;
}
