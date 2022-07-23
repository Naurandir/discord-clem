package at.naurandir.discord.clem.bot.model.relic;

import at.naurandir.discord.clem.bot.model.DbEntity;
import at.naurandir.discord.clem.bot.model.enums.RelicTier;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
@Table(name = "relic")
public class Relic extends DbEntity {
    
    @Enumerated(EnumType.STRING)
    private RelicTier tier;

    @OneToMany(mappedBy="relic", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<RelicDrop> drops;
}
