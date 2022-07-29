package at.naurandir.discord.clem.bot.model.item;

import at.naurandir.discord.clem.bot.model.enums.WeaponType;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Naurandir
 */
@Getter
@Setter
@Entity
@Table(name = "weapon")
public class Weapon extends Item {
    
    @Enumerated(EnumType.STRING)
    private WeaponType type;
    
    @Column
    private Double accuracy;
    
    @Column
    private Integer ammo;
    
    @Column
    private Double criticalChance;
    
    @Column
    private Double criticalMultiplier;
    
    @Column
    private Double fireRate;
    
    @Column
    private Integer magazineSize;
    
    @Column
    private Double multishot;
    
    @Column
    private String noise;
    
    @Column
    private Double procChance;
    
    @Column
    private Double reloadTime;
    
    @Column
    private Double totalDamage;
    
    @Column
    private String triggerType;
    
    @OneToMany(mappedBy="weapon", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<ItemBuild> builds;
}
