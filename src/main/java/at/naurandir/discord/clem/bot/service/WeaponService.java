package at.naurandir.discord.clem.bot.service;

import at.naurandir.discord.clem.bot.model.item.ItemMapper;
import at.naurandir.discord.clem.bot.model.item.Weapon;
import at.naurandir.discord.clem.bot.repository.WeaponRepository;
import at.naurandir.discord.clem.bot.service.client.WarframeClient;
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
public class WeaponService {
    
    @Autowired
    private ItemMapper itemMapper;
    
    @Autowired
    private WeaponRepository weaponRepository;
    
    @Autowired
    private WarframeClient warframeClient;
    
    @Value( "${discord.clem.weapon.url}" )
    private String apiUrlWeapon;
    
    @Value("#{${discord.clem.weapon.headers}}")
    private Map<String, String> apiHeaders;
    
    @Scheduled(cron = "${discord.clem.weapon.scheduler.cron}")
    public void syncWeapons() throws IOException {
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
    
}
