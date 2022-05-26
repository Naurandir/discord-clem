package at.naurandir.discord.clem.bot.service.client;

import at.naurandir.discord.clem.bot.service.client.dto.DropTableDTO;
import at.naurandir.discord.clem.bot.service.client.dto.WorldStateDTO;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.nio.charset.Charset;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author Naurandir
 */
@Slf4j
public class WarframeClient {
    Gson gson = new Gson();
    
    public WorldStateDTO getCurrentWorldState() throws IOException {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet("https://api.warframestat.us/pc");
            httpGet.addHeader("Accept-Language", "en");
            
            try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
                log.info("getCurrentWorldState: received http status [{}]", response.getStatusLine());
                String jsonString = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));
                return gson.fromJson(jsonString, WorldStateDTO.class);
            }
        }
    }
    
    // https://warframe.fandom.com/wiki/Module:DropTables/JSON/Relics
    // https://warframe.fandom.com/wiki/Module:DropTables/JSON/Missions
    
    public DropTableDTO getCurrentDropTables() throws IOException {
        DropTableDTO dropTableDtoWithRelicsOnly = getDropTableWithRelics();
        DropTableDTO dropTableDtoWithMissionsOnly = null;
        
        return new DropTableDTO(dropTableDtoWithRelicsOnly.getRelics(), null);
    }

    private DropTableDTO getDropTableWithRelics() throws JsonSyntaxException, ParseException, IOException {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet("https://warframe.fandom.com/wiki/Module:DropTables/JSON/Relics");
            
            try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
                log.info("getCurrentWorldState: received http status [{}]", response.getStatusLine());
                String htmlString = EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));
                
                Document html = Jsoup.parse(htmlString);
                Elements elements = html.select("pre");
                
                String jsonString = elements.first().data();
                log.debug("getDropTables: found element: [{}]", jsonString);
                
                // json conform
                jsonString = jsonString.replace("return'", "")
                        .replace("&quot;", "\"");
                
                // remove the last ' as it is not part of the json
                jsonString = jsonString.substring(0, jsonString.length() - 2);
                
                return gson.fromJson(jsonString, DropTableDTO.class);
            }
        }
    }
}
