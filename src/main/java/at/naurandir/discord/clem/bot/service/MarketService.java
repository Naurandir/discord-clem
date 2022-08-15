package at.naurandir.discord.clem.bot.service;

import at.naurandir.discord.clem.bot.model.item.MarketItem;
import at.naurandir.discord.clem.bot.model.item.MarketItemMapper;
import at.naurandir.discord.clem.bot.model.item.MarketLichWeapon;
import at.naurandir.discord.clem.bot.model.item.MarketLichWeaponMapper;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 *
 * @author Naurandir
 */
@Slf4j
@Service
public class MarketService extends SyncService {
    
    @Autowired
    private MarketItemRepository marketItemRepository;
    
    @Autowired
    private MarketLichWeaponRepository marketLichWeaponRepository;
    
    @Autowired
    private MarketItemMapper marketItemMapper;
    
    @Autowired
    private MarketLichWeaponMapper marketLichWeaponMapper;
    
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
    @Scheduled(cron = "${discord.clem.market.scheduler.cron}")
    public void sync() {
        try {
            doSync();
        } catch (Exception ex) {
            log.error("sync: throwed error: ", ex);
        }
    }
    
    @Override
    public void doSync() throws IOException {
        List<MarketItem> marketItemsDb = marketItemRepository.findByEndDateIsNull();
        List<MarketLichWeapon> marketLichWeaponsDb = marketLichWeaponRepository.findByEndDateIsNull();
        
        log.debug("sync: received [{}] market items and [{}] lich weapons from database.", marketItemsDb.size(), marketLichWeaponsDb.size());
        
        MarketItemsDTO currentItemsResponse = warframeClient.getData(apiItemUrl, apiHeaders, MarketItemsDTO.class);
        List<MarketItemDTO> currentItems = currentItemsResponse.getPayload().get("items");
        
        MarketLichWeaponsDTO currentLichWeaponsResponse = warframeClient.getData(apiLichWeaponUrl, apiHeaders, MarketLichWeaponsDTO.class);
        List<MarketLichWeaponDTO> currentLichWeapons = currentLichWeaponsResponse.getPayload().get("weapons");
        
        log.debug("sync: received [{}] market items and [{}] lich weapons from market", currentItems.size(), currentLichWeapons.size());
        
        syncItems(marketItemsDb, currentItems);
        syncLichWeapons(marketLichWeaponsDb, currentLichWeapons);
    }
    
    private void syncItems(List<MarketItem> marketItemsDb, List<MarketItemDTO> currentMarketItems) throws IOException {
        List<String> itemNames = marketItemsDb.stream().map(item -> item.getName()).collect(Collectors.toList());
        List<String> itemDtoNames = currentMarketItems.stream().map(item -> item.getItemName()).collect(Collectors.toList());
        
        List<MarketItemDTO> newItemDTOs = currentMarketItems.stream()
                .filter(item -> !itemNames.contains(item.getItemName()))
                .collect(Collectors.toList());
        List<MarketItemDTO> updateMarketItemDTOs = currentMarketItems.stream()
                .filter(item -> itemNames.contains(item.getItemName()))
                .collect(Collectors.toList());
        
        // add
        for (MarketItemDTO newItemDTO : newItemDTOs) {
            MarketItem newMarketItem = marketItemMapper.fromDtoToMarketItem(newItemDTO);
            newMarketItem = marketItemRepository.save(newMarketItem);
            log.info("syncItems: added new market item [{} - {}]", newMarketItem.getId(), newMarketItem.getName());
        }
        
        // update
        for (MarketItemDTO updateMarketItemDTO : updateMarketItemDTOs) {
            MarketItem marketItemDb = marketItemsDb.stream()
                    .filter(item -> item.getName().equals(updateMarketItemDTO.getItemName()))
                    .findFirst()
                    .get();
            marketItemMapper.updateMarketItem(marketItemDb, updateMarketItemDTO);
        }
        marketItemRepository.saveAll(marketItemsDb);
        
        // inactivate
        List<MarketItem> toInactivateItemsDb = new ArrayList<>();
        for (MarketItem marketItemDb : marketItemsDb) {
            if (!itemDtoNames.contains(marketItemDb.getName())) {
                toInactivateItemsDb.add(marketItemDb);
                marketItemDb.setEndDate(LocalDateTime.now());
            }
        }
        marketItemRepository.saveAll(toInactivateItemsDb);
    }

    private void syncLichWeapons(List<MarketLichWeapon> marketLichWeaponsDb, List<MarketLichWeaponDTO> currentMarketLichWeapons) throws IOException {
        List<String> lichWeaponNames = marketLichWeaponsDb.stream().map(lichWeapon -> lichWeapon.getName()).collect(Collectors.toList());
        List<String> lichWeaponDtoNames = currentMarketLichWeapons.stream().map(lichWeapon -> lichWeapon.getItemName()).collect(Collectors.toList());
        
        List<MarketLichWeaponDTO> newLichWeaponDTOs = currentMarketLichWeapons.stream()
                .filter(lichWeapon -> !lichWeaponNames.contains(lichWeapon.getItemName()))
                .collect(Collectors.toList());
        List<MarketLichWeaponDTO> updateMarketLichWeaponDTOs = currentMarketLichWeapons.stream()
                .filter(lichWeapon -> lichWeaponNames.contains(lichWeapon.getItemName()))
                .collect(Collectors.toList());
        
        // add
        for (MarketLichWeaponDTO newLichWeaponDTO : newLichWeaponDTOs) {
            MarketLichWeapon newMarketLichWeapon = marketLichWeaponMapper.fromDtoToLichWeapon(newLichWeaponDTO);
            newMarketLichWeapon = marketLichWeaponRepository.save(newMarketLichWeapon);
            log.info("syncLichWeapons: added new market lichWeapon [{} - {}]", newMarketLichWeapon.getId(), newMarketLichWeapon.getName());
        }
        
        // update
        for (MarketLichWeaponDTO updateMarketLichWeaponDTO : updateMarketLichWeaponDTOs) {
            MarketLichWeapon marketLichWeaponDb = marketLichWeaponsDb.stream()
                    .filter(lichWeapon -> lichWeapon.getName().equals(updateMarketLichWeaponDTO.getItemName()))
                    .findFirst()
                    .get();
            marketLichWeaponMapper.updateLichWeapon(marketLichWeaponDb, updateMarketLichWeaponDTO);
        }
        marketLichWeaponRepository.saveAll(marketLichWeaponsDb);
        
        // inactivate
        List<MarketLichWeapon> toInactivateLichWeaponsDb = new ArrayList<>();
        for (MarketLichWeapon marketLichWeaponDb : marketLichWeaponsDb) {
            if (!lichWeaponDtoNames.contains(marketLichWeaponDb.getName())) {
                toInactivateLichWeaponsDb.add(marketLichWeaponDb);
                marketLichWeaponDb.setEndDate(LocalDateTime.now());
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
