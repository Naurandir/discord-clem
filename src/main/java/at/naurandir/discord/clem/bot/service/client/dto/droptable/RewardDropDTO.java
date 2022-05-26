package at.naurandir.discord.clem.bot.service.client.dto.droptable;

import at.naurandir.discord.clem.bot.model.enums.Rarity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author Naurandir
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class RewardDropDTO {
    
    private String itemName;
    private Rarity rarity;
    private Double chance;
}
