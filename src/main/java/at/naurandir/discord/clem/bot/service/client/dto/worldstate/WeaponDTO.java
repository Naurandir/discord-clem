package at.naurandir.discord.clem.bot.service.client.dto.worldstate;

import java.util.List;
import java.util.Optional;
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
@EqualsAndHashCode(callSuper = true)
public class WeaponDTO extends ItemDTO {
    
    private String accuracy;
    private Integer ammo;
    private Double criticalChance;
    private Double criticalMultiplier;
    private Double fireRate;
    private Integer magazineSize;
    private Double multishot;
    private String noise;
    private Double procChance;
    private Double reloadTime;
    private Double totalDamage;
    private String trigger;
    private List<WeaponAttackDTO> attacks;
}
