package at.naurandir.discord.clem.bot.model.market;

import at.naurandir.discord.clem.bot.model.DbEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
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
@Table(name = "market_item")
public class MarketItem extends DbEntity {
    
    @Column
    private String urlName;
    
    @Column
    private String thumb;
}
