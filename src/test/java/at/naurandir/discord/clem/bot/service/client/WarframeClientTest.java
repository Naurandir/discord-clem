package at.naurandir.discord.clem.bot.service.client;

import at.naurandir.discord.clem.bot.model.enums.Rarity;
import at.naurandir.discord.clem.bot.service.client.dto.DropTableDTO;
import at.naurandir.discord.clem.bot.service.client.dto.droptable.MissionDropDTO;
import at.naurandir.discord.clem.bot.service.client.dto.droptable.RelicDropDTO;
import at.naurandir.discord.clem.bot.service.client.dto.droptable.RewardDropDTO;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 *
 * @author Naurandir
 */
@ExtendWith(MockitoExtension.class)
public class WarframeClientTest {

    @Mock
    private CloseableHttpClient httpClient;
    @Mock
    private CloseableHttpResponse response;
    
    private String httpContent = "";

    private WarframeClient warframeClient;

    @BeforeEach
    public void setup() throws IOException {
        warframeClient = new WarframeClient() {
            @Override
            CloseableHttpClient getClient() {
                return httpClient;
            }

            @Override
            String getHttpContent(CloseableHttpResponse response) throws IOException {
                return httpContent;
            }
        };

        when(httpClient.execute(any())).thenReturn(response);
    }

    @Test
    public void shouldParseRelicsDropTableFromHtml() throws IOException {
        RelicDropDTO expected = new RelicDropDTO();
        expected.setTier("Axi");
        expected.setRelicName("A1");
        expected.setState("Intact");
        
        RewardDropDTO reward = new RewardDropDTO();
        reward.setItemName("Akstiletto Prime Barrel");
        reward.setRarity(Rarity.UNCOMMON);
        reward.setChance(11d);
        
        expected.setRewards(List.of(reward));
        
        httpContent = "<pre class='mw-code mw-script' dir='ltr'>" +
                "return'{&quot;relics&quot;:[{&quot;tier&quot;:&quot;Axi&quot;,&quot;relicName&quot;:&quot;A1&quot;,&quot;state&quot;:&quot;Intact&quot;,&quot;rewards&quot;:[{&quot;_id&quot;:&quot;6733cc5298452209aa29dd72027c7df1&quot;,&quot;itemName&quot;:&quot;Akstiletto Prime Barrel&quot;,&quot;rarity&quot;:&quot;Uncommon&quot;,&quot;chance&quot;:11}],&quot;_id&quot;:&quot;3cbd889de3a944c0b8840f7300db909a&quot;}]}'"
                +"</pre>";
        
        DropTableDTO dto = warframeClient.getDropTableWithRelics();
        
        assertEquals(1, dto.getRelics().size());
        assertEquals(expected, dto.getRelics().get(0));
    }
    
//    @Test
//    public void shouldParsemissionsDropTableFromHtml() throws IOException {
//        Map<String,Map<String,MissionDropDTO>> expected = new HashMap<>();
//        Map<String,MissionDropDTO> nodeMissionDrop = new HashMap<>();
//        Map<String, List<RewardDropDTO>> rewards = new HashMap<>();
//        
//        RewardDropDTO reward = new RewardDropDTO();
//        reward.setChance(50d);
//        reward.setItemName("2,000 Credits Cache");
//        reward.setRarity(Rarity.COMMON);
//        
//        rewards.put("A", List.of(reward));
//        
//        MissionDropDTO missionDrop = new MissionDropDTO();
//        missionDrop.setGameMode("Survival");
//        missionDrop.setIsEvent(false);
//        missionDrop.setRewards(rewards);
//        
//        nodeMissionDrop.put("Apollodorus", missionDrop);
//        expected.put("Mercury", nodeMissionDrop);
//        httpContent = "<pre class='mw-code mw-script' dir='ltr'>" +
//                "return[[{&quot;missionRewards&quot;:{&quot;Mercury&quot;:{&quot;Apollodorus&quot;:{&quot;gameMode&quot;:&quot;Survival&quot;,&quot;isEvent&quot;:false,&quot;rewards&quot;:{&quot;A&quot;:[{&quot;_id&quot;:&quot;83b8ed8608ef404c5ceeef9ef2906af1&quot;,&quot;itemName&quot;:&quot;2,000 Credits Cache&quot;,&quot;rarity&quot;:&quot;Common&quot;,&quot;chance&quot;:50}]}}}}}]]"
//                +"</pre>";
//        
//        DropTableDTO dto = warframeClient.getDropTableWithMissions();
//        
//        assertEquals(1, dto.getMissionRewards().size());
//        assertEquals(expected, dto.getMissionRewards());
//    }
}
