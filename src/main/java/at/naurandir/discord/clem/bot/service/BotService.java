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

import java.time.Duration;
import java.util.List;
import java.util.TimeZone;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    
    private GatewayDiscordClient client;
    
    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));// set time zone UTC
        
        initBot();
    }
    
    private void initBot() {
        client = DiscordClient.create(discordToken)
                .gateway()
                .setInitialPresence(s -> ClientPresence.online(ClientActivity.listening(prefix)))
                .login()
                .block();
        
        client.on(MessageCreateEvent.class, this::handleMessage).timeout(Duration.ofSeconds(60)).subscribe();
        client.on(MessageDeleteEvent.class, this::handleMessage).timeout(Duration.ofSeconds(60)).subscribe();
        pushes.forEach(push -> push.init(client));
    }
    
    @PreDestroy
    public void destroy () {
        log.info("destroy: destroying bot...");
        client.logout();
        log.info("destroy: destroying bot done");
    }
    
    private Mono<Void> handleMessage(MessageCreateEvent event) {
        if (log.isDebugEnabled()) {
            log.debug("handleMessage: received message create event [{} - {}]", 
                event.getMessage().getContent(), event.getMessage().getEmbeds());
        }
        
        if (isOwnBot(event) && isPinnedMessage(event)) {
            event.getMessage()
                    .delete("not required message, like pin status message, can be deleted")
                    .timeout(Duration.ofSeconds(60)).subscribe();
            return Flux.empty().then();
        } else if (isOwnBot(event)) {
            pushes.forEach(push -> push.handleOwnEvent(event));
            return Flux.empty().then();
        } else {
            return handleEvent(event, prefix);
        }
    }
    
    private Mono<Void> handleMessage(MessageDeleteEvent event) {
        if (log.isDebugEnabled()) {
            log.debug("handleMessage: received message delete event [{}]", 
                event.getMessage());
        }
        
        if (event.getMessage().isEmpty()) {
            return Flux.empty().then();
        }
        
        if (!isOwnBot(event)) {
            return Flux.empty().then();
        }
        
        pushes.forEach(push -> push.handleDeleteOwnEvent(event));
        return Flux.empty().then();
    }
    
    private Mono<Void> handleEvent(MessageCreateEvent event, String prefix) {
        if (event.getMember().isPresent() && event.getMember().get().isBot()) {
            return Flux.empty().then();
        }
               
        return Flux.fromIterable(commands)
                .filter(command -> isCommandWordUsage(event, command) || isBotMentionedUsage(event, command))
                .next()
                .flatMap(command -> command.handle(event));
    }

    private boolean isCommandWordUsage(MessageCreateEvent event, Command command) {
        return event.getMessage().getContent().startsWith(prefix + " " + command.getCommandWord());
    }
    
    private boolean isBotMentionedUsage(MessageCreateEvent event, Command command) {
        String mentionedCommand = "<@" + client.getSelfId().asString() +"> " + command.getCommandWord();
        return event.getMessage().getContent().startsWith(mentionedCommand);
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
    
    public GatewayDiscordClient getClient() {
        return client;
    }
}
