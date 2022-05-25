package at.naurandir.discord.clem.bot.service;

import at.naurandir.discord.clem.bot.model.WarframeState;
import at.naurandir.discord.clem.bot.service.client.WarframeClient;
import at.naurandir.discord.clem.bot.service.client.dto.WorldStateDTO;
import java.io.IOException;
import java.util.Objects;
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
        
        warframeState = new WarframeState();
        
        WorldStateDTO newWorldState = warframeClient.getCurrentWorldState();
        warframeState.setCetusCycle(newWorldState.getCetusCycleDTO());
        warframeState.setVallisCycle(newWorldState.getVallisCycleDTO());
        warframeState.setAlerts(newWorldState.getAlertsDTO());
        warframeState.setVoidTraderDTO(newWorldState.getVoidTraderDTO());
        
        warframeState.setVoidTraderStateChanged(false);
        warframeState.setAlertsStateChanged(false);
    }
    
    public WarframeState getWarframeState() {
        return warframeState;
    }
    
    public void refreshWarframe() {
        try {
            WorldStateDTO newWorldState = warframeClient.getCurrentWorldState();
            log.debug("getCurrentWorldState: received dto.");
            
            updateAlerts(newWorldState);
            updateWorldCycles(newWorldState);
            updateVoidTrader(newWorldState);
        } catch (Exception ex) {
            log.error("getCurrentWorldState: update throwed exception: ", ex);
        }
    }

    private void updateWorldCycles(WorldStateDTO newWorldState) {
        warframeState.setCetusCycle(newWorldState.getCetusCycleDTO());
        warframeState.setVallisCycle(newWorldState.getVallisCycleDTO());
        warframeState.setCambionCycle(newWorldState.getCambionCycleDTO());
    }   

    private void updateAlerts(WorldStateDTO newWorldState) {
        warframeState.setAlertsStateChanged((warframeState.getAlerts() == null && newWorldState.getAlertsDTO() != null) ||
                                            (warframeState.getAlerts() != null && newWorldState.getAlertsDTO() == null) ||
                                            warframeState.getAlerts().size() != newWorldState.getAlertsDTO().size());
        warframeState.setAlerts(newWorldState.getAlertsDTO());
    }

    private void updateVoidTrader(WorldStateDTO newWorldState) {
        warframeState.setVoidTraderStateChanged(!Objects.equals(warframeState.getVoidTraderDTO().getActive(), newWorldState.getVoidTraderDTO().getActive()));
        warframeState.setVoidTraderDTO(newWorldState.getVoidTraderDTO());
    }
}
