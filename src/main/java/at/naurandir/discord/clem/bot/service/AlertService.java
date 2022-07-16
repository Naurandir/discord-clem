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
import org.springframework.stereotype.Service;

/**
 *
 * @author Naurandir
 */
@Slf4j
@Service
public class AlertService {
    
    @Autowired
    private AlertMapper mapper;
    
    @Autowired
    private AlertRepository alertRepository;
    
    @Value( "${discord.clem.alert.url}" )
    private String apiUrl;
    
    @Value("#{${discord.clem.alert.headers}}")
    private Map<String, String> apiHeaders;
    
    private final WarframeClient warframeClient = new WarframeClient();
    
    public void syncAlerts() throws IOException {
        List<AlertDTO> currentAlerts = warframeClient.getListData(apiUrl, apiHeaders);
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
            
            log.debug("syncAlerts: alert [{}] not existing, adding to database...", alert.getId());
            Alert newAlert = mapper.alertDtoToAlert(alert);
            
            newAlert.setStartDate(now);
            newAlert.setModifyDate(now);
            
            if (now.isAfter(newAlert.getExpiry())) {
                newAlert.setEndDate(now);
            }
            
            // add mission
//            Mission alertMission = new Mission();
//            alertMission.setNode(alert.getMission().getNode());
//            alertMission.setType(alert.getMission().getType());
//            alertMission.setFaction(alert.getMission().getFaction());
//            alertMission.setMinEnemyLevel(alert.getMission().getMinEnemyLevel());
//            alertMission.setMaxEnemyLevel(alert.getMission().getMaxEnemyLevel());
//            
//            newAlert.setAlertMission(alertMission);
            
            newAlert = alertRepository.save(newAlert);
            log.info("syncAlerts: alert [{}] added to database, db id [{}]", alert.getId(), newAlert.getId());
        }
        
        // remove old alerts
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
}
