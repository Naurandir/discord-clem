package at.naurandir.discord.clem.bot.service;

import at.naurandir.discord.clem.bot.model.Alert;
import at.naurandir.discord.clem.bot.model.AlertMapper;
import at.naurandir.discord.clem.bot.repository.AlertRepository;
import at.naurandir.discord.clem.bot.service.client.WarframeClient;
import at.naurandir.discord.clem.bot.service.client.dto.worldstate.AlertDTO;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Naurandir
 */
@Slf4j
@Service
public class AlertService {
    
    private final AlertMapper mapper;
    private final AlertRepository alertRepository;
    private final WarframeClient warframeClient;
    
    private final String apiUrl;
    private final Map<String, String> apiHeaders;
    
    @Autowired
    public AlertService(AlertRepository alertRepository, AlertMapper mapper) {
        this.alertRepository = alertRepository;
        this.mapper = mapper;
        this.warframeClient = new WarframeClient();
        
        this.apiUrl = "https://api.warframestat.us/pc/alerts";
        
        Map<String, String> tempHeaders = new HashMap<>();
        tempHeaders.put("Accept-Language", "en");
        this.apiHeaders = Collections.unmodifiableMap(tempHeaders);
    }
    
    @Transactional
    public void syncAlerts() throws IOException {
        List<AlertDTO> currentAlerts = warframeClient.getData(apiUrl, apiHeaders);
        List<Alert> alertsDB = alertRepository.findByEndDateIsNull();
        
        LocalDateTime now = LocalDateTime.now();
        
        // add new alerts
        for (AlertDTO alert : currentAlerts) {
            boolean isInDatabase = alertsDB.stream()
                    .anyMatch(alertDB -> alertDB.getExternalId().equals(alert.getId()));
            
            if (isInDatabase) {
                log.debug("syncAlerts: alert [{}] already exists in database", alert.getId());
                continue; // nothing to do
            }
            
            log.debug("syncAlerts: alert [{}] not existing, adding to database...", alert.getId());
            Alert newAlert = mapper.alertDtoToAlert(alert);
            newAlert.setNotifiedActivation(false);
            newAlert.setNotifiedExpiry(false);
            
            newAlert.setStartDate(now);
            newAlert.setModifyDate(now);
            
            if (now.isAfter(newAlert.getExpiry())) {
                newAlert.setEndDate(now);
            }
            
            newAlert = alertRepository.save(newAlert);
            log.info("syncAlerts: alert [{}] added to database, db id [{}]", alert.getId(), newAlert.getId());
        }
        
        // inactivate
        for (Alert alert : alertsDB) {
            if (now.isAfter(alert.getExpiry())) {
                alert.setModifyDate(now);
                alert.setEndDate(now);
                
                alertRepository.save(alert);
                log.info("syncAlerts: alert [{}] expired, set end date to now.", alert.getId());
            }
        }
    }
    
    @Transactional
    public List<Alert> getActiveAlerts() {
        return alertRepository.findByEndDateIsNull();
    }
    
    private List<Alert> findOldInactiveAlerts() {
        return alertRepository.findByEndDateAfter(LocalDateTime.now().minusDays(31));
    }
}
