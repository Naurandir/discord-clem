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
        try (CloseableHttpClient httpClient = getClient()) {
            HttpGet httpGet = new HttpGet("https://api.warframestat.us/pc");
            httpGet.addHeader("Accept-Language", "en");
            
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                log.info("getCurrentWorldState: received http status [{}]", response.getStatusLine());
                String jsonString = getHttpContent(response);
                return gson.fromJson(jsonString, WorldStateDTO.class);
            }
        }
    }
    
    public DropTableDTO getCurrentDropTable() throws IOException {
        DropTableDTO dropTableDtoWithRelicsOnly = getDropTableWithRelics();
        //DropTableDTO dropTableDtoWithMissionsOnly = getDropTableWithMissions();
        return new DropTableDTO(dropTableDtoWithRelicsOnly.getRelics(), null);
    }

    DropTableDTO getDropTableWithRelics() throws JsonSyntaxException, ParseException, IOException {
        try (CloseableHttpClient httpClient = getClient()) {
            HttpGet httpGet = new HttpGet("https://warframe.fandom.com/wiki/Module:DropTables/JSON/Relics");
            
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                log.info("getDropTableWithRelics: received http status [{}]", response.getStatusLine());
                String htmlString = getHttpContent(response);
                
                Document html = Jsoup.parse(htmlString);
                Elements elements = html.getElementsByClass("mw-code");
                
                String jsonString = elements.first().text();
                jsonString = jsonString.replace("return'", "");
                jsonString = jsonString.substring(0, jsonString.length() - 1);
                
                return gson.fromJson(jsonString, DropTableDTO.class);
            }
        }
    }
    
    DropTableDTO getDropTableWithMissions() throws JsonSyntaxException, ParseException, IOException {
        try (CloseableHttpClient httpClient = getClient()) {
            HttpGet httpGet = new HttpGet("https://warframe.fandom.com/wiki/Module:DropTables/JSON/Missions");
            
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                log.info("getDropTableWithMissions: received http status [{}]", response.getStatusLine());
                String htmlString = getHttpContent(response);
                
                Document html = Jsoup.parse(htmlString);
                Elements elements = html.getElementsByClass("mw-code");
                
                String jsonString = elements.first().text();
                jsonString = jsonString.replace("return[[", "");
                jsonString = jsonString.substring(0, jsonString.length() - 2);
                
                return gson.fromJson(jsonString, DropTableDTO.class);
            }
        }
    }
    
    CloseableHttpClient getClient() {
        return HttpClients.createDefault();
    }
    
    String getHttpContent(CloseableHttpResponse response) throws IOException {
        return EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));
    }
}
