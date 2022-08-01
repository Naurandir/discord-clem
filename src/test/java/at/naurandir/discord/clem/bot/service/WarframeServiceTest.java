package at.naurandir.discord.clem.bot.service;

import at.naurandir.discord.clem.bot.model.item.Warframe;
import at.naurandir.discord.clem.bot.model.item.WarframeMapper;
import at.naurandir.discord.clem.bot.repository.WarframeRepository;
import at.naurandir.discord.clem.bot.service.client.dto.worldstate.WarframeDTO;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
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
public class WarframeServiceTest extends BaseServiceTest {
    
    @Spy
    private WarframeMapper warframeMapper;
    
    @Spy
    private WarframeRepository warframeRepository;

    @InjectMocks
    private WarframeService warframeService;
    
    private Warframe warframe;
    
    private final int numberOfWarframes = 88; // 88 are in the json currently
    
    @Override
    @BeforeEach
    public void setup() throws IOException {
        warframe = new Warframe();
        warframe.setId(815L);
        warframe.setName("Any");
        warframe.setUniqueName("Any");
        
        when(warframeMapper.fromDtoToWarframe(any())).thenReturn(warframe);
        
        when(warframeRepository.findByEndDateIsNull()).thenReturn(new ArrayList<>());
        when(warframeRepository.save(any())).thenReturn(warframe);
        
        setWarframeClientGetListMock("dto/warframes.json", WarframeDTO.class);
    }
    
    @Test
    public void shouldSyncNewWarframes() throws IOException {
        warframeService.sync();
        
        verify(warframeRepository, times(numberOfWarframes)).save(any());
        verify(warframeMapper, times(numberOfWarframes)).fromDtoToWarframe(any());
    }
    
    @Test
    public void shouldSyncExistingWarframesNothingToDo() throws IOException {
        when(warframeRepository.findByEndDateIsNull()).thenReturn(List.of(warframe));
        warframe.setUniqueName("/Lotus/Powersuits/Bard/Bard");
        
        warframeService.sync();
        
        verify(warframeRepository, times(numberOfWarframes - 1)).save(any());
        verify(warframeMapper, times(numberOfWarframes - 1)).fromDtoToWarframe(any());
    }
    
    @Test
    public void shouldSyncExpiredWarframes() throws IOException {
        when(warframeRepository.findByEndDateIsNull()).thenReturn(List.of(warframe));
        warframe.setName("oldWarframe");
        
        warframeService.sync();
        
        verify(warframeRepository, times(numberOfWarframes)).save(any());
        verify(warframeMapper, times(numberOfWarframes)).fromDtoToWarframe(any());
        Assertions.assertTrue(() -> warframe.getEndDate() != null);
    }
}
