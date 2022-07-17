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
public class VoidFissureDTO {

    private String id;
    private String node;
    private Boolean expired;
    private String missionType;
    private String tier;
    private Integer tierNum;
    private String enemy;
    private String activation;
    private String expiry;
    private Boolean isStorm;
}