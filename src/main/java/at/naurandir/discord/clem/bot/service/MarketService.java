package at.naurandir.discord.clem.bot.service;

import at.naurandir.discord.clem.bot.model.item.Item;
import at.naurandir.discord.clem.bot.model.item.MarketType;
import at.naurandir.discord.clem.bot.model.item.Warframe;
import at.naurandir.discord.clem.bot.model.item.WarframeMapper;
import at.naurandir.discord.clem.bot.model.item.Weapon;
import at.naurandir.discord.clem.bot.model.item.WeaponMapper;
import at.naurandir.discord.clem.bot.service.client.WarframeClient;
import at.naurandir.discord.clem.bot.service.client.dto.market.MarketItemDTO;
import at.naurandir.discord.clem.bot.service.client.dto.market.MarketItemsDTO;
import at.naurandir.discord.clem.bot.service.client.dto.market.MarketLichAuctionDTO;
import at.naurandir.discord.clem.bot.service.client.dto.market.MarketLichAuctionsDTO;
import at.naurandir.discord.clem.bot.service.client.dto.market.MarketLichWeaponDTO;
import at.naurandir.discord.clem.bot.service.client.dto.market.MarketLichWeaponsDTO;
import at.naurandir.discord.clem.bot.service.client.dto.market.MarketOrderDTO;
import at.naurandir.discord.clem.bot.service.client.dto.market.MarketOrdersResultDTO;
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
import org.springframework.stereotype.Service;

/**
 *
 * @author Naurandir
 */
@Slf4j
@Service
public class MarketService extends SyncService {
    
    @Autowired
    private WarframeService warframeService;
    
    @Autowired
    private WeaponService weaponService;
    
    @Autowired
    private WarframeClient warframeClient;
    
    @Value( "${discord.clem.market.item.url}" )
    private String apiItemUrl;
    
    @Value( "${discord.clem.market.item.order.url}" )
    private String apiOrderUrl;
    
    @Value( "${discord.clem.market.lich.weapon.url}" )
    private String apiLichWeaponUrl;
    
    @Value( "${discord.clem.market.lich.auction.url}" )
    private String apiLichAuctionUrl;
    
    @Value( "${discord.clem.market.lich.auction.element.url}" )
    private String apiLichAuctionWithElementUrl;
    
    @Value("#{${discord.clem.market.headers}}")
    private Map<String, String> apiHeaders;

    @Override
    public void sync() throws IOException {
        List<Warframe> warframesDb = warframeService.getWarframes();
        List<Weapon> weaponsDb = weaponService.getWeapons();
        
        log.debug("sync: received [{}] warframes and [{}] weapons from database.", warframesDb.size(), weaponsDb.size());
        
        MarketItemsDTO currentItemsResponse = warframeClient.getData(apiItemUrl, apiHeaders, MarketItemsDTO.class);
        List<MarketItemDTO> currentItems = currentItemsResponse.getPayload().get("items");
        
        MarketLichWeaponsDTO currentLichWeaponsResponse = warframeClient.getData(apiLichWeaponUrl, apiHeaders, MarketLichWeaponsDTO.class);
        List<MarketLichWeaponDTO> currentLichWeapons = currentLichWeaponsResponse.getPayload().get("weapons");
        
        log.debug("sync: received [{}] market items and [{}] lich weapons from market", currentItems.size(), currentLichWeapons.size());
        
        syncItems(warframesDb, weaponsDb, currentItems);
        syncLichWeapons(weaponsDb, currentLichWeapons);
    }
    
    private void syncItems(List<Warframe> warframesDb, List<Weapon> weaponsDb, List<MarketItemDTO> currentItems) throws IOException {        
        List<String> warframeNames = warframesDb.stream().map(item -> item.getName()).collect(Collectors.toList());
        List<String> weaponNames = weaponsDb.stream().map(item -> item.getName()).collect(Collectors.toList());
        
        for (Warframe warframe : warframesDb) {
            MarketItemDTO warframeItem = currentItems.stream()
                    .filter(currentItem -> warframeNames.contains(currentItem.getItemName()))
                    .findAny()
                    .orElse(null);
            if (warframeItem != null) {
                warframeService.addMarketData(warframe, warframeItem, MarketType.ITEM);
                warframeService.save(warframe);
            }
        }
        
        for (Weapon weapon : weaponsDb) {
            MarketItemDTO weaponItem = currentItems.stream()
                    .filter(currentItem -> weaponNames.contains(currentItem.getItemName()))
                    .findAny()
                    .orElse(null);
            if (weaponItem != null) {
                weaponService.addMarketData(weapon, weaponItem, MarketType.ITEM);
                weaponService.save(weapon);
            }
        }
    }

    private void syncLichWeapons(List<Weapon> weaponsDb, List<MarketLichWeaponDTO> currentLichWeapons) throws IOException {
        List<String> weaponNames = weaponsDb.stream().map(item -> item.getName()).collect(Collectors.toList());
        
        for (Weapon weapon : weaponsDb) {
            MarketItemDTO weaponItem = currentLichWeapons.stream()
                    .filter(currentItem -> weaponNames.contains(currentItem.getItemName()))
                    .findAny()
                    .orElse(null);
            if (weaponItem != null) {
                weaponService.addMarketData(weapon, weaponItem, MarketType.LICH_WEAPON);
                weaponService.save(weapon);
            }
        }
    }
    
    public List<Item> getMarketItems(String name) {
        List<Item> foundMarketItems = new ArrayList<>();
        
        warframeService.getWarframesByNameAndMarketType(name, MarketType.ITEM)
                .forEach(item -> foundMarketItems.add(item));
        weaponService.getWeaponsByNameAndMarketType(name, MarketType.ITEM)
                .forEach(item -> foundMarketItems.add(item));
        
        return foundMarketItems;
    }
    
    public List<Weapon> getMarketLichWeapons(String name) {
        return weaponService.getWeaponsByNameAndMarketType(name, MarketType.LICH_WEAPON);
    }
    
    public List<MarketOrderDTO> getCurrentOrders(Item marketItem) throws IOException {
        MarketOrdersResultDTO result = warframeClient.getData(
                apiOrderUrl.replace("{itemName}", marketItem.getMarketUrlName()), 
                apiHeaders, 
                MarketOrdersResultDTO.class);
        
        return result.getPayload().getOrders();
    }
    
    public List<MarketLichAuctionDTO> getCurrentLichAuctions(Item lichWeapon, Optional<String> element) throws IOException {
        String apiUrl;
        if (element.isPresent()) {
            apiUrl = apiLichAuctionWithElementUrl
                    .replace("{weaponName}", lichWeapon.getMarketUrlName())
                    .replace("{element}", element.get());
        } else {
            apiUrl = apiLichAuctionUrl
                    .replace("{weaponName}", lichWeapon.getMarketUrlName());
        }
        
        MarketLichAuctionsDTO result = warframeClient.getData(apiUrl, apiHeaders, MarketLichAuctionsDTO.class);
        return result.getPayload().get("auctions");
    }

    @Override
    boolean isFirstTimeStartup() {
        return warframeService.getWarframesByMarketType(MarketType.ITEM).isEmpty() ||
               weaponService.getWeaponsByMarketType(MarketType.LICH_WEAPON).isEmpty();
    }    
}
