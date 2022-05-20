package at.naurandir.discord.clem.bot;

import at.naurandir.discord.clem.bot.command.Command;
import at.naurandir.discord.clem.bot.command.CommandGenerator;
import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import discord4j.core.object.presence.Presence;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
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
    
    @Value( "${discord.clem.command.prefix}" )
    private String prefix;
    
    @Autowired
    private List<CommandGenerator> commandGenerators;
    
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
        
        client.on(MessageCreateEvent.class, this::handleCommand).subscribe();
        
        log.info("init: starting up bot done, listening on: {}", prefix);
    }
    
    private Mono<Void> handleCommand(MessageCreateEvent event) {
        log.debug("handleCommand: received message create event [{}]", event.getMessage().getContent());
        return Flux.fromIterable(commandGenerators)
            .filter(commandGenerator -> event.getMessage().getContent().startsWith(
                        prefix + " " + commandGenerator.getCommand().getCommandWord()))
            .next()
            .flatMap(command -> command.getCommand().execute(event));
    }
    
    @PreDestroy
    public void destroy () {
        log.info("destroy: destroying bot...");
        client.logout();
        log.info("destroy: destroying bot done");
    }
    
    @Scheduled(fixedRate = 15_000)
    public void updateStatusForSilvester() throws InterruptedException, ExecutionException {
        client.updatePresence(ClientPresence.online(ClientActivity.listening(prefix)));
    }
}
