
package at.naurandir.discord.clem.bot.service;

import at.naurandir.discord.clem.bot.service.command.Command;
import at.naurandir.discord.clem.bot.service.push.Push;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.MessageDeleteEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import java.util.List;
import java.util.TimeZone;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 *
 * @author Naurandir
 */
@Slf4j
@Service
public class BotService {
    
    @Value( "${discord.clem.token}" )
    private String discordToken;
    
    @Value( "${discord.clem.command.prefix}" )
    private String prefix;
    
    @Autowired
    private List<Command> commands;
    
    @Autowired
    private List<Push> pushes;
    
    @Autowired
    private WorldStateService worldStateService;
    
    private GatewayDiscordClient client;
    
    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));// set time zone UTC
        
        initBot();
        initPushes();
    }
    
    private void initBot() {
        client = DiscordClient.create(discordToken)
                .gateway()
                .setInitialPresence(s -> ClientPresence.online(ClientActivity.listening(prefix)))
                .login()
                .block();
        
        client.on(MessageCreateEvent.class, this::handleMessage).subscribe();
        client.on(MessageDeleteEvent.class, this::handleMessage).subscribe();
    }
    
    private void initPushes() {
        pushes.forEach(push -> push.init(client));
    }
    
    @PreDestroy
    public void destroy () {
        log.info("destroy: destroying bot...");
        client.logout();
        log.info("destroy: destroying bot done");
    }
    
    private Mono<Void> handleMessage(MessageCreateEvent event) {
        log.debug("handleMessage: received message create event [{} - {}]", 
                event.getMessage().getContent(), event.getMessage().getEmbeds());
        
        if (isOwnBot(event) && isPinnedMessage(event)) {
            event.getMessage()
                    .delete("not required message, like pin status message, can be deleted")
                    .subscribe();
            return Flux.empty().then();
        } else if (isOwnBot(event)) {
            pushes.forEach(push -> push.handleOwnEvent(event));
            return Flux.empty().then();
        } else {
            return handleEvent(event, prefix);
        }
    }
    
    public Mono<Void> handleEvent(MessageCreateEvent event, String prefix) {
        if (event.getMember().isPresent() && event.getMember().get().isBot()) {
            return Flux.empty().then();
        }
        
        return Flux.fromIterable(commands)
                .filter(command -> event.getMember().isEmpty() || !event.getMember().get().isBot())
                .filter(command -> event.getMessage().getContent().startsWith(
                        prefix + " " + command.getCommandWord()))
                .next()
                .flatMap(command -> command.handle(event, worldStateService.getWarframeState()));
    }
    
    private Mono<Void> handleMessage(MessageDeleteEvent event) {
        log.debug("handleMessage: received message delete event [{}]", 
                event.getMessage());
        
        if (event.getMessage().isEmpty()) {
            return Flux.empty().then();
        }
        
        if (!isOwnBot(event)) {
            return Flux.empty().then();
        }
        
        pushes.forEach(push -> push.handleDeleteOwnEvent(event));
        return Flux.empty().then();
    }
    
    private boolean isOwnBot(MessageCreateEvent event) {
        return event.getMember().isPresent() && 
               event.getMember().get().getId().equals(client.getSelfId());
    }
    
    private boolean isOwnBot(MessageDeleteEvent event) {
        return event.getMessage().isPresent() &&
               event.getMessage().get().getData().author().id().asLong() == client.getSelfId().asLong();
    }
    
    private boolean isPinnedMessage(MessageCreateEvent event) {
        return event.getMessage().getType().equals(Message.Type.CHANNEL_PINNED_MESSAGE);
    }
    
    /**
     * refresh all information required from warframe every minute,
     * analyse the difference and push the difference into the channels
     * save the new state
     */
    @Scheduled(fixedRate = 60 * 1_000)
    public void refreshWorldState() {
        try {
            worldStateService.refreshWorldState();
            pushes.forEach(push -> push.push(client, worldStateService.getWarframeState()));
        } catch (Exception ex) {
            log.error("refreshWorldState: update throwed exception: ", ex);
        }
    }
    
    /**
     * once per hour we refresh the drop tables
     */
    @Scheduled(fixedRate = 60 * 60 * 1_000)
    public void refreshDropTables() {
        try {
            worldStateService.refreshDropTable();
        } catch (Exception ex) {
            log.error("refreshDropTables: update throwed exception: ", ex);
        }
    }
    
    
    /**
     * once per day we refresh the market data
     */
    @Scheduled(fixedRate = 24 * 60 * 60 * 1_000)
    public void refreshMarket() {
        try {
            worldStateService.refreshMarket();
        } catch (Exception ex) {
            log.error("refreshMarket: update throwed exception: ", ex);
        }
    }
}
