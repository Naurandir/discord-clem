package at.naurandir.discord.clem.bot.model.mission;

import at.naurandir.discord.clem.bot.model.DbEntity;
import static at.naurandir.discord.clem.bot.model.enums.Rotation.*;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
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
@Table(name = "mission")
public class Mission extends DbEntity {
    
    @Column
    private String node;
    
    @Column
    private String faction;
    
    @Column
    private String type;
    
    @Column
    private Integer minLevelEnemy;
    
    @Column
    private Integer maxLevelEnemy;
    
    @OneToMany(mappedBy="mission", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<MissionReward> allRewards;
    
    public Set<MissionReward> getGeneralRewards() {
        return allRewards.stream().filter(reward -> reward.getRotation() == GENERAL)
                .collect(Collectors.toSet());
    }
    
    public Set<MissionReward> getRotationARewards() {
        return allRewards.stream().filter(reward -> reward.getRotation() == A)
                .collect(Collectors.toSet());
    }
    
    public Set<MissionReward> getRotationBRewards() {
        return allRewards.stream().filter(reward -> reward.getRotation() == B)
                .collect(Collectors.toSet());
    }
    
    public Set<MissionReward> getRotationCRewards() {
        return allRewards.stream().filter(reward -> reward.getRotation() == C)
                .collect(Collectors.toSet());
    }
}
