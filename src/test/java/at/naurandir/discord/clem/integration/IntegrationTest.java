package at.naurandir.discord.clem.integration;

import at.naurandir.discord.clem.bot.service.BotService;
import at.naurandir.discord.clem.bot.service.MissionService;
import at.naurandir.discord.clem.bot.service.WarframeService;
import at.naurandir.discord.clem.bot.service.WeaponService;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.EnabledIf;

/**
 *
 * @author Naurandir
 */
@EnabledIf(value = "${discord.clem.integration.test}", loadContext = true)
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
    
    @Test
    public void testWarframeService() throws IOException {
        warframeService.syncWarframes(); // add
        warframeService.syncWarframes(); // update
    }
    
    @Test
    public void testWeaponService() throws IOException {
        weaponService.syncWeapons(); // add
        weaponService.syncWeapons(); // update
    }
    
    @Test
    public void testMissionService() throws IOException {
        missionService.syncMissions(); // add
        missionService.syncMissions(); // update
    }
}
