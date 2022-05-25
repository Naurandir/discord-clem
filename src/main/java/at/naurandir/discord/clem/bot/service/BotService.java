
package at.naurandir.discord.clem.bot.service;

import at.naurandir.discord.clem.bot.service.command.Command;
import at.naurandir.discord.clem.bot.service.push.Push;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
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
    
    private boolean isOwnBot(MessageCreateEvent event) {
        return event.getMember().isPresent() && 
               event.getMember().get().getId().equals(client.getSelfId());
    }
    
    private boolean isMessageToDelete(MessageCreateEvent event) {
        return event.getMessage().getType().equals(Message.Type.CHANNEL_PINNED_MESSAGE);
    }
    
    private Mono<Void> handleMessage(MessageCreateEvent event) {
        log.debug("handleCommand: received message create event [{} - {}]", 
                event.getMessage().getContent(), event.getMessage().getEmbeds());
        
        if (isOwnBot(event) && isMessageToDelete(event)) {
            event.getMessage()
                    .delete("not required message, like pin status message, can be deleted")
                    .subscribe();
        } else if (isOwnBot(event)) {
            handleOwnEvent(event);
        }
        
        return handleEvent(event, prefix);
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
    
    public void handleOwnEvent(MessageCreateEvent event) {
        pushes.forEach(push -> push.handleOwnEvent(event));
    }
    
    /**
     * refresh all information required from warframe every minute,
     * analyse the difference and push the difference into the channels
     * save the new state
     */
    @Scheduled(fixedRate = 60 * 1_000)
    public void refreshWarframe() {
        try {
            worldStateService.refreshWarframe();
            pushes.forEach(push -> push.push(client, worldStateService.getWarframeState()));
        } catch (Exception ex) {
            log.error("refreshWarframe: update throwed exception: ", ex);
        }
    }
    
}
