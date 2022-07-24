package at.naurandir.discord.clem.bot.service;

import at.naurandir.discord.clem.bot.model.enums.Rarity;
import at.naurandir.discord.clem.bot.model.mission.Mission;
import at.naurandir.discord.clem.bot.model.mission.MissionMapper;
import at.naurandir.discord.clem.bot.model.mission.MissionReward;
import at.naurandir.discord.clem.bot.repository.MissionRepository;
import at.naurandir.discord.clem.bot.repository.MissionRewardRepository;
import at.naurandir.discord.clem.bot.service.client.WarframeClient;
import at.naurandir.discord.clem.bot.service.client.dto.droptable.MissionDropTableDTO;
import at.naurandir.discord.clem.bot.service.client.dto.droptable.RewardDTO;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
public class MissionService {

    @Autowired
    private MissionMapper missionMapper;

    @Autowired
    private MissionRepository missionRepository;
    
    @Autowired
    private MissionRewardRepository missionRewardRepository;
    
    @Autowired
    private WarframeClient warframeClient;

    @Value("${discord.clem.mission.url}")
    private String apiUrl;

    @Value("#{${discord.clem.mission.headers}}")
    private Map<String, String> apiHeaders;

    @Transactional
    @Scheduled(cron = "${discord.clem.mission.scheduler.cron}")
    public void syncMissions() throws IOException {
        
        List<Mission> missionsDb = missionRepository.findByEndDateIsNull();
        List<String> missionNames = missionsDb.stream().map(mission -> mission.getName()).collect(Collectors.toList());
        List<MissionDropTableDTO> missionDTOs = getMissionDTOs();
        
        List<String> missionDtoNames = missionDTOs.stream().map(mission -> mission.getName()).collect(Collectors.toList());
        List<MissionDropTableDTO> newMissionDTOs = missionDTOs.stream()
                .filter(mission -> !missionNames.contains(mission.getName()))
                .collect(Collectors.toList());
        List<MissionDropTableDTO> updateMissionDTOs = missionDTOs.stream()
                .filter(weapon -> missionNames.contains(weapon.getName()))
                .collect(Collectors.toList());
        
        log.info("syncMissions: received [{}] missions, [{}] are new, [{}] can be updated.", 
                missionDTOs.size(), newMissionDTOs.size(), updateMissionDTOs.size());
        
        // add
        for (MissionDropTableDTO missionDTO : newMissionDTOs) {
            Mission newMission = missionMapper.missionDtoToMission(missionDTO);
            Set<MissionReward> missionRewards = newMission.getAllRewards();
            
            Mission dbMission = missionRepository.save(newMission);
            
            missionRewards.forEach(reward -> reward.setMission(dbMission));
            missionRewardRepository.saveAll(missionRewards);
            
            log.info("syncMissions: added new mission [{} - {}]", dbMission.getId(), dbMission.getName());
        }
        
        // update
        for (MissionDropTableDTO missionDTO : updateMissionDTOs) {
            Mission missionDb = missionsDb.stream()
                    .filter(mission -> mission.getName().equals(missionDTO.getName()))
                    .findFirst()
                    .get();
            
            // delete old mission rewards
            missionDb.getAllRewards().clear();
            missionRewardRepository.deleteByMission(missionDb);
            
            // update
            missionMapper.updateMission(missionDb, missionDTO);
            missionRepository.save(missionDb);
            
            missionDb.getAllRewards().forEach(reward -> reward.setMission(missionDb));
            missionRewardRepository.saveAll(missionDb.getAllRewards());
        }
        
        // inactivate
        List<Mission> toInactivateMissionsDb = new ArrayList<>();
        for (Mission mission : missionsDb) {
            if (!missionDtoNames.contains(mission.getName())) {
                toInactivateMissionsDb.add(mission);
                mission.setEndDate(LocalDateTime.now());
                log.info("syncMissions: inactivated old mission [{} - {}]", mission.getId(), mission.getName());
            }
        }
        missionRepository.saveAll(toInactivateMissionsDb);
    }

    private List<MissionDropTableDTO> getMissionDTOs() throws IOException {
        String dropTableString = warframeClient.getDataRaw(apiUrl, apiHeaders);
        
        Document html = Jsoup.parse(dropTableString);
        Elements elements = html.select("table");

        Elements missionElements = elements.get(0).select("tr");
        return getMissionDTOs(missionElements);
    }

    private List<MissionDropTableDTO> getMissionDTOs(Elements missionElements) {

        List<MissionDropTableDTO> missions = new ArrayList<>();
        MissionDropTableDTO currentMission = new MissionDropTableDTO();
        missions.add(currentMission);
        String currentRotation = "G";

        for (Element element : missionElements) {            
            String text = element.text();
            if (text.equals("")) {
                currentMission = new MissionDropTableDTO();
                missions.add(currentMission);
                currentRotation = "G";
                continue;
            }

            if (currentMission.getName() == null) {
                currentMission.setName(text);
                continue;
            }

            if (text.equals("Rotation A")) {
                currentRotation = "A";
                continue;
            } else if (text.equals("Rotation B")) {
                currentRotation = "B";
                continue;
            } else if (text.equals("Rotation C")) {
                currentRotation = "C";
                continue;
            }

            // finally a real reward and not mission name or rotation
            Elements rewards = element.select("td");
            String item = rewards.first().text();
            String dropChance = rewards.last().text().replace("Very ", "").replace("Ultra ", "");
            Rarity rarity = Rarity.valueOf(dropChance.split(" ")[0].toUpperCase());
            Double chance = Double.parseDouble(dropChance.split(" ")[1].replace("(", "").replace("%)", ""));

            RewardDTO reward = new RewardDTO();
            reward.setReward(item);
            reward.setRarity(rarity);
            reward.getChance().add(chance);

            if (currentRotation.equals("G")) {
                currentMission.getRewardsRotationGeneral().add(reward);
            } else if (currentRotation.equals("A")) {
                currentMission.getRewardsRotationA().add(reward);
            } else if (currentRotation.equals("B")) {
                currentMission.getRewardsRotationB().add(reward);
            } else if (currentRotation.equals("C")) {
                currentMission.getRewardsRotationC().add(reward);
            }
        }

        return missions.stream()
                .filter(mission -> mission.getName() != null)
                .collect(Collectors.toList());
    }

    public List<Mission> getMissions() {
        return missionRepository.findByEndDateIsNull();
    }
}
