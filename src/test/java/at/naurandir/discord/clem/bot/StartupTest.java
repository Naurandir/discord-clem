package at.naurandir.discord.clem.bot;

import at.naurandir.discord.clem.bot.service.BotService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

/**
 *
 * @author Naurandir
 */
@SpringBootTest
public class StartupTest {
    
    @MockBean
    private BotService botService;
    
    @Test
    public void startup() {
        //
    }
}
