package at.naurandir.discord.clem.bot.service.client.dto.market;

import java.util.List;
import java.util.Map;
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
public class MarketItemsDTO {
    
    private Map<String, List<MarketItemDTO>> payload;
}
