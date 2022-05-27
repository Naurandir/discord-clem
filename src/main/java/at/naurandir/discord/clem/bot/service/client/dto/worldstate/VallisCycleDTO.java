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
public class VallisCycleDTO {
    
    private String id;
    private String expiry;
    private Boolean isWarm;
    private String state;
    private String activation;
    private String timeLeft;
    private String shortString;
    
}
