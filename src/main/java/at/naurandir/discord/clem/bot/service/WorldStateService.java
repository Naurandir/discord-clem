package at.naurandir.discord.clem.bot.service;

import at.naurandir.discord.clem.bot.model.WarframeState;
import at.naurandir.discord.clem.bot.service.client.WarframeClient;
import at.naurandir.discord.clem.bot.service.client.dto.WorldStateDTO;
import java.io.IOException;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 *
 * @author Naurandir
 */
@Slf4j
@Service
public class WorldStateService {
    
    private WarframeClient warframeClient;
    private WarframeState warframeState;
    
    @PostConstruct
    public void init() throws IOException {
        warframeClient = new WarframeClient();
        WorldStateDTO newWorldState = warframeClient.getCurrentWorldState();
        
        warframeState = new WarframeState(newWorldState);
    }
    
    public WarframeState getWarframeState() {
        return warframeState;
    }
    
    public void refreshWarframe() {
        try {
            WorldStateDTO newWorldState = warframeClient.getCurrentWorldState();
            log.debug("getCurrentWorldState: received dto.");
            warframeState.updateByWorldState(newWorldState);
        } catch (Exception ex) {
            log.error("getCurrentWorldState: update throwed exception: ", ex);
        }
    }
}
