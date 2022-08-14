package at.naurandir.discord.clem.bot.service;

import at.naurandir.discord.clem.bot.model.fissure.VoidFissure;
import at.naurandir.discord.clem.bot.model.fissure.VoidFissureMapper;
import at.naurandir.discord.clem.bot.repository.VoidFissureRepository;
import at.naurandir.discord.clem.bot.service.client.WarframeClient;
import at.naurandir.discord.clem.bot.service.client.dto.worldstate.VoidFissureDTO;
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
public class VoidFissureService extends SyncService {
    
    @Autowired
    private VoidFissureMapper voidFissureMapper;
    
    @Autowired
    private VoidFissureRepository voidFissureRepository;

    @Value( "${discord.clem.void_fissure.url}" )
    private String apiUrl;
    
    @Value("#{${discord.clem.void_fissure.headers}}")
    private Map<String, String> apiHeaders;
    
    private final WarframeClient warframeClient = new WarframeClient();
    
    @Override
    @Scheduled(cron = "${discord.clem.void_fissure.scheduler.cron}")
    public void sync() {
        try {
            doSync();
        } catch (Exception ex) {
            log.error("sync:_throwed error: ", ex);
        }
    }
    
    @Override
    public void doSync() throws IOException {
        List<VoidFissureDTO> currentFissures = warframeClient.getListData(apiUrl, apiHeaders, VoidFissureDTO.class);
        List<VoidFissure> fissuresDb = voidFissureRepository.findByEndDateIsNull();
        
        LocalDateTime now = LocalDateTime.now();
        
        // add new / update
        for (VoidFissureDTO fissure : currentFissures) {
            boolean isInDatabase = fissuresDb.stream()
                    .anyMatch(dbFissure -> dbFissure.getExternalId().equals(fissure.getId()));
            
            if (isInDatabase) {
                continue; // nothing to do
            }
            
            VoidFissure newFissure = voidFissureMapper.voidFissureDtoToVoidFissure(fissure);
            
            newFissure.setStartDate(now);
            newFissure.setModifyDate(now);
            
            if (now.isAfter(newFissure.getExpiry())) {
                newFissure.setEndDate(now);
            }
            
            newFissure = voidFissureRepository.save(newFissure);
            log.info("syncFissures: added new fissure [{} - {}]", fissure.getId(), newFissure.getId());
        }
        
        // remove old
        for (VoidFissure fissure : fissuresDb) {
            if (now.isAfter(fissure.getExpiry())) {
                fissure.setModifyDate(now);
                fissure.setEndDate(now);
                
                voidFissureRepository.save(fissure);
                log.info("syncFissures: fissure [{}] expired, set end date to now.", fissure.getId());
            }
        }
    }
    
    public List<VoidFissure> getNormalVoidFissures() {
        return voidFissureRepository.findByEndDateIsNullAndIsStorm(false);
    }
    
    public List<VoidFissure> getStormVoidFissures() {
        return voidFissureRepository.findByEndDateIsNullAndIsStorm(true);
    }
    
    @Override
    boolean isFirstTimeStartup() {
        return voidFissureRepository.findByEndDateIsNullAndIsStorm(false).isEmpty() &&
                voidFissureRepository.findByEndDateIsNullAndIsStorm(true).isEmpty();
    }
}
