package at.naurandir.discord.clem.bot.service.client.dto;

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
public class VallisCycleDTO {
    
    private String id;
    private String expiry;
    private Boolean isWarm;
    private String state;
    private String activation;
    private String timeLeft;
    private String shortString;
    
}
