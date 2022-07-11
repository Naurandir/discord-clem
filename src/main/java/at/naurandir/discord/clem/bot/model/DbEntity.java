package at.naurandir.discord.clem.bot.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
public abstract class DbEntity implements Serializable {
    
    @Id 
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    
    @Column
    private String externalId;
    
    @Column(length = 100)
    private String name;
    
    @Column
    private LocalDateTime startDate;
    
    @Column
    private LocalDateTime endDate;
    
    @Column
    private LocalDateTime modifyDate;
}
