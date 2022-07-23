package at.naurandir.discord.clem.bot.model.item;

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
@Table(name = "warframe")
public class Warframe extends Item {
    
}
