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
public class MissionDTO {
    
    private String node;
    private Integer minEnemyLevel;
    private Integer maxEenemyLevel;
    private String type;
    private String enemySpec;
    private String levelOverride;
    private String description;
}
