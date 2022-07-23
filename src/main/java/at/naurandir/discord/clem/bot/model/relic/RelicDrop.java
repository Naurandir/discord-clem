package at.naurandir.discord.clem.bot.model.relic;

import at.naurandir.discord.clem.bot.model.DbEntity;
import at.naurandir.discord.clem.bot.model.enums.Rarity;
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
@Table(name = "relic_drop")
public class RelicDrop extends DbEntity {
    
    @ManyToOne
    @JoinColumn(name="relic_id")
    private Relic relic;
    
    @Column
    private String reward;
    
    @Column
    private Rarity rarity;
}
