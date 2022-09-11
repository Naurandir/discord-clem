package at.naurandir.discord.clem.bot.controller;

import at.naurandir.discord.clem.bot.service.AlertService;
import at.naurandir.discord.clem.bot.service.RelicService;
import at.naurandir.discord.clem.bot.service.VoidTraderService;
import at.naurandir.discord.clem.bot.service.push.AlertsPush;
import at.naurandir.discord.clem.bot.service.push.VoidFissuresPush;
import at.naurandir.discord.clem.bot.service.push.VoidStormPush;
import at.naurandir.discord.clem.bot.service.push.VoidTraderPush;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Naurandir
 */
@Slf4j
@RestController
public class RefreshController {
    
    @Autowired
    private VoidTraderService voidTraderService;
    
    @Autowired
    private VoidTraderPush voidTraderPush;
    
    @Autowired
    private RelicService relicService;
    
    @Autowired
    private VoidFissuresPush voidFissuresPush;
    
    @Autowired
    private VoidStormPush voidStormPush;
    
    @Autowired
    private AlertService alertService;
    
    @Autowired
    private AlertsPush alertsPush;
    
    @Operation(summary = "refresh void trader", description = "refresh void trader", tags = {"Refresh"})
    @GetMapping(value = ControllerEndpoint.REFRESH_VOID_TRADER, name = "refresh void trader")
    public void refreshVoidTrader() {
        log.info("refreshVoidTrader: start manual refresh...");
        voidTraderService.sync();
        voidTraderPush.push();
        log.info("refreshVoidTrader: start manual refresh done.");
    }
    
    @Operation(summary = "refresh void relics", description = "refresh void relics", tags = {"Refresh"})
    @GetMapping(value = ControllerEndpoint.REFRESH_VOID_RELICS, name = "refresh void relics")
    public void refreshRelics() {
        log.info("refreshRelics: start manual refresh...");
        relicService.sync();
        voidFissuresPush.push();
        voidStormPush.push();
        log.info("refreshRelics: start manual refresh done.");
    }
    
    @Operation(summary = "refresh alerts", description = "refresh alerts", tags = {"Refresh"})
    @GetMapping(value = ControllerEndpoint.REFRESH_ALERTS, name = "refresh alerts")
    public void refreshAlerts() {
        log.info("refreshAlerts: start manual refresh...");
        alertService.sync();
        alertsPush.push();
        log.info("refreshAlerts: start manual refresh done.");
    }
}
