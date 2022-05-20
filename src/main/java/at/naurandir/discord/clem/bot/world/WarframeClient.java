package at.naurandir.discord.clem.bot.world;

import java.io.IOException;
import java.nio.charset.Charset;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author Naurandir
 */
@Slf4j
public class WarframeClient {
    
    public void getCurrentWorldState() {
        
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet("https://api.warframestat.us/pc");
            httpGet.addHeader("Accept-Language", "en");
            
            try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
                log.info("getCurrentWorldState: received http status [{}]", response.getStatusLine());
                String jsonString = EntityUtils.toString(response.getEntity(), Charset.forName("UTF_8"));
                
            }
        } catch (IOException ex) {
            log.info("getCurrentWorldState: exception happened: ", ex);
        }
    }
}
