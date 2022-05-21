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
public class CetusCycleDTO {
    
    private String id;
    private String expiry;
    private String activation;
    private Boolean isDay;
    private String state;
    private String timeleft;
    private Boolean isCetus;
    private String shortString;
}
