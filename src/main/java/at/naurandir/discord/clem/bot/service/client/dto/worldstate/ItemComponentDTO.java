package at.naurandir.discord.clem.bot.service.client.dto.worldstate;

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
public class ItemComponentDTO {
    private String name;
    private Integer itemCount;
}
