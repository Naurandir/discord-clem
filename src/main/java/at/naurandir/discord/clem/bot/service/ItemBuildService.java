package at.naurandir.discord.clem.bot.service;

import at.naurandir.discord.clem.bot.model.item.ItemBuildMapper;
import at.naurandir.discord.clem.bot.repository.ItemBuildRepository;
import at.naurandir.discord.clem.bot.service.client.WarframeClient;
import at.naurandir.discord.clem.bot.service.client.dto.overframe.OverframeDTO;
import at.naurandir.discord.clem.bot.service.client.dto.overframe.OverframeItemDTO;
import at.naurandir.discord.clem.bot.service.client.dto.overframe.OverframeItemTierDTO;
import at.naurandir.discord.clem.bot.service.client.dto.overframe.OverframeItemTierListDTO;
import java.io.IOException;
import java.util.ArrayList;
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
public class ItemBuildService {
    
    @Autowired
    private ItemBuildMapper itemBuildMapper;
    
    @Autowired
    private ItemBuildRepository itemBuildRepository;
    
    @Autowired
    private WarframeService warframeService;
    
    @Autowired
    private WeaponService weaponService;
    
    @Autowired
    private WarframeClient warframeClient;
    
    @Value("${discord.clem.build.tier.url}")
    private String apiTierListUrl;
    
    @Value("${discord.clem.build.tier.url}")
    private String apiItemUrl;

    @Value("#{${discord.clem.build.headers}}")
    private Map<String, String> apiHeaders;
    
       public OverframeDTO getCurrentBuilds() throws IOException {
        OverframeDTO dto = new OverframeDTO();
        
        OverframeItemTierListDTO warframeTierlist = 
                warframeClient.getData(apiTierListUrl.replace("type", "0"),
                        apiHeaders, 
                        OverframeItemTierListDTO.class);
        
        OverframeItemTierListDTO primaryWeaponTierlist = 
                warframeClient.getData(apiTierListUrl.replace("type", "1"), 
                        apiHeaders, 
                        OverframeItemTierListDTO.class);
        
        OverframeItemTierListDTO secondaryTierlist = 
                warframeClient.getData(apiTierListUrl.replace("type", "2"), 
                        apiHeaders, 
                        OverframeItemTierListDTO.class);
        
        OverframeItemTierListDTO meeleTierlist = 
                warframeClient.getData(apiTierListUrl.replace("type", "3"), 
                        apiHeaders, 
                        OverframeItemTierListDTO.class);
        
        List<OverframeItemDTO> warframes = new ArrayList<>();
        for (OverframeItemTierDTO itemTier : warframeTierlist.getTierList()) {
            OverframeItemDTO item = warframeClient.getData(
                    apiItemUrl.replace("id", String.valueOf(itemTier.getItemId())), 
                    apiHeaders, 
                    OverframeItemDTO.class);
            warframes.add(item);
        }
        
        List<OverframeItemDTO> primaryWeapons = new ArrayList<>();
        for (OverframeItemTierDTO itemTier : primaryWeaponTierlist.getTierList()) {
            OverframeItemDTO item = warframeClient.getData(
                    apiItemUrl.replace("id", String.valueOf(itemTier.getItemId())), 
                    apiHeaders, 
                    OverframeItemDTO.class);
            primaryWeapons.add(item);
        }
        
        List<OverframeItemDTO> secondaryWeapons = new ArrayList<>();
        for (OverframeItemTierDTO itemTier : secondaryTierlist.getTierList()) {
            OverframeItemDTO item = warframeClient.getData(
                    apiItemUrl.replace("id", String.valueOf(itemTier.getItemId())), 
                    apiHeaders, 
                    OverframeItemDTO.class);
            secondaryWeapons.add(item);
        }
        
        List<OverframeItemDTO> meeleWeapons = new ArrayList<>();
        for (OverframeItemTierDTO itemTier : meeleTierlist.getTierList()) {
            OverframeItemDTO item = warframeClient.getData(
                    apiItemUrl.replace("id", String.valueOf(itemTier.getItemId())), 
                    apiHeaders, 
                    OverframeItemDTO.class);
            meeleWeapons.add(item);
        }
        
        dto.setWarframes(warframes);
        dto.setPrimaryWeapons(primaryWeapons);
        dto.setSecondaryWeapons(secondaryWeapons);
        dto.setMeeleWeapons(meeleWeapons);
        
        return dto;
    }
}
