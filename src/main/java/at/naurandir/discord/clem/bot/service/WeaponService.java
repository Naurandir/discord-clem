package at.naurandir.discord.clem.bot.service;

import at.naurandir.discord.clem.bot.model.item.Weapon;
import at.naurandir.discord.clem.bot.model.item.WeaponMapper;
import at.naurandir.discord.clem.bot.repository.WeaponRepository;
import at.naurandir.discord.clem.bot.service.client.WarframeClient;
import at.naurandir.discord.clem.bot.service.client.dto.worldstate.WeaponDTO;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import static org.apache.commons.lang3.StringUtils.isEmpty;
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
public class WeaponService extends SyncService {
    
    @Autowired
    private WeaponMapper weaponMapper;
    
    @Autowired
    private WeaponRepository weaponRepository;
    
    @Autowired
    private WarframeClient warframeClient;
    
    @Value( "${discord.clem.weapon.url}" )
    private String apiUrlWeapon;
    
    @Value("#{${discord.clem.weapon.headers}}")
    private Map<String, String> apiHeaders;
    
    @Override
    @Scheduled(cron = "${discord.clem.weapon.scheduler.cron}")
    public void sync() throws IOException {
        List<Weapon> weaponsDb = weaponRepository.findByEndDateIsNull();
        List<String> weaponNames = weaponsDb.stream().map(weapon -> weapon.getUniqueName()).collect(Collectors.toList());
        log.debug("syncWeapons: received [{}] weapons from database.", weaponsDb.size());
        
        List<WeaponDTO> weaponDTOs = warframeClient.getListData(apiUrlWeapon, apiHeaders, WeaponDTO.class);
        weaponDTOs = weaponDTOs.stream().filter(weapon -> !isEmpty(weapon.getWikiaUrl())).collect(Collectors.toList());
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
            Weapon newWeapon = weaponMapper.fromDtoToWeapon(newWeaponDTO);
            newWeapon = weaponRepository.save(newWeapon);
            log.info("syncWeapons: added new weapon [{} - {}]", newWeapon.getId(), newWeapon.getName());
        }
        
        // update
        for (WeaponDTO updateWeaponDTO : updateWeaponDTOs) {
            Weapon weaponDb = weaponsDb.stream()
                    .filter(weapon -> weapon.getUniqueName().equals(updateWeaponDTO.getUniqueName()))
                    .findFirst()
                    .get();
            weaponMapper.updateWeapon(weaponDb, updateWeaponDTO);
        }
        weaponRepository.saveAll(weaponsDb);
        
        // inactivate
        List<Weapon> toInactivateWeaponsDb = new ArrayList<>();
        for (Weapon weaponDb : weaponsDb) {
            if (!weaponDtoNames.contains(weaponDb.getUniqueName())) {
                toInactivateWeaponsDb.add(weaponDb);
                weaponDb.setEndDate(LocalDateTime.now());
            }
        }
        weaponRepository.saveAll(toInactivateWeaponsDb);
    }
    
    public List<Weapon> getWeapons() {
        return weaponRepository.findByEndDateIsNull();
    }
    
    public List<Weapon> findWeaponsByName(String name) {
        return weaponRepository.findByNameContainingIgnoreCaseAndEndDateIsNull(name);
    }

    void updateWikiThumbnail(Weapon weapon, String pictureUrl) {
        Optional<Weapon> foundWeapon = weaponRepository.findById(weapon.getId());
        if (foundWeapon.isEmpty()) {
            log.warn("updateWikiThumbnail: cannot update thumbnail of weapon [{} - {}], as reload of weapon did not work",
                    weapon.getId(), weapon.getName());
            return;
        }
        
        foundWeapon.get().setWikiaThumbnail(pictureUrl);
        weaponRepository.save(foundWeapon.get());
    }
    
    @Override
    boolean isFirstTimeStartup() {
        return weaponRepository.findByEndDateIsNull().isEmpty();
    }
}
