package at.naurandir.discord.clem.bot.service;

import at.naurandir.discord.clem.bot.model.enums.Rarity;
import at.naurandir.discord.clem.bot.model.relic.Relic;
import at.naurandir.discord.clem.bot.model.relic.RelicMapper;
import at.naurandir.discord.clem.bot.repository.RelicDropRepository;
import at.naurandir.discord.clem.bot.repository.RelicRepository;
import at.naurandir.discord.clem.bot.service.client.WarframeClient;
import at.naurandir.discord.clem.bot.service.client.dto.droptable.RelicDTO;
import at.naurandir.discord.clem.bot.service.client.dto.droptable.RewardDTO;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
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
public class RelicService extends SyncService {

    @Autowired
    private RelicMapper relicMapper;
    
    @Autowired
    private RelicRepository relicRepository;
    
    @Autowired
    private RelicDropRepository relicDropRepository;
    
    @Autowired
    private WarframeClient warframeClient;
    
    @Value( "${discord.clem.relic.url}" )
    private String apiUrl;
    
    @Value("#{${discord.clem.relic.headers}}")
    private Map<String, String> apiHeaders;
    
    @Override
    @Transactional
    @Scheduled(cron = "${discord.clem.relic.scheduler.cron}")
    public void sync() throws IOException {
        List<Relic> relicsDb = relicRepository.findAll();
        List<String> relicNames = relicsDb.stream()
                .map(relic -> relic.getName() + relic.getTier().getTierString())
                .collect(Collectors.toList());
        
        Set<RelicDTO> relicDtos = getRelicDTOs();
        List<RelicDTO> newRelicDTOs = relicDtos.stream()
                .filter(relic -> !relicNames.contains(relic.getName() + relic.getTier()))
                .collect(Collectors.toList());
        List<RelicDTO> updateRelicDTOs = relicDtos.stream()
                .filter(relic -> relicNames.contains(relic.getName() + relic.getTier()))
                .collect(Collectors.toList());
        log.info("syncRelics: received [{}] relics, [{}] are new, [{}] can be updated.", 
                relicDtos.size(), newRelicDTOs.size(), updateRelicDTOs.size());        
        
        // add
        for (RelicDTO newRelicDTO : newRelicDTOs) {
            Relic newRelic = relicMapper.fromDtoToRelic(newRelicDTO);
            Relic relicDb = relicRepository.save(newRelic);
            
            relicDb.getDrops().forEach(drop -> drop.setRelic(relicDb));
            relicDropRepository.saveAll(relicDb.getDrops());
            
            log.info("syncRelics: added new relic [{} - {} {}]", relicDb.getId(), relicDb.getName(), relicDb.getTier());
        }
        
        // update
        for (RelicDTO updateRelicDTO : updateRelicDTOs) {
            Relic relicDb = relicsDb.stream()
                    .filter(relic -> relic.getName().equals(updateRelicDTO.getName()) &&
                                     relic.getTier().getTierString().equals(updateRelicDTO.getTier()))
                    .findFirst()
                    .get();
            
            // delete old relic drops
            relicDb.getDrops().clear();
            relicDropRepository.deleteByRelic(relicDb);
            
            // update
            relicMapper.updateRelic(relicDb, updateRelicDTO);
            relicRepository.save(relicDb);
            
            relicDb.getDrops().forEach(drop -> drop.setRelic(relicDb));
            relicDropRepository.saveAll(relicDb.getDrops());
        }        
    }

    private Set<RelicDTO> getRelicDTOs() throws IOException {
        String dropTableString = warframeClient.getDataRaw(apiUrl, apiHeaders);
        Document html = Jsoup.parse(dropTableString);
        Elements elements = html.select("table");

        Elements relicElements = elements.get(1).select("tr");
        return getRelicsHtml(relicElements);
    }

    private Set<RelicDTO> getRelicsHtml(Elements relicRewards) {
        Map<String, RelicDTO> relics = new HashMap<>();

        for (int i = 0; i < relicRewards.size() - 8; i += 8) {
            String[] titleSplitted = relicRewards.get(i).selectFirst("th").text().split(" ");
            String tier = titleSplitted[0];
            String name = titleSplitted[1];
            String relicKey = titleSplitted[1] + " " + titleSplitted[0];

            RelicDTO relicDrop = relics.get(relicKey);
            if (relicDrop != null) {
                continue;
            }

            relicDrop = new RelicDTO();
            relicDrop.setName(name);
            relicDrop.setTier(tier);

            for (int j = 0; j < 6; j++) {
                String rewardName = relicRewards.get(i + j + 1).select("td").get(0).text();
                String rarityString = relicRewards.get(i + j + 1).select("td").get(1).text().split(" ")[0];

                RewardDTO reward = new RewardDTO();
                reward.setReward(rewardName);
                reward.setRarity(Rarity.valueOf(rarityString.toUpperCase()));
                relicDrop.getDrops().add(reward);
            }

            relics.put(relicKey, relicDrop);
        }

        return relics.values().stream().collect(Collectors.toSet());
    }
    
    public List<Relic> getRelics() {
        return relicRepository.findByEndDateIsNull();
    }
    
    @Override
    boolean isFirstTimeStartup() {
        return relicRepository.findByEndDateIsNull().isEmpty();
    }
}
