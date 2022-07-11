package at.naurandir.discord.clem.bot.model;

import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
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
    
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "alert_id")
    private Mission alertMission;
    
    @Column
    private LocalDateTime activation;
    
    @Column
    private LocalDateTime expiry;
    
    @Column
    private Boolean notifiedActivation;
    
    @Column
    private Boolean notifiedExpiry;
    
    @Column
    @Convert(converter = RewardTypesConverter.class)
    private List<String> rewardTypes;
}
