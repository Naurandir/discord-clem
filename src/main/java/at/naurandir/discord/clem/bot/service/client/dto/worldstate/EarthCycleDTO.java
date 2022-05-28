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
public class EarthCycleDTO {
    private String id;
    private String expiry;
    private String activation;
    private Boolean isDay;
    private String state;
    private String timeLeft;
}
