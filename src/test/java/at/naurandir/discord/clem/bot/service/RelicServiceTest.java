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
public class RelicServiceTest extends BaseServiceTest {

    @Autowired
    private RelicService relicService;
    
    @Test
    public void syncRelics() throws IOException {
        relicService.syncRelics();
    }
}
