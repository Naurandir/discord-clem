package at.naurandir.discord.clem.bot;

import at.naurandir.discord.clem.bot.service.WarframeService;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message.Type;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
/**
 *
 * @author Naurandir
 */
@Slf4j
@Component
public class ClemBot {
    
    @Value( "${discord.clem.token}" )
    private String discordToken;
    
    @Value( "${discord.clem.id}" )
    private String id;
    
    @Value( "${discord.clem.command.prefix}" )
    private String prefix;
    
    @Autowired
    private WarframeService warframeService;
    
    private GatewayDiscordClient client;
    
    @PostConstruct
    public void init() throws InterruptedException, ExecutionException {
        log.info("init: starting up bot...");
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));// set time zone UTC
        
        client = DiscordClient.create(discordToken)
                .gateway()
                .setInitialPresence(s -> ClientPresence.online(ClientActivity.listening(prefix)))
                .login()
                .block();
        
        client.on(MessageCreateEvent.class, this::handleMessage).subscribe();
        
        log.info("init: starting up bot done, listening on: {}", prefix);
    }
    
    private Mono<Void> handleMessage(MessageCreateEvent event) {
        log.debug("handleCommand: received message create event [{}]", event.getMessage());
        if (isOwnBot(event) && isMessageToDelete(event)) {
            event.getMessage()
                    .delete("not required message, like pin status message, can be deleted")
                    .subscribe();
        }else if (isOwnBot(event)) {
            warframeService.handleOwnEvent(event, client);
        }
        
        return warframeService.handleEvent(event, prefix);
    }

    private boolean isOwnBot(MessageCreateEvent event) {
        return event.getMember().isPresent() && event.getMember().get().getId().asString().equals(id);
    }
    
    private boolean isMessageToDelete(MessageCreateEvent event) {
        return event.getMessage().getType().equals(Type.CHANNEL_PINNED_MESSAGE);
    }
    
    @PreDestroy
    public void destroy () {
        log.info("destroy: destroying bot...");
        client.logout();
        log.info("destroy: destroying bot done");
    }
    
    @Scheduled(fixedRate = 1 * 60 * 1_000)
    public void updateStatus() throws InterruptedException, ExecutionException {
        log.info("updateStatus: updating current status...");
        warframeService.refreshWarframe(client);
        log.info("updateStatus: updating current status finished.");
    }
}
