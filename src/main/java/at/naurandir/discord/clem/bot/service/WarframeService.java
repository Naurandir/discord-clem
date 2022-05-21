
package at.naurandir.discord.clem.bot.service;

import at.naurandir.discord.clem.bot.service.command.Command;
import at.naurandir.discord.clem.bot.service.command.CommandTest;
import at.naurandir.discord.clem.bot.service.client.WarframeClient;
import at.naurandir.discord.clem.bot.service.client.dto.WorldStateDTO;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 *
 * @author Naurandir
 */
@Slf4j
@Service
public class WarframeService {
    
    @Autowired
    private List<Command> commands;
    
    private WarframeClient client;
    private WarframeState warframeState;
    
    @PostConstruct
    public void init() throws IOException {
        client = new WarframeClient();
        warframeState = new WarframeState();
        
        WorldStateDTO newWorldState = client.getCurrentWorldState();
        warframeState.setCetusCycle(newWorldState.getCetusCycleDTO());
    }
    
    public List<Command> getCommands() {
        return commands;
    }
    
    /**
     * enrich the handle command with the current state from warframe
     * @param command
     * @param event
     * @return 
     */
    public Mono<Void> handle(Command command, MessageCreateEvent event) {
        return command.handle(event, warframeState);
    }
    
    /**
     * refresh all information required from warframe,
     * analyse the difference and push the difference into the channels
     * save the new state
     * @param discordClient 
     */
    public void refreshWarframe(GatewayDiscordClient discordClient) {
        try {
            WorldStateDTO newWorldState = client.getCurrentWorldState();
            log.info("getCurrentWorldState: received dto [{}]", newWorldState);
            
            updateWorldCycles(newWorldState);
            
            // push notifications for diff
            commands.forEach(command -> command.push(discordClient, warframeState));
            
        } catch (Exception ex) {
            log.error("getCurrentWorldState: update throwed exception: ", ex);
        }
    }

    private void updateWorldCycles(WorldStateDTO newWorldState) {
        // cetus
        boolean isCetusCycleChanged = !Objects.equals(newWorldState.getCetusCycleDTO().getIsDay(),
                warframeState.getCetusCycle().getIsDay());
        warframeState.setCetusCycleChanged(isCetusCycleChanged);
        warframeState.setCetusCycle(newWorldState.getCetusCycleDTO());
        
        // fortuna
        
    }
    
    
}
