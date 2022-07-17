package at.naurandir.discord.clem.bot.service;

import at.naurandir.discord.clem.bot.model.fissure.VoidFissureMapper;
import at.naurandir.discord.clem.bot.repository.VoidFissureRepository;
import at.naurandir.discord.clem.bot.service.client.WarframeClient;
import at.naurandir.discord.clem.bot.service.client.dto.worldstate.VoidFissureDTO;
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
public class VoidFissureService {
    
    @Autowired
    private VoidFissureMapper voidFissureMapper;
    
    @Autowired
    private VoidFissureRepository voidFissureRepository;

    @Value( "${discord.clem.void_fissure.url}" )
    private String apiUrl;
    
    @Value("#{${discord.clem.void_fissure.headers}}")
    private Map<String, String> apiHeaders;
    
    private final WarframeClient warframeClient = new WarframeClient();
    
    public void syncFissures() throws IOException {
        List<VoidFissureDTO> currentFissures = warframeClient.getListData(apiUrl, apiHeaders, VoidFissureDTO.class);
    }
}
