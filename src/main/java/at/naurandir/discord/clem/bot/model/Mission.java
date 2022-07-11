package at.naurandir.discord.clem.bot.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author aspiel
 */
@Getter
@Setter
@Entity
@Table(name = "mission")
public class Mission extends DbEntity {
    
    @Column
    private String node;
    
    @Column
    private String faction;
    
    @Column
    private Integer maxEnemyLevel;
    
    @Column
    private Integer minEnemyLevel;
    
    @Column
    private String type;
    
    @Column
    private String description;
}
