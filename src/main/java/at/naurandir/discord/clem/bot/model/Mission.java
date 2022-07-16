package at.naurandir.discord.clem.bot.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author aspiel
 */
@Getter
@Setter
@Embeddable
public class Mission {
    
    @Column(length = 50, nullable = false)
    private String node;
    
    @Column(length = 30, nullable = false)
    private String faction;
    
    @Column(nullable = false)
    private Integer maxEnemyLevel;
    
    @Column(nullable = false)
    private Integer minEnemyLevel;
    
    @Column(length = 30, nullable = false)
    private String type;
}
