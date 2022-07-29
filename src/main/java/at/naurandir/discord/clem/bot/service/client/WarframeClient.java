package at.naurandir.discord.clem.bot.service.client;

import at.naurandir.discord.clem.bot.service.client.dto.MarketDTO;
import at.naurandir.discord.clem.bot.service.client.dto.market.MarketItemsDTO;
import at.naurandir.discord.clem.bot.service.client.dto.market.MarketLichAuctionsDTO;
import at.naurandir.discord.clem.bot.service.client.dto.market.MarketLichWeaponsDTO;
import at.naurandir.discord.clem.bot.service.client.dto.market.MarketOrdersResultDTO;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

/**
 *
 * @author Naurandir
 */
@Slf4j
@Service
public class WarframeClient {
    
    private final Gson gson = new Gson();
    
    public String getDataRaw(String url, Map<String, String> headers) throws IOException {
        try (CloseableHttpClient httpClient = getClient()) {
            HttpGet httpGet = new HttpGet(url);
            for (Map.Entry<String, String> header : headers.entrySet()) {
                httpGet.addHeader(header.getKey(), header.getValue());
            }
            
            StopWatch watch = new StopWatch();
            watch.start();
            
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                log.info("getDataRaw: received http status [{}] for url [{}]", response.getStatusLine(), url);
                watch.stop();
                log.debug("getDataRaw: needed [{}]ms for getting raw data", watch.getTotalTimeMillis());
                return getHttpContent(response);
            }
        }
    }
    
    public <T> T getData(String url, Map<String, String> headers, Class<T> clazz) throws IOException {
        try (CloseableHttpClient httpClient = getClient()) {
            HttpGet httpGet = new HttpGet(url);
            for (Map.Entry<String, String> header : headers.entrySet()) {
                httpGet.addHeader(header.getKey(), header.getValue());
            }
            
            StopWatch watch = new StopWatch();
            watch.start();
            
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                log.info("getData: received http status [{}] for url [{}]", response.getStatusLine(), url);
                String jsonString = getHttpContent(response);
                
                watch.stop();
                log.debug("getData: needed [{}]ms for getting [{}]", watch.getTotalTimeMillis(), clazz);
                
                return gson.fromJson(jsonString, clazz);
            }
        }
    }
    
    public <T> List<T> getListData(String url, Map<String, String> headers, Class<T> clazz) throws IOException {
        try (CloseableHttpClient httpClient = getClient()) {
            HttpGet httpGet = new HttpGet(url);
            for (Map.Entry<String, String> header : headers.entrySet()) {
                httpGet.addHeader(header.getKey(), header.getValue());
            }
            
            StopWatch watch = new StopWatch();
            watch.start();
            
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                log.info("getData: received http status [{}] for url [{}]", response.getStatusLine(), url);
                String jsonString = getHttpContent(response);
                
                Type clazzType = TypeToken.getParameterized(List.class, clazz).getType();
                
                watch.stop();
                log.debug("getListData: needed [{}]ms for getting list of [{}]", watch.getTotalTimeMillis(), clazz);
                
                return gson.fromJson(jsonString, clazzType);
            }
        }
    }
    
    public MarketDTO getCurrentMarket() throws IOException {
        MarketDTO market = new MarketDTO();
        market.setItems(getCurrentMarketItems());
        market.setLichWeapons(getCurrentLichWeapons());
        
        return market;
    }
    
    private MarketItemsDTO getCurrentMarketItems() throws IOException {
        try (CloseableHttpClient httpClient = getClient()) {
            HttpGet httpGet = new HttpGet("https://api.warframe.market/v1/items");
            httpGet.addHeader("Language", "en");
            
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                log.info("getCurrentMarketItems: received http status [{}]", response.getStatusLine());
                String jsonString = getHttpContent(response);
                return gson.fromJson(jsonString, MarketItemsDTO.class);
            }
        }
    }
    
    private MarketLichWeaponsDTO getCurrentLichWeapons() throws IOException {
        try (CloseableHttpClient httpClient = getClient()) {
            HttpGet httpGet = new HttpGet("https://api.warframe.market/v1/lich/weapons");
            httpGet.addHeader("Language", "en");
            
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                log.info("getCurrentLichWeapons: received http status [{}]", response.getStatusLine());
                String jsonString = getHttpContent(response);
                return gson.fromJson(jsonString, MarketLichWeaponsDTO.class);
            }
        }
    }
    
    public MarketOrdersResultDTO getCurrentOrders(String urlName) throws IOException {
        try (CloseableHttpClient httpClient = getClient()) {
            HttpGet httpGet = new HttpGet("https://api.warframe.market/v1/items/" + urlName + "/orders");
            httpGet.addHeader("Language", "en");
            httpGet.addHeader("Platform", "pc");
            
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                log.info("getCurrentOrders: received http status [{}]", response.getStatusLine());
                String jsonString = getHttpContent(response);
                return gson.fromJson(jsonString, MarketOrdersResultDTO.class);
            }
        }
    }
    
    public MarketLichAuctionsDTO getCurrentLichAuctions(String urlName, Optional<String> element) throws IOException {
        try (CloseableHttpClient httpClient = getClient()) {
            String url = "https://api.warframe.market/v1/auctions/search?type=lich&weapon_url_name=" + urlName;
            if (element.isPresent()) {
                url += "&element=" + element.get();
            }
            log.debug("getCurrentLichAuctions: calling [{}]", url);
            
            HttpGet httpGet = new HttpGet(url);
            httpGet.addHeader("Language", "en");
            httpGet.addHeader("Platform", "pc");
            
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                log.info("getCurrentLichAuctions: received http status [{}]", response.getStatusLine());
                String jsonString = getHttpContent(response);
                return gson.fromJson(jsonString, MarketLichAuctionsDTO.class);
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
