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
public class AlertServiceTest extends BaseServiceTest {

    @Autowired
    private AlertService alertService;
    
    @Test
    public void syncMissions() throws IOException {
        alertService.syncAlerts();
    }
}
