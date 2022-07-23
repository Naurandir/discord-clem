package at.naurandir.discord.clem.bot.model.alert;

import at.naurandir.discord.clem.bot.model.DbEntity;
import at.naurandir.discord.clem.bot.model.mission.EmbeddableMission;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Embedded;
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
@Table(name = "alert")
public class Alert extends DbEntity {
    
    @Embedded
    private EmbeddableMission alertMission;
    
    @Column(nullable = false)
    private LocalDateTime activation;
    
    @Column(nullable = false)
    private LocalDateTime expiry;
    
    @Column(nullable = false)
    private String rewards;
}
