package at.naurandir.discord.clem.bot.model.event;

import at.naurandir.discord.clem.bot.model.DbEntity;
import java.time.LocalDateTime;
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
@Table(name = "event")
public class Event extends DbEntity {
    
    @Column
    private String externalId;
    
    @Column
    private String tooltip;
    
    @Column
    private LocalDateTime eventStart;
    
    @Column
    private LocalDateTime eventEnd;
}
