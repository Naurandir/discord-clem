package at.naurandir.discord.clem.integration;

import at.naurandir.discord.clem.bot.service.BotService;
import at.naurandir.discord.clem.bot.service.ItemBuildService;
import at.naurandir.discord.clem.bot.service.MarketService;
import at.naurandir.discord.clem.bot.service.MissionService;
import at.naurandir.discord.clem.bot.service.WarframeService;
import at.naurandir.discord.clem.bot.service.WeaponService;
import at.naurandir.discord.clem.bot.service.command.BuildSearchCommand;
import java.io.IOException;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit.jupiter.EnabledIf;

/**
 *
 * @author Naurandir
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnabledIf(value = "${discord.clem.integration.test}", loadContext = true)
@Profile("test")
@SpringBootTest
public class IntegrationTest {
    
    @MockBean
    private BotService botService;

    @Autowired
    private WarframeService warframeService;
    
    @Autowired
    private WeaponService weaponService;
    
    @Autowired
    private MissionService missionService;
    
    @Autowired
    private ItemBuildService itemBuildService;
    
    @Autowired
    private MarketService marketService;
    
    @Autowired
    private BuildSearchCommand buildSearchCommand;
    
    @Test
    @Order(1)
    public void testWarframeService() throws IOException {
        warframeService.sync(); // update
    }
    
    @Test
    @Order(2)
    public void testWeaponService() throws IOException {
        weaponService.sync(); // update
    }
    
    @Test
    @Order(3)
    public void testBuildService() throws IOException {
        itemBuildService.sync(); // update
    }
    
    @Test
    @Order(4)
    public void testMissionService() throws IOException {
        missionService.sync(); // update
    }
    
    @Test
    @Order(5)
    public void testMarketService() throws IOException {
        marketService.sync(); // add
        marketService.sync(); // update
    }
}
