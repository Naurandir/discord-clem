package at.naurandir.discord.clem.bot.service;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 *
 * @author Naurandir
 */
@SpringBootTest
public class MissionServiceTest extends BaseServiceTest {

    @Autowired
    private MissionService missionService;
    
    @Test
    public void syncMissions() throws IOException {
        missionService.syncMissions();
    }
}
