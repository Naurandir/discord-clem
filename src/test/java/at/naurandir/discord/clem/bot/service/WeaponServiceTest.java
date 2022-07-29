package at.naurandir.discord.clem.bot.service;

import at.naurandir.discord.clem.bot.model.item.Weapon;
import at.naurandir.discord.clem.bot.model.item.WeaponMapper;
import at.naurandir.discord.clem.bot.repository.WeaponRepository;
import at.naurandir.discord.clem.bot.service.client.dto.worldstate.WeaponDTO;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 *
 * @author Naurandir
 */
@ExtendWith(MockitoExtension.class)
public class WeaponServiceTest extends BaseServiceTest {
    
    @Spy
    private WeaponMapper weaponMapper;
    
    @Spy
    private WeaponRepository weaponRepository;

    @InjectMocks
    private WeaponService weaponService;
    
    private Weapon weapon;
    
    private final int numberOfWeapons = 80; // 80 are in the json currently
    
    @Override
    @BeforeEach
    public void setup() throws IOException {
        weapon = new Weapon();
        weapon.setId(815L);
        weapon.setName("Any");
        weapon.setUniqueName("Any");
        
        when(weaponMapper.fromDtoToWeapon(any())).thenReturn(weapon);
        
        when(weaponRepository.findByEndDateIsNull()).thenReturn(new ArrayList<>());
        when(weaponRepository.save(any())).thenReturn(weapon);
        
        setWarframeClientGetListMock("dto/weapons.json", WeaponDTO.class);
    }
    
    @Test
    public void shouldSyncNewWeapons() throws IOException {
        weaponService.syncWeapons();
        
        verify(weaponRepository, times(numberOfWeapons)).save(any());
        verify(weaponMapper, times(numberOfWeapons)).fromDtoToWeapon(any());
    }
    
    @Test
    public void shouldSyncExistingWeaponsNothingToDo() throws IOException {
        when(weaponRepository.findByEndDateIsNull()).thenReturn(List.of(weapon));
        weapon.setUniqueName("/Lotus/Weapons/Tenno/LongGuns/SapientPrimary/SapientPrimaryWeapon");
        
        weaponService.syncWeapons();
        
        verify(weaponRepository, times(numberOfWeapons - 1)).save(any());
        verify(weaponMapper, times(numberOfWeapons - 1)).fromDtoToWeapon(any());
    }
    
    @Test
    public void shouldSyncExpiredWeapons() throws IOException {
        when(weaponRepository.findByEndDateIsNull()).thenReturn(List.of(weapon));
        weapon.setName("oldWeapon");
        
        weaponService.syncWeapons();
        
        verify(weaponRepository, times(numberOfWeapons)).save(any());
        verify(weaponMapper, times(numberOfWeapons)).fromDtoToWeapon(any());
        Assertions.assertTrue(() -> weapon.getEndDate() != null);
    }
}
