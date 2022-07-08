package at.naurandir.discord.clem.bot.model;

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
@Table(name = "alert")
public class Alert extends DbEntity {

    @Column
    private String externalId;
    
    @Column
    private LocalDateTime alertStart;
}
