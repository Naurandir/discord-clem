package at.naurandir.discord.clem.bot.model.mission;

import at.naurandir.discord.clem.bot.model.DbEntity;
import at.naurandir.discord.clem.bot.model.enums.Rarity;
import at.naurandir.discord.clem.bot.model.enums.Rotation;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
@Table(name = "mission_reward")
public class MissionReward extends DbEntity {

    @ManyToOne
    @JoinColumn(name="mission_id")
    private Mission mission;
    
    private Rotation rotation;
    private Rarity rarity;
    private Double chance;
    
}
