package at.naurandir.discord.clem.bot.model;

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
@Table(name = "overframe_item")
public class OverframeItem extends DbEntity {
    
}
