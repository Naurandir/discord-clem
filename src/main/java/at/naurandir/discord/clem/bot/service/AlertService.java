package at.naurandir.discord.clem.bot.service;

import at.naurandir.discord.clem.bot.model.alert.Alert;
import at.naurandir.discord.clem.bot.model.alert.AlertMapper;
import at.naurandir.discord.clem.bot.repository.AlertRepository;
import at.naurandir.discord.clem.bot.service.client.WarframeClient;
import at.naurandir.discord.clem.bot.service.client.dto.worldstate.AlertDTO;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 *
 * @author Naurandir
 */
@Slf4j
@Service
public class AlertService extends SyncService {
    
    @Autowired
    private AlertMapper alertMapper;
    
    @Autowired
    private AlertRepository alertRepository;
    
    @Autowired
    private WarframeClient warframeClient;
    
    @Autowired
    private UpdatePipelineService updatePipelineService;
    
    @Value( "${discord.clem.alert.url}" )
    private String apiUrl;
    
    @Value("#{${discord.clem.alert.headers}}")
    private Map<String, String> apiHeaders;
    
    @Override
    @Scheduled(cron = "${discord.clem.alert.scheduler.cron}")
    public void sync() {
        try {
            doSync();
        } catch (Exception ex) {
            log.error("sync:_throwed error: ", ex);
        }
    }
    
    @Override
    public void doSync() throws IOException {
        List<AlertDTO> currentAlerts = warframeClient.getListData(apiUrl, apiHeaders, AlertDTO.class);
        List<Alert> dbAlerts = alertRepository.findByEndDateIsNull();
        LocalDateTime now = LocalDateTime.now();
        
        // add new alerts
        for (AlertDTO alert : currentAlerts) {
            boolean isInDatabase = dbAlerts.stream()
                    .anyMatch(dbAlert -> dbAlert.getExternalId().equals(alert.getId()));
            
            if (isInDatabase) {
                log.debug("syncAlerts: alert [{}] already exists in database", alert.getId());
                continue; // nothing to do
            }
            
            Alert newAlert = alertMapper.alertDtoToAlert(alert);
            
            if (now.isAfter(newAlert.getExpiry())) {
                newAlert.setEndDate(now);
            }
            
            newAlert = alertRepository.save(newAlert);
            log.info("syncAlerts: added new alert [{} - {}]", newAlert.getId(), newAlert.getExternalId());
            
            updatePipelineService.addAlertNotify(newAlert);
        }
        
        // inactivate old alerts
        for (Alert alert : dbAlerts) {
            if (now.isAfter(alert.getExpiry())) {
                alert.setModifyDate(now);
                alert.setEndDate(now);
                
                alertRepository.save(alert);
                log.info("syncAlerts: alert [{}] expired, set end date to now.", alert.getId());
            }
        }
    }
    
    public List<Alert> getActiveAlerts() {
        return alertRepository.findByEndDateIsNull();
    }

    @Override
    boolean isFirstTimeStartup() {
        return alertRepository.findByEndDateIsNull().isEmpty();
    }
}
