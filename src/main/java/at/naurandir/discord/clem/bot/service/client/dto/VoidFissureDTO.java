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
public class VoidFissureDTO {
    
    private String node;
    private Boolean expired;
    private String missionType;
    private String tier;
    private String enemy;
    private String activation;
    private String expiry;
}
