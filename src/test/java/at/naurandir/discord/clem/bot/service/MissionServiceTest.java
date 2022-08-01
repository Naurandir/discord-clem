package at.naurandir.discord.clem.bot.service;

import at.naurandir.discord.clem.bot.model.mission.Mission;
import at.naurandir.discord.clem.bot.model.mission.MissionMapper;
import at.naurandir.discord.clem.bot.model.mission.MissionReward;
import at.naurandir.discord.clem.bot.repository.MissionRepository;
import at.naurandir.discord.clem.bot.repository.MissionRewardRepository;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 *
 * @author Naurandir
 */
@ExtendWith(MockitoExtension.class)
public class MissionServiceTest extends BaseServiceTest {
    
    @Spy
    private MissionMapper missionMapper;

    @Spy
    private MissionRepository missionRepository;
    
    @Mock
    private MissionRewardRepository missionRewardRepository;

    @InjectMocks
    private MissionService missionService;

    private Mission mission;
    private MissionReward missionReward;
    
    private final int numberOfMissions = 20; // 20 missions exists in the droptable.html
    
    @Override
    @BeforeEach
    public void setup() throws IOException, URISyntaxException {
        super.setup();
        
        mission = new Mission();
        mission.setId(815L);
        mission.setFaction("any");
        mission.setMinLevelEnemy(1);
        mission.setMaxLevelEnemy(10);
        mission.setName("Mission");
        
        missionReward = new MissionReward();
        mission.setAllRewards(new HashSet<>());
        mission.getAllRewards().add(missionReward);
        
        when(missionMapper.missionDtoToMission(any())).thenReturn(mission);
        when(missionRepository.save(any())).thenReturn(mission);
        
        setWarframeClientGetRawMock("dto/droptable.html");
    }
    
    @Test
    public void shouldSyncNewMissions() throws IOException {
        missionService.sync();
        
        verify(missionMapper, times(numberOfMissions)).missionDtoToMission(any());
    }
    
    @Test
    public void shouldSyncExistingMissions() throws IOException {
        when(missionRepository.findByEndDateIsNull()).thenReturn(List.of(mission));
        mission.setName("Venus/Bifrost Echo (Skirmish)");
        
        missionService.sync();
        
        verify(missionMapper, times(1)).updateMission(any(), any());
        Assertions.assertTrue(() -> mission.getEndDate() == null);
    }
    
    @Test
    public void shouldSyncExpiredMissions() throws IOException {
        when(missionRepository.findByEndDateIsNull()).thenReturn(List.of(mission));
        mission.setName("An Old Mission");
        
        missionService.sync();
        
        verify(missionMapper, times(numberOfMissions)).missionDtoToMission(any());
        verify(missionMapper, times(0)).updateMission(any(), any());
        verify(missionRepository, times(1)).saveAll(any());
        
        Assertions.assertTrue(() -> mission.getEndDate() != null);
    }
}
