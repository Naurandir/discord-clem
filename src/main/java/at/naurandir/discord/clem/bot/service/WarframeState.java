package at.naurandir.discord.clem.bot.service;

import at.naurandir.discord.clem.bot.service.client.dto.AlertDTO;
import at.naurandir.discord.clem.bot.service.client.dto.CambionCycleDTO;
import at.naurandir.discord.clem.bot.service.client.dto.CetusCycleDTO;
import at.naurandir.discord.clem.bot.service.client.dto.VallisCycleDTO;
import at.naurandir.discord.clem.bot.service.client.dto.VoidFissureDTO;
import at.naurandir.discord.clem.bot.service.client.dto.VoidTraderDTO;
import at.naurandir.discord.clem.bot.service.client.dto.WorldStateDTO;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Naurandir
 */
@Getter
@Setter
public class WarframeState {
    
    private VoidTraderDTO voidTrader;
    
    private List<AlertDTO> alerts;
    private List<VoidFissureDTO> fissures;
    
    private CetusCycleDTO cetusCycle;
    private VallisCycleDTO vallisCycle;
    private CambionCycleDTO cambionCycle;
    
    private boolean isVoidTraderStateChanged;
    private boolean isAlertsStateChanged;
    
    public void updateByWorldState(WorldStateDTO worldState) {
        voidTrader = worldState.getVoidTraderDTO();
        
        alerts = worldState.getAlertsDTO();
        fissures = worldState.getFissuresDTO();
        
        cetusCycle = worldState.getCetusCycleDTO();
        vallisCycle = worldState.getVallisCycleDTO();
        cambionCycle = worldState.getCambionCycleDTO();
    }
}
