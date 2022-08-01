package at.naurandir.discord.clem.bot.service;

import at.naurandir.discord.clem.bot.model.market.MarketItem;
import at.naurandir.discord.clem.bot.model.market.MarketItemMapper;
import at.naurandir.discord.clem.bot.model.market.MarketLichWeapon;
import at.naurandir.discord.clem.bot.model.market.MarketLichWeaponMapper;
import at.naurandir.discord.clem.bot.repository.MarketItemRepository;
import at.naurandir.discord.clem.bot.repository.MarketLichWeaponRepository;
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
    private MarketItemMapper marketItemMapper;
    
    @Autowired
    private MarketItemRepository marketItemRepository;
    
    @Autowired
    private MarketLichWeaponMapper marketLichWeaponMapper;
    
    @Autowired
    private MarketLichWeaponRepository marketLichWeaponRepository;
    
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
        syncItems();
        syncLichWeapons();
    }
    
    private void syncItems() throws IOException {
        List<MarketItem> itemsDb = marketItemRepository.findByEndDateIsNull();
        List<String> itemNames = itemsDb.stream().map(item -> item.getName()).collect(Collectors.toList());
        log.debug("syncItems: received [{}] items from database.", itemsDb.size());
        
        MarketItemsDTO currentItemsResponse = warframeClient.getData(apiItemUrl, apiHeaders, MarketItemsDTO.class);
        List<MarketItemDTO> currentItems = currentItemsResponse.getPayload().get("items");
        List<String> itemDtoNames = currentItems.stream().map(item -> item.getItemName()).collect(Collectors.toList());
        
        List<MarketItemDTO> newItemDTOs = currentItems.stream()
                .filter(item -> !itemNames.contains(item.getItemName()))
                .collect(Collectors.toList());
        List<MarketItemDTO> updateItemDTOs = currentItems.stream()
                .filter(item -> itemNames.contains(item.getItemName()))
                .collect(Collectors.toList());
        log.info("syncItems: received [{}] items, [{}] are new, [{}] can be updated.", 
                currentItems.size(), newItemDTOs.size(), updateItemDTOs.size());
        
        // add
        for (MarketItemDTO newItemDTO : newItemDTOs) {
            MarketItem newMarketItem = marketItemMapper.fromDtoToMarketItem(newItemDTO);
            newMarketItem = marketItemRepository.save(newMarketItem);
            log.info("syncItems: added new item [{} - {}]", newMarketItem.getId(), newMarketItem.getName());
        }
        
        // update
        for (MarketItemDTO updateItemDTO : updateItemDTOs) {
            MarketItem itemDb = itemsDb.stream()
                    .filter(item -> item.getName().equals(updateItemDTO.getItemName()))
                    .findFirst()
                    .get();
            marketItemMapper.updateMarketItem(itemDb, updateItemDTO);
        }
        marketItemRepository.saveAll(itemsDb);
        
        // inactivate
        List<MarketItem> toInactivateItemsDb = new ArrayList<>();
        for (MarketItem itemDb : itemsDb) {
            if (!itemDtoNames.contains(itemDb.getName())) {
                toInactivateItemsDb.add(itemDb);
                itemDb.setEndDate(LocalDateTime.now());
                log.info("syncItems: inactivated item [{} - {}]", itemDb.getId(), itemDb.getName());
            }
        }
        marketItemRepository.saveAll(toInactivateItemsDb);
    }

    private void syncLichWeapons() throws IOException {
        List<MarketLichWeapon> lichWeaponsDb = marketLichWeaponRepository.findByEndDateIsNull();
        List<String> lichWeaponNames = lichWeaponsDb.stream().map(lichWeapon -> lichWeapon.getName()).collect(Collectors.toList());
        log.debug("syncLichWeapons: received [{}] lichWeapons from database.", lichWeaponsDb.size());
        
        MarketLichWeaponsDTO currentLichWeaponsResponse = warframeClient.getData(apiLichWeaponUrl, apiHeaders, MarketLichWeaponsDTO.class);
        List<MarketLichWeaponDTO> currentLichWeapons = currentLichWeaponsResponse.getPayload().get("weapons");
        List<String> lichWeaponDtoNames = currentLichWeapons.stream().map(lichWeapon -> lichWeapon.getItemName()).collect(Collectors.toList());
        
        List<MarketLichWeaponDTO> newLichWeaponDTOs = currentLichWeapons.stream()
                .filter(lichWeapon -> !lichWeaponNames.contains(lichWeapon.getItemName()))
                .collect(Collectors.toList());
        List<MarketLichWeaponDTO> updateLichWeaponDTOs = currentLichWeapons.stream()
                .filter(lichWeapon -> lichWeaponNames.contains(lichWeapon.getItemName()))
                .collect(Collectors.toList());
        log.info("syncLichWeapons: received [{}] lichWeapons, [{}] are new, [{}] can be updated.", 
                currentLichWeapons.size(), newLichWeaponDTOs.size(), updateLichWeaponDTOs.size());
        
        // add
        for (MarketLichWeaponDTO newLichWeaponDTO : newLichWeaponDTOs) {
            MarketLichWeapon newMarketLichWeapon = marketLichWeaponMapper.fromDtoToLichWeapon(newLichWeaponDTO);
            newMarketLichWeapon = marketLichWeaponRepository.save(newMarketLichWeapon);
            log.info("syncLichWeapons: added new lichWeapon [{} - {}]", newMarketLichWeapon.getId(), newMarketLichWeapon.getName());
        }
        
        // update
        for (MarketLichWeaponDTO updateLichWeaponDTO : updateLichWeaponDTOs) {
            MarketLichWeapon lichWeaponDb = lichWeaponsDb.stream()
                    .filter(lichWeapon -> lichWeapon.getName().equals(updateLichWeaponDTO.getItemName()))
                    .findFirst()
                    .get();
            marketLichWeaponMapper.updateLichWeapon(lichWeaponDb, updateLichWeaponDTO);
        }
        marketLichWeaponRepository.saveAll(lichWeaponsDb);
        
        // inactivate
        List<MarketLichWeapon> toInactivateLichWeaponsDb = new ArrayList<>();
        for (MarketLichWeapon lichWeaponDb : lichWeaponsDb) {
            if (!lichWeaponDtoNames.contains(lichWeaponDb.getName())) {
                toInactivateLichWeaponsDb.add(lichWeaponDb);
                lichWeaponDb.setEndDate(LocalDateTime.now());
                log.info("syncLichWeapons: inactivated lichWeapon [{} - {}]", lichWeaponDb.getId(), lichWeaponDb.getName());
            }
        }
        marketLichWeaponRepository.saveAll(toInactivateLichWeaponsDb);
    }
    
    public List<MarketItem> getMarketItems(String name) {
        return marketItemRepository.findByNameContainingIgnoreCaseAndEndDateIsNull(name);
    }
    
    public List<MarketLichWeapon> getMarketLichWeapons(String name) {
        return marketLichWeaponRepository.findByNameContainingIgnoreCaseAndEndDateIsNull(name);
    }
    
    public List<MarketOrderDTO> getCurrentOrders(MarketItem marketItem) throws IOException {
        MarketOrdersResultDTO result = warframeClient.getData(
                apiOrderUrl.replace("{itemName}", marketItem.getUrlName()), 
                apiHeaders, 
                MarketOrdersResultDTO.class);
        
        return result.getPayload().getOrders();
    }
    
    public List<MarketLichAuctionDTO> getCurrentLichAuctions(MarketLichWeapon lichWeapon, Optional<String> element) throws IOException {
        String apiUrl;
        if (element.isPresent()) {
            apiUrl = apiLichAuctionWithElementUrl
                    .replace("{weaponName}", lichWeapon.getUrlName())
                    .replace("{element}", element.get());
        } else {
            apiUrl = apiLichAuctionUrl
                    .replace("{weaponName}", lichWeapon.getUrlName());
        }
        
        MarketLichAuctionsDTO result = warframeClient.getData(apiUrl, apiHeaders, MarketLichAuctionsDTO.class);
        return result.getPayload().get("auctions");
    }

    @Override
    boolean isFirstTimeStartup() {
        return marketItemRepository.findByEndDateIsNull().isEmpty() ||
               marketLichWeaponRepository.findByEndDateIsNull().isEmpty();
    }    
}
