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
public class ItemServiceTest extends BaseServiceTest {
    
    @Autowired
    private ItemService itemService;

    @Test
    public void syncItems() throws IOException {
        itemService.syncItems();
    }
}
