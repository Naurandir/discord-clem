package at.naurandir.discord.clem.bot.service;

import at.naurandir.discord.clem.bot.model.enums.Rarity;
import at.naurandir.discord.clem.bot.model.enums.RelicTier;
import at.naurandir.discord.clem.bot.model.relic.Relic;
import at.naurandir.discord.clem.bot.model.relic.RelicDrop;
import at.naurandir.discord.clem.bot.model.relic.RelicMapper;
import at.naurandir.discord.clem.bot.repository.RelicDropRepository;
import at.naurandir.discord.clem.bot.repository.RelicRepository;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
public class RelicServiceTest extends BaseServiceTest {
    
    @Spy
    private RelicMapper relicMapper;
    
    @Spy
    private RelicRepository relicRepository;
    
    @Mock
    private RelicDropRepository relicDropRepository;

    @InjectMocks
    private RelicService relicService;
    
    private Relic relic;
    private Set<RelicDrop> drops;
    
    private final int numberOfRelics = 47; // 47 relics currently in droptable.html
    
    @Override
    @BeforeEach
    public void setup() throws IOException, URISyntaxException {
        relic = new Relic();
        relic.setId(815L);
        relic.setExternalId("anyId");
        relic.setName("any");
        relic.setTier(RelicTier.LITH);
        
        RelicDrop drop = new RelicDrop();
        drop.setRarity(Rarity.RARE);
        drop.setName("anyReward");
        drop.setReward("anyReward");
        
        drops = new HashSet<>();
        drops.add(drop);        
        relic.setDrops(drops);
        
        when(relicMapper.fromDtoToRelic(any())).thenReturn(relic);
        
        when(relicRepository.findAll()).thenReturn(new ArrayList<>());
        when(relicRepository.save(any())).thenReturn(relic);
        
        setWarframeClientGetRawMock("dto/droptable.html");
    }
    
    @Test
    public void shouldSyncNewRelics() throws IOException {
        relicService.sync();
        
        verify(relicRepository, times(numberOfRelics)).save(any());
        verify(relicMapper, times(numberOfRelics)).fromDtoToRelic(any());
    }
    
    @Test
    public void shouldSyncExistingRelicsNothingToDo() throws IOException {
        when(relicRepository.findAll()).thenReturn(List.of(relic));
        relic.setName("E2");
        relic.setTier(RelicTier.NEO);
        
        relicService.sync();
        
        verify(relicMapper, times(1)).updateRelic(any(), any());
        verify(relicMapper, times(numberOfRelics - 1)).fromDtoToRelic(any());
    }
}
