package at.naurandir.discord.clem.bot.model;

import at.naurandir.discord.clem.bot.service.client.dto.DropTableDTO;
import at.naurandir.discord.clem.bot.service.client.dto.MarketDTO;
import at.naurandir.discord.clem.bot.service.client.dto.worldstate.AlertDTO;
import at.naurandir.discord.clem.bot.service.client.dto.worldstate.CambionCycleDTO;
import at.naurandir.discord.clem.bot.service.client.dto.worldstate.CetusCycleDTO;
import at.naurandir.discord.clem.bot.service.client.dto.worldstate.VallisCycleDTO;
import at.naurandir.discord.clem.bot.service.client.dto.worldstate.VoidFissureDTO;
import at.naurandir.discord.clem.bot.service.client.dto.worldstate.VoidTraderDTO;
import at.naurandir.discord.clem.bot.service.client.dto.WorldStateDTO;
import at.naurandir.discord.clem.bot.service.client.dto.droptable.MissionDTO;
import at.naurandir.discord.clem.bot.service.client.dto.droptable.RelicDTO;
import at.naurandir.discord.clem.bot.service.client.dto.market.MarketItemDTO;
import at.naurandir.discord.clem.bot.service.client.dto.worldstate.EarthCycleDTO;
import at.naurandir.discord.clem.bot.service.client.dto.worldstate.EventDTO;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
    private List<EventDTO> events;
    private List<VoidFissureDTO> fissures;
    
    private EarthCycleDTO earthCycle;
    private CetusCycleDTO cetusCycle;
    private VallisCycleDTO vallisCycle;
    private CambionCycleDTO cambionCycle;
    
    private VoidTraderDTO voidTrader;
    
    private boolean isVoidTraderStateChanged;
    private boolean isAlertsStateChanged;
    private boolean isEventStateChanged;
    
    // drop table
    private List<RelicDTO> relics;  
    private List<MissionDTO> missions;
    
    // market
    private List<MarketItemDTO> marketItems;
    private List<MarketItemDTO> marketLichWeapons;
    
    private Comparator<VoidFissureDTO> voidFissureComparator = Comparator.comparing(VoidFissureDTO::getTierNum);

    public WarframeState(WorldStateDTO newWorldState, DropTableDTO dropTable, MarketDTO market) {
        updateWorldStateData(newWorldState);
        refreshDropTableData(dropTable);
        refreshMarketData(market);
    }

    public void refreshWorldState(WorldStateDTO newWorldState) {
        checkWorldStateDataChanged(newWorldState);
        updateWorldStateData(newWorldState);
    }
    
    public final void refreshDropTableData(DropTableDTO dropTable) {
        relics = dropTable.getRelics();
        missions = dropTable.getMissions();
    }

    public final void refreshMarketData(MarketDTO marketData) {
        marketItems = marketData.getItems().getPayload().get("items");
        marketLichWeapons = marketData.getLichWeapons().getPayload().get("weapons");
    }
    
    private void updateWorldStateData(WorldStateDTO newWorldState) {
        voidTrader = newWorldState.getVoidTraderDTO();
        
        events = Objects.requireNonNullElseGet(newWorldState.getEventsDTO(), List::of);
        alerts = Objects.requireNonNullElseGet(newWorldState.getAlertsDTO(), List::of);
        fissures = Objects.requireNonNullElseGet(newWorldState.getFissuresDTO(), List::of);
        Collections.sort(fissures, voidFissureComparator);

        earthCycle = newWorldState.getEarthCycleDTO();
        cetusCycle = newWorldState.getCetusCycleDTO();
        vallisCycle = newWorldState.getVallisCycleDTO();
        cambionCycle = newWorldState.getCambionCycleDTO();
    }
    
    private void checkWorldStateDataChanged(WorldStateDTO newWorldState) {
        setVoidTraderStateChanged(!Objects.equals(getVoidTrader().getActive(),
                newWorldState.getVoidTraderDTO().getActive()));
        
        setAlertsStateChanged(!Objects.equals(
                Optional.of(newWorldState.getAlertsDTO()).orElse(List.of()).size(),
                getAlerts().size()));        
        
        setEventStateChanged(!Objects.equals(
                Optional.of(newWorldState.getEventsDTO()).orElse(List.of()).size(),
                getEvents().size()));
    }
}