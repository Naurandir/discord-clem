package at.naurandir.discord.clem.bot.model.cycle;

import at.naurandir.discord.clem.bot.model.DbEntity;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
@Table(name = "cycle")
public class Cycle extends DbEntity {

    @Column(length = 50, nullable = false)
    private String currentState;
    
    @Column(nullable = false)
    private LocalDateTime expiry;
    
    @Enumerated(EnumType.STRING)
    private CycleType cycleType;
}
