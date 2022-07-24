package at.naurandir.discord.clem.bot.service;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

/**
 *
 * @author Naurandir
 */
@SpringBootTest
public class WarframeServiceTest extends BaseServiceTest {
    
    @MockBean
    private BotService botService;
    
    @Autowired
    private WarframeService warframeService;

    @Test
    public void syncWarframes() throws IOException {
        warframeService.syncWarframes();
    }
}
