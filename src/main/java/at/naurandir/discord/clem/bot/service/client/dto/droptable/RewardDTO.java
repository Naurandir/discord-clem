package at.naurandir.discord.clem.bot.service.client.dto.droptable;

import at.naurandir.discord.clem.bot.model.enums.Rarity;
import java.util.ArrayList;
import java.util.List;
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
public class RewardDTO {
    
    private String reward;
    private Rarity rarity;
    
    private List<Double> chance = new ArrayList<>();
}
