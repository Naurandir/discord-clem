package at.naurandir.discord.clem.bot.service.client.dto.worldstate;

import java.util.List;
import lombok.Data;

/**
 *
 * @author Naurandir
 */
@Data
public class AlertDTO {
    
        private EmbeddableMissionDTO mission;
        private Boolean active;
        private String id;
        private String expiry;
        private String activation;
        private List<String> rewardTypes;
        
}
