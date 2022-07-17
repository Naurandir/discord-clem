package at.naurandir.discord.clem.bot.model.fissure;

import at.naurandir.discord.clem.bot.model.DbEntity;
import at.naurandir.discord.clem.bot.model.Mission;
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
@Table(name = "void_fissure")
public class VoidFissure extends DbEntity {

    @Embedded
    private Mission fissureMission;
    
    @Column(nullable = false)
    private LocalDateTime activation;
    
    @Column(nullable = false)
    private LocalDateTime expiry;
    
    @Column(nullable = false)
    private String tier;
    
    @Column(nullable = false)
    Boolean isStorm;
}
