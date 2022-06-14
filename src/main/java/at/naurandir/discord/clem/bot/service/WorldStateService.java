package at.naurandir.discord.clem.bot.service;

import at.naurandir.discord.clem.bot.model.WarframeState;
import at.naurandir.discord.clem.bot.service.client.WarframeClient;
import at.naurandir.discord.clem.bot.service.client.dto.DropTableDTO;
import at.naurandir.discord.clem.bot.service.client.dto.MarketDTO;
import at.naurandir.discord.clem.bot.service.client.dto.WorldStateDTO;
import at.naurandir.discord.clem.bot.service.client.dto.overframe.OverframeDTO;
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
        DropTableDTO newDropTable = warframeClient.getCurrentDropTable();
        MarketDTO newMarket = warframeClient.getCurrentMarket();
        OverframeDTO builds = warframeClient.getCurrentBuilds();
        
        warframeState = new WarframeState(newWorldState, newDropTable, newMarket, builds);
    }
    
    public WarframeState getWarframeState() {
        return warframeState;
    }
    
    public void refreshWorldState() {
        try {
            WorldStateDTO newWorldState = warframeClient.getCurrentWorldState();
            log.debug("refreshWorldState: received dto.");
            warframeState.refreshWorldState(newWorldState);
        } catch (Exception ex) {
            log.error("refreshWorldState: update throwed exception: ", ex);
        }
    }
    
    public void refreshDropTable() {
        try {
            DropTableDTO dropTable = warframeClient.getCurrentDropTable();
            log.debug("refreshDropTable: received dto.");
            warframeState.refreshDropTableData(dropTable);
        } catch (Exception ex) {
            log.error("refreshDropTable: update throwed exception: ", ex);
        }
    }
    
    public void refreshMarket() {
        try {
            MarketDTO market = warframeClient.getCurrentMarket();
            log.debug("refreshMarket: received dto.");
            warframeState.refreshMarketData(market);
        } catch (Exception ex) {
            log.error("refreshMarket: update throwed exception: ", ex);
        }
    }
    
    public void refreshBuilds() {
        try {
            OverframeDTO overframeBuilds = warframeClient.getCurrentBuilds();
            log.debug("refreshBuilds: received dto.");
            warframeState.refreshBuildsData(overframeBuilds);
        } catch (Exception ex) {
            log.error("refreshBuilds: update throwed exception: ", ex);
        }
    }
}
