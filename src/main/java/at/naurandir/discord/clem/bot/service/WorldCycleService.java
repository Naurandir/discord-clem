package at.naurandir.discord.clem.bot.service;

import at.naurandir.discord.clem.bot.model.cycle.Cycle;
import at.naurandir.discord.clem.bot.model.cycle.CycleMapper;
import at.naurandir.discord.clem.bot.model.cycle.CycleType;
import at.naurandir.discord.clem.bot.repository.CycleRepository;
import at.naurandir.discord.clem.bot.service.client.WarframeClient;
import at.naurandir.discord.clem.bot.service.client.dto.worldstate.CambionCycleDTO;
import at.naurandir.discord.clem.bot.service.client.dto.worldstate.CetusCycleDTO;
import at.naurandir.discord.clem.bot.service.client.dto.worldstate.EarthCycleDTO;
import at.naurandir.discord.clem.bot.service.client.dto.worldstate.VallisCycleDTO;
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
public class WorldCycleService {
    
    @Autowired
    private CycleMapper cycleMapper;
    
    @Autowired
    private CycleRepository cycleRepository;
    
    @Value( "${discord.clem.cycle.earth.url}" )
    private String apiEarthUrl;
    
    @Value( "${discord.clem.cycle.cetus.url}" )
    private String apiCetusUrl;
    
    @Value( "${discord.clem.cycle.vallis.url}" )
    private String apiVallisUrl;
    
    @Value( "${discord.clem.cycle.cambion.url}" )
    private String apiCambionUrl;
    
    @Value("#{${discord.clem.cycle.headers}}")
    private Map<String, String> apiHeaders;
    
    private final WarframeClient warframeClient = new WarframeClient();

    void syncCycles() throws IOException {
        List<Cycle> dbCycles = getCycles();
        EarthCycleDTO currentEarthCycle = warframeClient.getData(apiEarthUrl, apiHeaders, EarthCycleDTO.class);
        CetusCycleDTO currentCetusCycle = warframeClient.getData(apiCetusUrl, apiHeaders, CetusCycleDTO.class);
        VallisCycleDTO currentVallisCycle = warframeClient.getData(apiVallisUrl, apiHeaders, VallisCycleDTO.class);
        CambionCycleDTO currentCambionCycle = warframeClient.getData(apiCambionUrl, apiHeaders, CambionCycleDTO.class);
        
        if (dbCycles.isEmpty()) {
            createCycles(currentEarthCycle, currentCetusCycle, currentVallisCycle, currentCambionCycle);
        } else {
            updateCycles(dbCycles, currentEarthCycle, currentCetusCycle, currentVallisCycle, currentCambionCycle);
        }
    }

    private void createCycles(EarthCycleDTO currentEarthCycle, 
            CetusCycleDTO currentCetusCycle, 
            VallisCycleDTO currentVallisCycle, 
            CambionCycleDTO currentCambionCycle) {
        
        log.info("createCycles: creating new cycles...");
        List<Cycle> newCycles = new ArrayList<>();
        newCycles.add(cycleMapper.fromDtoToCycle(currentEarthCycle));
        newCycles.add(cycleMapper.fromDtoToCycle(currentCetusCycle));
        newCycles.add(cycleMapper.fromDtoToCycle(currentVallisCycle));
        newCycles.add(cycleMapper.fromDtoToCycle(currentCambionCycle));
        
        Iterable<Cycle> savedCycles = cycleRepository.saveAll(newCycles);
        for (Cycle savedCycle : savedCycles) {
            log.info("createCycles: created new cycle [{}]", savedCycle);
        }
    }

    private void updateCycles(List<Cycle> dbCycles, 
            EarthCycleDTO currentEarthCycle, 
            CetusCycleDTO currentCetusCycle, 
            VallisCycleDTO currentVallisCycle, 
            CambionCycleDTO currentCambionCycle) {
        
        for (Cycle dbCycle : dbCycles) {
            if (dbCycle.getCycleType() == CycleType.EARTH) {
                Cycle currentCycle = cycleMapper.fromDtoToCycle(currentEarthCycle);
                dbCycle.setExpiry(currentCycle.getExpiry());
                dbCycle.setCurrentState(currentCycle.getCurrentState());
                continue;
            }
            
            if (dbCycle.getCycleType() == CycleType.CETUS) {
                Cycle currentCycle = cycleMapper.fromDtoToCycle(currentCetusCycle);
                dbCycle.setExpiry(currentCycle.getExpiry());
                dbCycle.setCurrentState(currentCycle.getCurrentState());
                continue;
            }
            
            if (dbCycle.getCycleType() == CycleType.VALLIS) {
                Cycle currentCycle = cycleMapper.fromDtoToCycle(currentVallisCycle);
                dbCycle.setExpiry(currentCycle.getExpiry());
                dbCycle.setCurrentState(currentCycle.getCurrentState());
                continue;
            }
            
            if (dbCycle.getCycleType() == CycleType.CAMBION) {
                Cycle currentCycle = cycleMapper.fromDtoToCycle(currentCambionCycle);
                dbCycle.setExpiry(currentCycle.getExpiry());
                dbCycle.setCurrentState(currentCycle.getCurrentState());
                continue;
            }
            
            throw new IllegalArgumentException("no valid type was found!");
        }
        
        Iterable<Cycle> savedCycles = cycleRepository.saveAll(dbCycles);
        for (Cycle savedCycle : savedCycles) {
            log.info("updateCycles: updated cycle [{}]", savedCycle);
        }
    }
    
    public List<Cycle> getCycles() {
        return cycleRepository.findAll();
    }
}
