package at.naurandir.discord.clem.bot.service.client;

import at.naurandir.discord.clem.bot.model.enums.Rarity;
import at.naurandir.discord.clem.bot.service.client.dto.DropTableDTO;
import at.naurandir.discord.clem.bot.service.client.dto.MarketDTO;
import at.naurandir.discord.clem.bot.service.client.dto.WorldStateDTO;
import at.naurandir.discord.clem.bot.service.client.dto.droptable.MissionDropDTO;
import at.naurandir.discord.clem.bot.service.client.dto.droptable.MissionRewardDropDTO;
import at.naurandir.discord.clem.bot.service.client.dto.droptable.RewardDropDTO;
import at.naurandir.discord.clem.bot.service.client.dto.market.MarketOrdersResultDTO;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
    
    public MarketDTO getCurrentMarket() throws IOException {
        try (CloseableHttpClient httpClient = getClient()) {
            HttpGet httpGet = new HttpGet("https://api.warframe.market/v1/items");
            httpGet.addHeader("Language", "en");
            
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                log.info("getCurrentWarframeMarket: received http status [{}]", response.getStatusLine());
                String jsonString = getHttpContent(response);
                return gson.fromJson(jsonString, MarketDTO.class);
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
    
    public DropTableDTO getCurrentDropTable() throws IOException {
        DropTableDTO dropTableDtoWithRelicsOnly = getDropTableWithRelics();
        DropTableDTO dropTableDtoWithMissionsOnly = getDropTableWithMissions();
        return new DropTableDTO(dropTableDtoWithRelicsOnly.getRelics(), dropTableDtoWithMissionsOnly.getMissionRewards());
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
                jsonString = jsonString.replace("return[[", "").replace("}}}]]", "}}}").replace(",{\"_id\":", ",\n{\"_id\":");
                
                DropTableDTO dto = gson.fromJson(jsonString, DropTableDTO.class);
                
                List<MissionDropDTO> missionDrops = dto.getMissionRewards().values().stream()
                        .flatMap(map -> map.values().stream())
                        .collect(Collectors.toList());
                
                for (MissionDropDTO missionDrop : missionDrops) {
                    if (missionDrop.getRewardsObject() instanceof List) {
                        createGeneralRewards(missionDrop);
                    } else {
                        createRotationRewards(missionDrop);
                    }
                    
                    missionDrop.setRewardsObject(null);
                }
                
                return dto;
            }
        }
    }

    private void createGeneralRewards(MissionDropDTO missionDrop) {
        List<Object> rewardObjectList = (List) missionDrop.getRewardsObject();
        List<RewardDropDTO> generalRewards = new ArrayList<>();
        
        for (Object element : rewardObjectList) {
            generalRewards.add(createRewardDrop(element));
        }
        
        MissionRewardDropDTO missionRewardDrop = new MissionRewardDropDTO(List.of(), List.of(), List.of(), generalRewards);
        missionDrop.setMissionRewards(missionRewardDrop);
    }
    
    private void createRotationRewards(MissionDropDTO missionDrop) {
        Map<String, List<Object>> rewardObjectMap = (Map<String, List<Object>>) missionDrop.getRewardsObject();
        List<RewardDropDTO> rotationRewardsA = new ArrayList<>();
        List<RewardDropDTO> rotationRewardsB = new ArrayList<>();
        List<RewardDropDTO> rotationRewardsC = new ArrayList<>();
        
        if (rewardObjectMap.get("A") != null) {
            for (Object element : rewardObjectMap.get("A")) {
                rotationRewardsA.add(createRewardDrop(element));
            }
        }
        
        if (rewardObjectMap.get("B") != null) {
            for (Object element : rewardObjectMap.get("B")) {
                rotationRewardsB.add(createRewardDrop(element));
            }
        }
        
        if (rewardObjectMap.get("C") != null) {
            for (Object element : rewardObjectMap.get("C")) {
                rotationRewardsC.add(createRewardDrop(element));
            }
        }
        
        MissionRewardDropDTO missionRewardDrop = new MissionRewardDropDTO(rotationRewardsA, rotationRewardsB, rotationRewardsC, List.of());
        missionDrop.setMissionRewards(missionRewardDrop);
    }
    
    private RewardDropDTO createRewardDrop(Object element) throws NumberFormatException {
        Map<String, Object> elementMap = (Map<String, Object>) element;
        RewardDropDTO rewardDrop = new RewardDropDTO();
        rewardDrop.setItemName((String) elementMap.get("itemName"));
        rewardDrop.setRarity(Rarity.valueOf(((String) elementMap.get("rarity")).toUpperCase()));
        rewardDrop.setChance((Double) elementMap.get("chance"));
        return rewardDrop;
    }
    
    CloseableHttpClient getClient() {
        return HttpClients.createDefault();
    }
    
    String getHttpContent(CloseableHttpResponse response) throws IOException {
        return EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));
    }
}
