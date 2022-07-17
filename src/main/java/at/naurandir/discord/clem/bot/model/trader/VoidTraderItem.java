package at.naurandir.discord.clem.bot.model.trader;

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
@Table(name = "void_trader_item")
public class VoidTraderItem extends DbEntity {
    
    @ManyToOne
    @JoinColumn(name="void_trader_id")
    private VoidTrader voidTrader;
    
    @Column
    private String item;
    
    @Column
    private Integer ducats;
    
    @Column
    private Long credits;
}
