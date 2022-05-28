package at.naurandir.discord.clem.bot.service.client.dto.market;

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
public class MarketOrdersResultDTO {
    
    private MarketOrdersDTO payload;
}
