
package at.naurandir.discord.clem.bot.service.client.dto.worldstate;

import com.google.gson.annotations.SerializedName;
import java.util.Map;
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
public class WeaponAttackDTO {

    private String name;
    private Double speed;
    private Integer flight;
    
    @SerializedName("crit_chance")
    private Double critChance;
    
    @SerializedName("crit_mult")
    private Double critMultiplyer;
    
    @SerializedName("status_chance")
    private Double statusChance;
    
    @SerializedName("shot_type")
    private String shotType;
    
    @SerializedName("shot_speed")
    private Integer shotSpeed;
    
    private Map<String, Double> damage;
}
