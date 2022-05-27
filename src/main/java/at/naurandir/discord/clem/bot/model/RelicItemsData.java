package at.naurandir.discord.clem.bot.model;

import at.naurandir.discord.clem.bot.model.enums.RelicState;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Naurandir
 */
@Getter
@Setter
public class RelicItemsData {
    private String relicName;
    private String itemName;
    private Map<RelicState, Double> chances;
}
