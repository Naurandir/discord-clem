package at.naurandir.discord.clem.bot.model;

import at.naurandir.discord.clem.bot.service.client.dto.AlertDTO;
import at.naurandir.discord.clem.bot.service.client.dto.CambionCycleDTO;
import at.naurandir.discord.clem.bot.service.client.dto.CetusCycleDTO;
import at.naurandir.discord.clem.bot.service.client.dto.VallisCycleDTO;
import at.naurandir.discord.clem.bot.service.client.dto.VoidFissureDTO;
import at.naurandir.discord.clem.bot.service.client.dto.VoidTraderDTO;
import at.naurandir.discord.clem.bot.service.client.dto.WorldStateDTO;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Naurandir
 */
@Getter
@Setter
public class WarframeState {
    
    private List<AlertDTO> alerts;
    private List<VoidFissureDTO> fissures;
    
    private CetusCycleDTO cetusCycle;
    private VallisCycleDTO vallisCycle;
    private CambionCycleDTO cambionCycle;
    
    private VoidTraderDTO voidTrader;
    
    private boolean isVoidTraderStateChanged;
    private boolean isAlertsStateChanged;
    
    private Comparator<VoidFissureDTO> voidFissureComparator = Comparator.comparing(VoidFissureDTO::getTierNum);

    public WarframeState(WorldStateDTO newWorldState) {
        updateData(newWorldState);
    }

    public void updateByWorldState(WorldStateDTO newWorldState) {
        checkDataChanged(newWorldState);
        updateData(newWorldState);
    }
    
    private void updateData(WorldStateDTO newWorldState) {
        voidTrader = newWorldState.getVoidTraderDTO();
        
        alerts = newWorldState.getAlertsDTO();
        fissures = newWorldState.getFissuresDTO();
        Collections.sort(fissures, voidFissureComparator);

        cetusCycle = newWorldState.getCetusCycleDTO();
        vallisCycle = newWorldState.getVallisCycleDTO();
        cambionCycle = newWorldState.getCambionCycleDTO();
    }
    
    private void checkDataChanged(WorldStateDTO newWorldState) {
        setAlertsStateChanged((getAlerts() == null && newWorldState.getAlertsDTO() != null) ||
                (getAlerts() != null && newWorldState.getAlertsDTO() == null) ||
                getAlerts().size() != newWorldState.getAlertsDTO().size());
        
        setVoidTraderStateChanged(!Objects.equals(getVoidTrader().getActive(),
                newWorldState.getVoidTraderDTO().getActive()));
    }
}
