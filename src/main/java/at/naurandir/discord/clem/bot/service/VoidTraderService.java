package at.naurandir.discord.clem.bot.service;

import at.naurandir.discord.clem.bot.model.trader.VoidTrader;
import at.naurandir.discord.clem.bot.model.trader.VoidTraderItem;
import at.naurandir.discord.clem.bot.model.trader.VoidTraderMapper;
import at.naurandir.discord.clem.bot.repository.VoidTraderItemRepository;
import at.naurandir.discord.clem.bot.repository.VoidTraderRepository;
import at.naurandir.discord.clem.bot.service.client.WarframeClient;
import at.naurandir.discord.clem.bot.service.client.dto.worldstate.VoidTraderDTO;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
public class VoidTraderService extends SyncService {

    @Autowired
    private VoidTraderMapper voidTraderMapper;
    
    @Autowired
    private VoidTraderRepository voidTraderRepository;
    
    @Autowired
    private VoidTraderItemRepository voidTraderItemRepository;
    
    @Value( "${discord.clem.void_trader.url}" )
    private String apiUrl;
    
    @Value("#{${discord.clem.void_trader.headers}}")
    private Map<String, String> apiHeaders;
    
    private final WarframeClient warframeClient = new WarframeClient();
    
    @Override
    @Scheduled(cron = "${discord.clem.void_trader.scheduler.cron}")
    public void sync() throws IOException {
        VoidTraderDTO currentVoidTrader = warframeClient.getData(apiUrl, apiHeaders, VoidTraderDTO.class);
        Optional<VoidTrader> voidTraderOpt = voidTraderRepository.findByExternalId(currentVoidTrader.getId());
        
        LocalDateTime now = LocalDateTime.now();
        
        if (voidTraderOpt.isEmpty()) {
            addVoidTrader(currentVoidTrader);
        } else if (voidTraderOpt.get().getInventory().isEmpty() && !currentVoidTrader.getInventory().isEmpty()) {
            addVoidTraderItems(currentVoidTrader, voidTraderOpt.get());
        } else if (now.isAfter(voidTraderOpt.get().getExpiry())) {
            inactivateVoidTrader(voidTraderOpt.get(), now);
        } else {
            log.debug("syncVoidTrader: void trader [id:{}, externalId:{}] existing in db and not expired, nothing to do.",
                    voidTraderOpt.get().getExternalId());
        }
    }

    private void addVoidTrader(VoidTraderDTO currentVoidTrader) {
        VoidTrader newVoidTrader = voidTraderMapper.voidTraderDtoToVoidTrader(currentVoidTrader);
        Set<VoidTraderItem> inventory = newVoidTrader.getInventory();
        newVoidTrader.setInventory(Set.of());
        
        VoidTrader dbVoidTrader = voidTraderRepository.save(newVoidTrader); // persist void trader      
        
        inventory.forEach(item -> item.setVoidTrader(dbVoidTrader));
        voidTraderItemRepository.saveAll(inventory);
        
        log.info("addVoidTrader: added void trader [id:{}, externalId:{}, items:{}]",
                newVoidTrader.getId(), newVoidTrader.getExternalId(), inventory.size());
    }
    
    private void addVoidTraderItems(VoidTraderDTO currentVoidTrader, VoidTrader dbVoidTrader) {
        Set<VoidTraderItem> inventory = voidTraderMapper.voidTraderItemDtoToVoidTraderItem(currentVoidTrader.getInventory());
        
        inventory.forEach(item -> item.setVoidTrader(dbVoidTrader));
        
        voidTraderItemRepository.saveAll(inventory);
        
        log.info("updateVoidTraderItems: updated void trader items [id:{}, externalId:{}, items:{}]",
                dbVoidTrader.getId(), dbVoidTrader.getExternalId(), inventory.size());
    }
    
    private void inactivateVoidTrader(VoidTrader voidTraderDb, LocalDateTime now) {
        log.info("inactivateVoidTrader: void trader [id:{}, externalId:{}] expired [{}], inactivate.",
                voidTraderDb.getId(), voidTraderDb.getExternalId(), voidTraderDb.getExpiry());
        
        voidTraderDb.setModifyDate(now);
        voidTraderDb.setEndDate(now);
        voidTraderRepository.save(voidTraderDb);
        
        voidTraderDb.getInventory().forEach(item -> item.setEndDate(now));
        voidTraderDb.getInventory().forEach(item -> item.setModifyDate(now));
        voidTraderItemRepository.saveAll(voidTraderDb.getInventory());
    }
    
    public Optional<VoidTrader> getVoidTrader() {
        return voidTraderRepository.findByEndDateIsNull();
    }
    
    @Override
    boolean isFirstTimeStartup() {
        return voidTraderRepository.findByEndDateIsNull().isEmpty();
    }
}
