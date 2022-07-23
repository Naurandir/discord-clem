package at.naurandir.discord.clem.bot.model.mission;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Naurandir
 */
@Getter
@Setter
@Embeddable
public class EmbeddableMission {

    @Column
    private String node;
    
    @Column
    private String type;
    
    @Column
    private String faction;
    
    @Column
    private Integer minLevelEnemy;
    
    @Column
    private Integer maxLevelEnemy;
}
