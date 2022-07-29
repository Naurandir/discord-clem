package at.naurandir.discord.clem.bot.model.item;

import java.time.LocalDateTime;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
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
@Table(name = "warframe")
public class Warframe extends Item {
    
    @Column
    private Integer health;
    @Column
    private Integer shield;
    @Column
    private Integer armor;
    @Column
    private Boolean vaulted;
    @Column
    private LocalDateTime vaultDate;
    @Column
    private String sex;
    @Column
    private Integer masteryRank;
    
    @Column
    private String firstAbilityName;
    @Column(length = 510)
    private String firstAbilityDescription;
    
    @Column
    private String secondAbilityName;
    @Column(length = 510)
    private String secondAbilityDescription;
    
    @Column
    private String thirdAbilityName;
    @Column(length = 510)
    private String thirdAbilityDescription;
    
    @Column
    private String fourthAbilityName;
    @Column(length = 510)
    private String fourthAbilityDescription;
    
    @Column(length = 510)
    private String passiveDescription;
    
    @OneToMany(mappedBy="warframe", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<ItemBuild> builds;
}
