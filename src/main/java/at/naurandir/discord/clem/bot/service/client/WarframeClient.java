package at.naurandir.discord.clem.bot.service.client;

import at.naurandir.discord.clem.bot.model.enums.Rarity;
import at.naurandir.discord.clem.bot.service.client.dto.DropTableDTO;
import at.naurandir.discord.clem.bot.service.client.dto.MarketDTO;
import at.naurandir.discord.clem.bot.service.client.dto.WorldStateDTO;
import at.naurandir.discord.clem.bot.service.client.dto.droptable.MissionDTO;
import at.naurandir.discord.clem.bot.service.client.dto.droptable.RelicDTO;
import at.naurandir.discord.clem.bot.service.client.dto.droptable.RewardDTO;
import at.naurandir.discord.clem.bot.service.client.dto.market.MarketItemsDTO;
import at.naurandir.discord.clem.bot.service.client.dto.market.MarketLichAuctionsDTO;
import at.naurandir.discord.clem.bot.service.client.dto.market.MarketLichWeaponsDTO;
import at.naurandir.discord.clem.bot.service.client.dto.market.MarketOrdersResultDTO;
import at.naurandir.discord.clem.bot.service.client.dto.overframe.OverframeBuildDTO;
import at.naurandir.discord.clem.bot.service.client.dto.overframe.OverframeDTO;
import at.naurandir.discord.clem.bot.service.client.dto.overframe.OverframeItemDTO;
import at.naurandir.discord.clem.bot.service.client.dto.overframe.OverframeItemTierDTO;
import at.naurandir.discord.clem.bot.service.client.dto.overframe.OverframeItemTierListDTO;
import com.google.gson.Gson;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    
    public OverframeDTO getCurrentBuilds() throws IOException {
        OverframeDTO dto = new OverframeDTO();
        
        OverframeItemTierListDTO warframeTierlist = getTierList("0");
        OverframeItemTierListDTO primaryWeaponTierlist = getTierList("1");
        OverframeItemTierListDTO secondaryTierlist = getTierList("2");
        OverframeItemTierListDTO meeleTierlist = getTierList("3");
        
        List<OverframeItemDTO> warframes = new ArrayList<>();
        for (OverframeItemTierDTO itemTier : warframeTierlist.getTierList()) {
            warframes.add(getOverframeItem(itemTier.getItemId()));
        }
        
        List<OverframeItemDTO> primaryWeapons = new ArrayList<>();
        for (OverframeItemTierDTO itemTier : primaryWeaponTierlist.getTierList()) {
            primaryWeapons.add(getOverframeItem(itemTier.getItemId()));
        }
        
        List<OverframeItemDTO> secondaryWeapons = new ArrayList<>();
        for (OverframeItemTierDTO itemTier : secondaryTierlist.getTierList()) {
            secondaryWeapons.add(getOverframeItem(itemTier.getItemId()));
        }
        
        List<OverframeItemDTO> meeleWeapons = new ArrayList<>();
        for (OverframeItemTierDTO itemTier : meeleTierlist.getTierList()) {
            meeleWeapons.add(getOverframeItem(itemTier.getItemId()));
        }
        
        dto.setWarframes(warframes);
        dto.setPrimaryWeapons(primaryWeapons);
        dto.setSecondaryWeapons(secondaryWeapons);
        dto.setMeeleWeapons(meeleWeapons);
        
        return dto;
    }
    
    private OverframeItemTierListDTO getTierList(String type) throws IOException {
        try (CloseableHttpClient httpClient = getClient()) {
            HttpGet httpGet = new HttpGet("https://overframe.gg/api/v1/tierlists/" + type + "/");            
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                log.info("getTierList: received http status [{}]", response.getStatusLine());
                String jsonString = getHttpContent(response);
                return gson.fromJson(jsonString, OverframeItemTierListDTO.class);
            }
        }
    }
    
    private OverframeItemDTO getOverframeItem(Long id) throws IOException {
        OverframeItemDTO item = new OverframeItemDTO();
        item.setId(id);
        
        try (CloseableHttpClient httpClient = getClient()) {
            HttpGet httpGet = new HttpGet("https://overframe.gg/items/arsenal/" + id + "/");            
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                log.info("getOverframeItem: received http status [{}] for item id [{}]", response.getStatusLine(), id);
                
                String htmlString = getHttpContent(response);
                Document html = Jsoup.parse(htmlString);
                
                // title
                // Title: Warframe Kuva Zarr - Warframe Kuva Zarr Builds - Overframe
                item.setName(html.select("title").first().text().split(" - ")[0].replace("Warframe ", ""));
                
                // picture url
                Elements metaElements = html.select("meta");
                for (Element metaElement : metaElements) {
                    if (metaElement.attr("content").startsWith("https://overframe.gg")) {
                        item.setPictureUrl(metaElement.attr("content"));
                        break;
                    }
                }
                
                // Builds
                item.setBuilds(new ArrayList<>());
                Elements buildElements = html.select("article").get(1).getElementsByTag("a");
                for (Element build : buildElements) {
                    OverframeBuildDTO buildDTO = new OverframeBuildDTO();
                    
                    buildDTO.setUrl("https://overframe.gg" + build.attr("href"));
                    buildDTO.setVotes(build.child(2).text());
                    
                    Element buildData = build.child(1);
                    
                    buildDTO.setTitle(buildData.child(0).text());
                    buildDTO.setFormasUsed(buildData.child(2).child(0).text());
                    
                    if (buildData.child(2).childrenSize() > 1) {
                        buildDTO.setGuideLength(buildData.child(2).child(1).text());
                    } else {
                        buildDTO.setGuideLength("Normal Guide");
                    }
                    
                    item.getBuilds().add(buildDTO);
                }
                
                return item;
            }
        }
    }
    
    public DropTableDTO getCurrentDropTable() throws IOException {
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
                List<MissionDTO> missions = getMissions(missionElements);
                
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
            String tier = titleSplitted[0];
            String name = titleSplitted[1];
            String relicKey = titleSplitted[1] + " " + titleSplitted[0];
            
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
    
    private List<MissionDTO> getMissions(Elements missionElements) {
        
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
