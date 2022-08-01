package at.naurandir.discord.clem.bot.model.item;

import at.naurandir.discord.clem.bot.model.DbEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
@Table(name = "item_build")
public class ItemBuild extends DbEntity {
    
    @ManyToOne
    @JoinColumn(name="weapon_id")
    private Weapon weapon;
    
    @ManyToOne
    @JoinColumn(name="warframe_id")
    private Warframe warframe;

    @Column
    private String title;
    
    @Column
    private String url;
    
    @Column
    private Integer votes;
    
    @Column
    private String formasUsed;
    
    @Column
    private String guideLength;
}
