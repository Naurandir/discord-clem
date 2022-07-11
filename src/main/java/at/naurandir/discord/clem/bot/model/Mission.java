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
    
    @Column(length = 50)
    private String node;
    
    @Column(length = 30)
    private String faction;
    
    @Column
    private Integer maxEnemyLevel;
    
    @Column
    private Integer minEnemyLevel;
    
    @Column(length = 30)
    private String type;
    
    @Column(length = 255)
    private String description;
}
