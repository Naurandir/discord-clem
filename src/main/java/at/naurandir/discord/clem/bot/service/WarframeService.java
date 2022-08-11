package at.naurandir.discord.clem.bot.service;

import at.naurandir.discord.clem.bot.model.item.MarketType;
import at.naurandir.discord.clem.bot.model.item.Warframe;
import at.naurandir.discord.clem.bot.model.item.WarframeMapper;
import at.naurandir.discord.clem.bot.repository.WarframeRepository;
import at.naurandir.discord.clem.bot.service.client.WarframeClient;
import at.naurandir.discord.clem.bot.service.client.dto.market.MarketItemDTO;
import at.naurandir.discord.clem.bot.service.client.dto.worldstate.WarframeDTO;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
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
public class WarframeService extends SyncService {

    @Autowired
    private WarframeMapper warframeMapper;
    
    @Autowired
    private WarframeRepository warframeRepository;
    
    @Autowired
    private WarframeClient warframeClient;
    
    @Value( "${discord.clem.warframe.url}" )
    private String apiUrlWarframe;
    
    @Value("#{${discord.clem.warframe.headers}}")
    private Map<String, String> apiHeaders;
    
    @Override
    @Scheduled(cron = "${discord.clem.warframe.scheduler.cron}")
    public void sync() throws IOException {
        List<Warframe> warframesDb = warframeRepository.findByEndDateIsNull();
        List<String> warframeNames = warframesDb.stream().map(warframe -> warframe.getUniqueName()).collect(Collectors.toList());
        log.debug("syncWarframes: received [{}] warframes from database.", warframesDb.size());
        
        List<WarframeDTO> warframeDTOs = warframeClient.getListData(apiUrlWarframe, apiHeaders, WarframeDTO.class);
        warframeDTOs = warframeDTOs.stream()
                .filter(warframe -> "Warframes".equals(warframe.getCategory()))
                .collect(Collectors.toList());
        List<String> warframeDtoNames = warframeDTOs.stream().map(warframe -> warframe.getUniqueName()).collect(Collectors.toList());
        List<WarframeDTO> newWarframeDTOs = warframeDTOs.stream()
                .filter(warframe -> !warframeNames.contains(warframe.getUniqueName()))
                .collect(Collectors.toList());
        List<WarframeDTO> updateWarframeDTOs = warframeDTOs.stream()
                .filter(warframe -> warframeNames.contains(warframe.getUniqueName()))
                .collect(Collectors.toList());
        log.info("syncWarframes: received [{}] warframes, [{}] are new, [{}] can be updated.", 
                warframeDTOs.size(), newWarframeDTOs.size(), updateWarframeDTOs.size());
        
        // add
        for (WarframeDTO newWarframeDTO : newWarframeDTOs) {
            Warframe newWarframe = warframeMapper.fromDtoToWarframe(newWarframeDTO);
            newWarframe = warframeRepository.save(newWarframe);
            log.info("syncWarframes: added new warframe [{} - {}]", newWarframe.getId(), newWarframe.getName());
        }
        
        // update
        for (WarframeDTO updateWarframeDTO : updateWarframeDTOs) {
            Warframe warframeDb = warframesDb.stream()
                    .filter(warframe -> warframe.getUniqueName().equals(updateWarframeDTO.getUniqueName()))
                    .findFirst()
                    .get();
            
            warframeMapper.updateWarframe(warframeDb, updateWarframeDTO);
        }
        warframeRepository.saveAll(warframesDb);
        
        // delete
        List<Warframe> toInactivateWarframesDb = new ArrayList<>();
        for (Warframe warframeDb : warframesDb) {
            if (!warframeDtoNames.contains(warframeDb.getUniqueName())) {
                toInactivateWarframesDb.add(warframeDb);
                warframeDb.setEndDate(LocalDateTime.now());
            }
        }
        warframeRepository.saveAll(toInactivateWarframesDb);
    }
    
    public List<Warframe> getWarframes() {
        return warframeRepository.findByEndDateIsNull();
    }
    
    public List<Warframe> getWarframesByMarketType(MarketType type) {
        return warframeRepository.findByMarketTypeAndEndDateIsNull(type);
    }
    
    public List<Warframe> findWarframesByName(String name) {
        return warframeRepository.findByNameContainingIgnoreCaseAndEndDateIsNull(name);
    }
    
    public List<Warframe> getWarframesByNameAndMarketType(String name, MarketType type) {
        return warframeRepository.findByNameContainingIgnoreCaseAndMarketTypeAndEndDateIsNull(name, type);
    }
    
    void updateWikiThumbnail(Warframe warframe, String pictureUrl) {
        Optional<Warframe> foundWarframe = warframeRepository.findById(warframe.getId());
        if (foundWarframe.isEmpty()) {
            log.warn("updateWikiThumbnail: cannot update thumbnail of warframe [{} - {}], as reload of warframe did not work",
                    warframe.getId(), warframe.getName());
            return;
        }
        
        foundWarframe.get().setWikiaThumbnail(pictureUrl);
        warframeRepository.save(foundWarframe.get());
    }
    
    @Override
    boolean isFirstTimeStartup() {
        return warframeRepository.findByEndDateIsNull().isEmpty();
    }

    void addMarketData(Warframe warframe, MarketItemDTO warframeItem, MarketType marketType) {
        warframeMapper.addMarketInfo(warframe, warframeItem);
        warframe.setMarketType(marketType);
    }

    Warframe save(Warframe warframe) {
        return warframeRepository.save(warframe);
    }
}
