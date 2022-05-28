package at.naurandir.discord.clem.bot.model;

import at.naurandir.discord.clem.bot.service.client.dto.DropTableDTO;
import at.naurandir.discord.clem.bot.service.client.dto.worldstate.AlertDTO;
import at.naurandir.discord.clem.bot.service.client.dto.worldstate.CambionCycleDTO;
import at.naurandir.discord.clem.bot.service.client.dto.worldstate.CetusCycleDTO;
import at.naurandir.discord.clem.bot.service.client.dto.worldstate.VallisCycleDTO;
import at.naurandir.discord.clem.bot.service.client.dto.worldstate.VoidFissureDTO;
import at.naurandir.discord.clem.bot.service.client.dto.worldstate.VoidTraderDTO;
import at.naurandir.discord.clem.bot.service.client.dto.WorldStateDTO;
import at.naurandir.discord.clem.bot.service.client.dto.droptable.MissionDropDTO;
import at.naurandir.discord.clem.bot.service.client.dto.droptable.RelicDropDTO;
import at.naurandir.discord.clem.bot.service.client.dto.worldstate.EarthCycleDTO;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
    
    // world
    private List<AlertDTO> alerts;
    private List<VoidFissureDTO> fissures;
    
    private EarthCycleDTO earthCycle;
    private CetusCycleDTO cetusCycle;
    private VallisCycleDTO vallisCycle;
    private CambionCycleDTO cambionCycle;
    
    private VoidTraderDTO voidTrader;
    
    private boolean isVoidTraderStateChanged;
    private boolean isAlertsStateChanged;
    
    // drop table
    private List<RelicDropDTO> relics;  
    private Map<String,Map<String,MissionDropDTO>> missions;
    
    private Comparator<VoidFissureDTO> voidFissureComparator = Comparator.comparing(VoidFissureDTO::getTierNum);

    public WarframeState(WorldStateDTO newWorldState, DropTableDTO dropTable) {
        updateWorldStateData(newWorldState);
        updateDropTableData(dropTable);
    }

    public void updateByWorldState(WorldStateDTO newWorldState) {
        checkWorldStateDataChanged(newWorldState);
        updateWorldStateData(newWorldState);
    }
    
    private void updateWorldStateData(WorldStateDTO newWorldState) {
        voidTrader = newWorldState.getVoidTraderDTO();
        
        alerts = newWorldState.getAlertsDTO();
        fissures = newWorldState.getFissuresDTO();
        Collections.sort(fissures, voidFissureComparator);

        earthCycle = newWorldState.getEarthCycleDTO();
        cetusCycle = newWorldState.getCetusCycleDTO();
        vallisCycle = newWorldState.getVallisCycleDTO();
        cambionCycle = newWorldState.getCambionCycleDTO();
    }
    
    private void checkWorldStateDataChanged(WorldStateDTO newWorldState) {
        setAlertsStateChanged(!Objects.equals(getAlerts(), newWorldState.getAlertsDTO()));        
        setVoidTraderStateChanged(!Objects.equals(getVoidTrader().getActive(),
                newWorldState.getVoidTraderDTO().getActive()));
    }
    
    public final void updateDropTableData(DropTableDTO dropTable) {
        relics = dropTable.getRelics();
        missions = dropTable.getMissionRewards();
    }
}
