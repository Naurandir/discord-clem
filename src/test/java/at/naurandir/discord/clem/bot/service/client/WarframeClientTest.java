package at.naurandir.discord.clem.bot.service.client;

import java.io.IOException;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Naurandir
 */
public class WarframeClientTest {
    
    private WarframeClient warframeClient = new WarframeClient();
    
    @Test
    public void test() throws IOException {
        warframeClient.getCurrentDropTable();
    }
}
