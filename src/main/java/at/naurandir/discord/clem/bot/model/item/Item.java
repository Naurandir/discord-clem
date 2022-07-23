package at.naurandir.discord.clem.bot.model.item;

import at.naurandir.discord.clem.bot.model.DbEntity;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Naurandir
 */
@Getter
@Setter
@MappedSuperclass
public class Item extends DbEntity {
    
    @Column
    private String uniqueName;
    
    @Column
    private Boolean tradable;
    
    @Column
    private Double marketCost;
}
