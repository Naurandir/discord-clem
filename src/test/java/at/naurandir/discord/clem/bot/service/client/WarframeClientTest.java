package at.naurandir.discord.clem.bot.service.client;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Optional;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
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

//    @Mock
//    private CloseableHttpClient httpClient;
//    @Mock
//    private CloseableHttpResponse response;
//    
//    private String httpContent = "";
//
    private WarframeClient warframeClient;
//
//    @BeforeEach
//    public void setup() throws IOException {
//        warframeClient = new WarframeClient() {
//            @Override
//            CloseableHttpClient getClient() {
//                return httpClient;
//            }
//
//            @Override
//            String getHttpContent(CloseableHttpResponse response) throws IOException {
//                return httpContent;
//            }
//        };
//
//        when(httpClient.execute(any())).thenReturn(response);
//    }

//    @Test
//    public void shouldParseRelicsDropTableFromHtml() throws IOException {
//        RelicDropDTO expected = new RelicDropDTO();
//        expected.setTier("Axi");
//        expected.setRelicName("A1");
//        expected.setState(RelicState.INTACT);
//        
//        RewardDropDTO reward = new RewardDropDTO();
//        reward.setItemName("Akstiletto Prime Barrel");
//        reward.setRarity(Rarity.UNCOMMON);
//        reward.setChance(11d);
//        
//        expected.setRewards(List.of(reward));
//        
//        httpContent = "<pre class='mw-code mw-script' dir='ltr'>" +
//                "return'{&quot;relics&quot;:[{&quot;tier&quot;:&quot;Axi&quot;,&quot;relicName&quot;:&quot;A1&quot;,&quot;state&quot;:&quot;Intact&quot;,&quot;rewards&quot;:[{&quot;_id&quot;:&quot;6733cc5298452209aa29dd72027c7df1&quot;,&quot;itemName&quot;:&quot;Akstiletto Prime Barrel&quot;,&quot;rarity&quot;:&quot;Uncommon&quot;,&quot;chance&quot;:11}],&quot;_id&quot;:&quot;3cbd889de3a944c0b8840f7300db909a&quot;}]}'"
//                +"</pre>";
//        
//        DropTableDTO dto = warframeClient.getDropTableWithRelics();
//        
//        assertEquals(1, dto.getRelics().size());
//        assertEquals(expected, dto.getRelics().get(0));
//    }
//    
//    @Test
//    public void shouldParsemissionsDropTableFromHtml() throws IOException {
//        Map<String,Map<String,MissionDropDTO>> expected = new HashMap<>();
//        Map<String,MissionDropDTO> nodeMissionDrop = new HashMap<>();
//        
//        RewardDropDTO reward = new RewardDropDTO();
//        reward.setChance(50d);
//        reward.setItemName("2,000 Credits Cache");
//        reward.setRarity(Rarity.COMMON);
//        
//        MissionRewardDropDTO missionReward = new MissionRewardDropDTO();
//        missionReward.setA(List.of(reward));
//        missionReward.setB(List.of(reward));
//        missionReward.setC(List.of(reward));
//        missionReward.setGeneralRewards(List.of());
//        
//        MissionDropDTO missionDrop = new MissionDropDTO();
//        missionDrop.setGameMode("Survival");
//        missionDrop.setIsEvent(false);        
//        missionDrop.setMissionRewards(missionReward);
//        
//        nodeMissionDrop.put("Apollodorus", missionDrop);
//        expected.put("Mercury", nodeMissionDrop);
//        
//        httpContent = "<pre class='mw-code mw-script' dir='ltr'>" +
//                "return[[{&quot;missionRewards&quot;:{&quot;Mercury&quot;:{&quot;Apollodorus&quot;:{&quot;gameMode&quot;:&quot;Survival&quot;,&quot;isEvent&quot;:false,&quot;rewards&quot;:{&quot;A&quot;:[{&quot;_id&quot;:&quot;83b8ed8608ef404c5ceeef9ef2906af1&quot;,&quot;itemName&quot;:&quot;2,000 Credits Cache&quot;,&quot;rarity&quot;:&quot;Common&quot;,&quot;chance&quot;:50}],&quot;B&quot;:[{&quot;_id&quot;:&quot;83b8ed8608ef404c5ceeef9ef2906af1&quot;,&quot;itemName&quot;:&quot;2,000 Credits Cache&quot;,&quot;rarity&quot;:&quot;Common&quot;,&quot;chance&quot;:50}],&quot;C&quot;:[{&quot;_id&quot;:&quot;83b8ed8608ef404c5ceeef9ef2906af1&quot;,&quot;itemName&quot;:&quot;2,000 Credits Cache&quot;,&quot;rarity&quot;:&quot;Common&quot;,&quot;chance&quot;:50}]}}}}}]]"
//                +"</pre>";
//        
//        DropTableDTO dto = warframeClient.getMissionsHtml();
//        
//        assertEquals(1, dto.getMissionRewards().size());
//        assertEquals(expected, dto.getMissionRewards());
//    }
    
//    @Test
//    public void realCall() throws IOException {
//        warframeClient = new WarframeClient();
//        warframeClient.getCurrentBuilds();
//    }
    
}
