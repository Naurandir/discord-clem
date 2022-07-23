package at.naurandir.discord.clem.bot.service;

import at.naurandir.discord.clem.bot.model.item.ItemMapper;
import at.naurandir.discord.clem.bot.model.item.Warframe;
import at.naurandir.discord.clem.bot.model.item.Weapon;
import at.naurandir.discord.clem.bot.repository.WarframeRepository;
import at.naurandir.discord.clem.bot.repository.WeaponRepository;
import at.naurandir.discord.clem.bot.service.client.WarframeClient;
import at.naurandir.discord.clem.bot.service.client.dto.worldstate.WarframeDTO;
import at.naurandir.discord.clem.bot.service.client.dto.worldstate.WeaponDTO;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
public class ItemService {

    @Autowired
    private ItemMapper itemMapper;
    
    @Autowired
    private WeaponRepository weaponRepository;
    
    @Autowired
    private WarframeRepository warframeRepository;
    
    @Value( "${discord.clem.item.warframe.url}" )
    private String apiUrlWarframe;
    
    @Value( "${discord.clem.item.weapon.url}" )
    private String apiUrlWeapon;
    
    @Value("#{${discord.clem.item.headers}}")
    private Map<String, String> apiHeaders;
    
    private final WarframeClient warframeClient = new WarframeClient();
    
    @Scheduled(cron = "${discord.clem.item.scheduler.cron}")
    public void syncItems() throws IOException {
        syncWeapons();
        syncWarframes();
    }
    
    private void syncWeapons() throws IOException {
        List<Weapon> weaponsDb = weaponRepository.findByEndDateIsNull();
        List<String> weaponNames = weaponsDb.stream().map(weapon -> weapon.getUniqueName()).collect(Collectors.toList());
        log.debug("syncWeapons: received [{}] weapons from database.", weaponsDb.size());
        
        List<WeaponDTO> weaponDTOs = warframeClient.getListData(apiUrlWeapon, apiHeaders, WeaponDTO.class);
        List<String> weaponDtoNames = weaponDTOs.stream().map(weapon -> weapon.getUniqueName()).collect(Collectors.toList());
        List<WeaponDTO> newWeaponDTOs = weaponDTOs.stream()
                .filter(weapon -> !weaponNames.contains(weapon.getUniqueName()))
                .collect(Collectors.toList());
        List<WeaponDTO> updateWeaponDTOs = weaponDTOs.stream()
                .filter(weapon -> weaponNames.contains(weapon.getUniqueName()))
                .collect(Collectors.toList());
        log.info("syncWeapons: received [{}] weapons, [{}] are new, [{}] can be updated.", 
                weaponDTOs.size(), newWeaponDTOs.size(), updateWeaponDTOs.size());
        
        // add
        for (WeaponDTO newWeaponDTO : newWeaponDTOs) {
            Weapon newWeapon = itemMapper.fromDtoToWeapon(newWeaponDTO);
            newWeapon = weaponRepository.save(newWeapon);
            log.info("syncWeapons: added new weapon [{} - {}]", newWeapon.getId(), newWeapon.getName());
        }
        
        // update
        for (WeaponDTO updateWeaponDTO : updateWeaponDTOs) {
            Weapon weaponDb = weaponsDb.stream()
                    .filter(weapon -> weapon.getUniqueName().equals(updateWeaponDTO.getUniqueName()))
                    .findFirst()
                    .get();
            
            itemMapper.updateWeapon(weaponDb, updateWeaponDTO);
        }
        weaponRepository.saveAll(weaponsDb);
        
        // inactivate
        List<Weapon> toInactivateWeaponsDb = new ArrayList<>();
        for (Weapon weaponDb : weaponsDb) {
            if (!weaponDtoNames.contains(weaponDb.getName())) {
                toInactivateWeaponsDb.add(weaponDb);
                weaponDb.setEndDate(LocalDateTime.now());
            }
        }
        weaponRepository.saveAll(toInactivateWeaponsDb);
    }
    
    private void syncWarframes() throws IOException {
        List<Warframe> warframesDb = warframeRepository.findByEndDateIsNull();
        List<String> warframeNames = warframesDb.stream().map(warframe -> warframe.getUniqueName()).collect(Collectors.toList());
        log.debug("syncWarframes: received [{}] warframes from database.", warframesDb.size());
        
        List<WarframeDTO> warframeDTOs = warframeClient.getListData(apiUrlWarframe, apiHeaders, WarframeDTO.class);
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
            Warframe newWarframe = itemMapper.fromDtoToWarframe(newWarframeDTO);
            newWarframe = warframeRepository.save(newWarframe);
            log.info("syncWarframes: added new warframe [{} - {}]", newWarframe.getId(), newWarframe.getName());
        }
        
        // update
        for (WarframeDTO updateWarframeDTO : updateWarframeDTOs) {
            Warframe warframeDb = warframesDb.stream()
                    .filter(warframe -> warframe.getUniqueName().equals(updateWarframeDTO.getUniqueName()))
                    .findFirst()
                    .get();
            
            itemMapper.updateWarframe(warframeDb, updateWarframeDTO);
        }
        warframeRepository.saveAll(warframesDb);
        
        // delete
        List<Warframe> toInactivateWarframesDb = new ArrayList<>();
        for (Warframe warframeDb : warframesDb) {
            if (!warframeDtoNames.contains(warframeDb.getName())) {
                toInactivateWarframesDb.add(warframeDb);
                warframeDb.setEndDate(LocalDateTime.now());
            }
        }
        warframeRepository.saveAll(toInactivateWarframesDb);
    }
    
}
