package at.naurandir.discord.clem.bot.service.client.dto.droptable;

import at.naurandir.discord.clem.bot.model.enums.Rarity;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Naurandir
 */
@Getter
@Setter
public class RewardDropDTO {
    
    private String itemName;
    private Rarity rarity;
    private Double chance;
}
