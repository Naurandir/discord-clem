package at.naurandir.discord.clem.bot.service.client;

import at.naurandir.discord.clem.bot.model.enums.Rarity;
import at.naurandir.discord.clem.bot.service.client.dto.DropTableDTO;
import at.naurandir.discord.clem.bot.service.client.dto.MarketDTO;
import at.naurandir.discord.clem.bot.service.client.dto.WorldStateDTO;
import at.naurandir.discord.clem.bot.service.client.dto.droptable.MissionDTO;
import at.naurandir.discord.clem.bot.service.client.dto.droptable.RelicDTO;
import at.naurandir.discord.clem.bot.service.client.dto.droptable.RewardDTO;
import at.naurandir.discord.clem.bot.service.client.dto.market.MarketOrdersResultDTO;
import com.google.gson.Gson;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
    
    public DropTableDTO getCurrentDropTableHtml() throws IOException {
        try (CloseableHttpClient httpClient = getClient()) {
            HttpGet httpGet = new HttpGet("https://n8k6e2y6.ssl.hwcdn.net/repos/hnfvc0o3jnfvc873njb03enrf56.html");
            
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                log.info("getDropTableWithRelics: received http status [{}]", response.getStatusLine());
                String htmlString = getHttpContent(response);
                
                Document html = Jsoup.parse(htmlString);
                Elements elements = html.select("table");
                
                Elements relicElements = elements.get(1).select("tr");
                List<RelicDTO> relics = getRelicsHtml(relicElements);
                
                Elements missionElements = elements.get(0).select("tr");
                List<MissionDTO> missions = getMissionsHtml(missionElements);
                
                DropTableDTO dropTable = new DropTableDTO();
                dropTable.setRelics(relics);
                dropTable.setMissions(missions);
                return dropTable;
            }
        }
    }

    private List<RelicDTO> getRelicsHtml(Elements relicRewards) {
        Map<String, RelicDTO> relics = new HashMap<>();
        
        for (int i=0; i<relicRewards.size()-8; i+=8) {
            String[] titleSplitted = relicRewards.get(i).selectFirst("th").text().split(" ");
            String name = titleSplitted[0];
            String tier = titleSplitted[1];
            String relicKey = titleSplitted[0] + " " + titleSplitted[1];
            
            RelicDTO relicDrop = relics.get(relicKey);
            if (relicDrop != null) {
                continue;
            }
            
            relicDrop = new RelicDTO();
            relicDrop.setName(name);
            relicDrop.setTier(tier);
            
            for (int j=0; j<6; j++) {
                String rewardName = relicRewards.get(i+j+1).select("td").get(0).text();
                String rarityString = relicRewards.get(i+j+1).select("td").get(1).text().split(" ")[0];
                
                RewardDTO reward = new RewardDTO();
                reward.setReward(rewardName);
                reward.setRarity(Rarity.valueOf(rarityString.toUpperCase()));
                relicDrop.getDrops().add(reward);
            }
            
            relics.put(relicKey, relicDrop);
        }
        
        return relics.values().stream().collect(Collectors.toList());
    }
    
    private List<MissionDTO> getMissionsHtml(Elements missionElements) {
        
        List<MissionDTO> missions = new ArrayList<>();
        MissionDTO currentMission = new MissionDTO();
        missions.add(currentMission);
        String currentRotation = "G";
        
        for (Element element : missionElements) {
            String text = element.text();
            if (text.equals("")) {
                currentMission = new MissionDTO();
                missions.add(currentMission);
                currentRotation = "G";
                continue;
            }
            
            if (currentMission.getName() == null) {
                currentMission.setName(text);
                continue;
            }
            
            if (text.equals("Rotation A")) {
                currentRotation = "A";
                continue;
            } else if (text.equals("Rotation B")) {
                currentRotation = "B";
                continue;
            } else if (text.equals("Rotation C")) {
                currentRotation = "C";
                continue;
            }
            
            // finally a real reward and not mission name or rotation
            Elements rewards = element.select("td");
            String item = rewards.first().text();
            String dropChance = rewards.last().text().replace("Very ", "").replace("Ultra ", "");
            Rarity rarity = Rarity.valueOf(dropChance.split(" ")[0].toUpperCase());
            Double chance = Double.parseDouble(dropChance.split(" ")[1].replace("(", "").replace("%)", ""));
            
            RewardDTO reward = new RewardDTO();
            reward.setReward(item);
            reward.setRarity(rarity);
            reward.getChance().add(chance);
            
            if (currentRotation.equals("G")) {
                currentMission.getRewardsRotationGeneral().add(reward);
            } else if (currentRotation.equals("A")) {
                currentMission.getRewardsRotationA().add(reward);
            } else if (currentRotation.equals("B")) {
                currentMission.getRewardsRotationB().add(reward);
            } else if (currentRotation.equals("C")) {
                currentMission.getRewardsRotationC().add(reward);
            }
        }
        
        return missions;
    }
    
    CloseableHttpClient getClient() {
        return HttpClients.createDefault();
    }
    
    String getHttpContent(CloseableHttpResponse response) throws IOException {
        return EntityUtils.toString(response.getEntity(), Charset.forName("UTF-8"));
    }
}
