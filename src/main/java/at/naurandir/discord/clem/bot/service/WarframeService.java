
package at.naurandir.discord.clem.bot.service;

import at.naurandir.discord.clem.bot.service.command.Command;
import at.naurandir.discord.clem.bot.service.client.WarframeClient;
import at.naurandir.discord.clem.bot.service.client.dto.WorldStateDTO;
import at.naurandir.discord.clem.bot.service.push.Push;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
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
    
    @Autowired
    private List<Push> pushes;
    
    private WarframeClient warframeClient;
    private WarframeState warframeState;
    
    @PostConstruct
    public void init() throws IOException {
        warframeClient = new WarframeClient();
        warframeState = new WarframeState();
        
        WorldStateDTO newWorldState = warframeClient.getCurrentWorldState();
        warframeState.updateByWorldState(newWorldState);        
        warframeState.setVoidTraderStateChanged(false);
        warframeState.setAlertsStateChanged(false);
    }
    
    public List<Command> getCommands() {
        return commands;
    }
    
    public List<Push> getPushes() {
        return pushes;
    }
    
    public void initPushes(GatewayDiscordClient client) {
        pushes.forEach(push -> push.init(client));
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
    
    public Mono<Void> handleEvent(MessageCreateEvent event, String prefix) {
        if (event.getMember().isPresent() && event.getMember().get().isBot()) {
            return Flux.empty().then();
        }
        
        return Flux.fromIterable(commands)
                .filter(command -> event.getMessage().getContent().startsWith(
                        prefix + " " + command.getCommandWord()))
                .next()
                .flatMap(command -> command.handle(event, warframeState));
    }
    
    public void handleOwnEvent(MessageCreateEvent event) {
        pushes.forEach(push -> push.handleOwnEvent(event));
    }
    
    /**
     * refresh all information required from warframe,
     * analyse the difference and push the difference into the channels
     * save the new state
     * @param discordClient 
     */
    public void refreshWarframe(GatewayDiscordClient discordClient) {
        try {
            WorldStateDTO newWorldState = warframeClient.getCurrentWorldState();
            log.debug("getCurrentWorldState: received dto.");
            
            // state change check
            checkAlertsChanged(newWorldState);
            checkVoidTraderChanged(newWorldState);
            
            // set new state
            warframeState.updateByWorldState(newWorldState);
            
            // push notifications for diff
            pushes.forEach(push -> push.push(discordClient, warframeState));
        } catch (Exception ex) {
            log.error("getCurrentWorldState: update throwed exception: ", ex);
        }
    }  

    private void checkAlertsChanged(WorldStateDTO newWorldState) {
        warframeState.setAlertsStateChanged((warframeState.getAlerts() == null && newWorldState.getAlertsDTO() != null) ||
                                            (warframeState.getAlerts() != null && newWorldState.getAlertsDTO() == null) ||
                                            warframeState.getAlerts().size() != newWorldState.getAlertsDTO().size());
    }

    private void checkVoidTraderChanged(WorldStateDTO newWorldState) {
        warframeState.setVoidTraderStateChanged(!Objects.equals(warframeState.getVoidTrader().getActive(), newWorldState.getVoidTraderDTO().getActive()));
    }
    
}
