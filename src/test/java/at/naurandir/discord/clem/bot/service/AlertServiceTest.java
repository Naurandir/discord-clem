package at.naurandir.discord.clem.bot.service;

import at.naurandir.discord.clem.bot.model.alert.Alert;
import at.naurandir.discord.clem.bot.model.alert.AlertMapper;
import at.naurandir.discord.clem.bot.repository.AlertRepository;
import at.naurandir.discord.clem.bot.service.client.dto.worldstate.AlertDTO;
import java.io.IOException;
import java.time.LocalDateTime;
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
public class AlertServiceTest extends BaseServiceTest {
    
    @Spy
    private AlertMapper alertMapper;
    
    @Spy
    private AlertRepository alertRepository;

    @InjectMocks
    private AlertService alertService;
    
    private Alert alert;
    
    private final int numberOfAlers = 3; // 3 are in the json currently
    
    @Override
    @BeforeEach
    public void setup() throws IOException {
        alert = new Alert();
        alert.setId(815L);
        alert.setExternalId("anyId");
        alert.setExpiry(LocalDateTime.now().plusDays(1));
        
        when(alertMapper.alertDtoToAlert(any())).thenReturn(alert);
        
        when(alertRepository.findByEndDateIsNull()).thenReturn(new ArrayList<>());
        when(alertRepository.save(any())).thenReturn(alert);
        
        setWarframeClientGetListMock("dto/alert.json", AlertDTO.class);
    }
    
    @Test
    public void shouldSyncNewAlerts() throws IOException {
        alertService.syncAlerts();
        
        verify(alertRepository, times(numberOfAlers)).save(any());
        verify(alertMapper, times(numberOfAlers)).alertDtoToAlert(any());
    }
    
    @Test
    public void shouldSyncExistingAlertsNothingToDo() throws IOException {
        when(alertRepository.findByEndDateIsNull()).thenReturn(List.of(alert));
        alert.setExternalId("62c48b8e5d6c4432ac0da741");
        
        alertService.syncAlerts();
        
        verify(alertRepository, times(numberOfAlers - 1)).save(any());
        verify(alertMapper, times(numberOfAlers - 1)).alertDtoToAlert(any());
    }
    
    @Test
    public void shouldSyncExpiredAlerts() throws IOException {
        when(alertRepository.findByEndDateIsNull()).thenReturn(List.of(alert));
        alert.setExternalId("oldId");
        alert.setExpiry(LocalDateTime.now().minusDays(1));
        
        alertService.syncAlerts();
        
        verify(alertRepository, times(numberOfAlers + 1)).save(any());
        verify(alertMapper, times(numberOfAlers)).alertDtoToAlert(any());
        Assertions.assertTrue(() -> alert.getEndDate() != null);
    }
}
