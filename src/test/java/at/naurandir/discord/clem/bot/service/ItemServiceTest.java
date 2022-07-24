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
public class ItemServiceTest extends BaseServiceTest {
    
    @MockBean
    private BotService botService;
    
    @Autowired
    private ItemService itemService;

    @Test
    public void syncItems() throws IOException {
        itemService.syncItems();
    }
}
