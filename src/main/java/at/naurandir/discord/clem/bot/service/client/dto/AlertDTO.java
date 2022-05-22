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
public class AlertDTO {
    
        private MissionDTO mission;
        private Boolean active;
        private Boolean expired;
        private String id;
        private String expiry;
        private String activation;
        private String rewardType;
        
}
