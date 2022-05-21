package at.naurandir.discord.clem.bot;

import at.naurandir.discord.clem.bot.service.WarframeService;
import at.naurandir.discord.clem.bot.service.command.Command;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.Channel.Type;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import java.util.List;
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
    private WarframeService warframeService;
    
    @Autowired
    private List<Command> commands;
    
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
        return Flux.fromIterable(commands)
            .filter(command -> event.getMessage().getContent().startsWith(
                        prefix + " " + command.getCommandWord()))
            .next()
            .flatMap(command -> command.execute(event));
    }
    
    @PreDestroy
    public void destroy () {
        log.info("destroy: destroying bot...");
        client.logout();
        log.info("destroy: destroying bot done");
    }
    
    @Scheduled(fixedRate = 5 * 60 * 1_000)
    public void updateStatus() throws InterruptedException, ExecutionException {
        log.info("updateStatus: updating current status...");
        client.updatePresence(ClientPresence.online(ClientActivity.listening(prefix)));
        warframeService.updateWorldState(client);
        log.info("updateStatus: updating current status finished.");
        
        client.getChannelById(Snowflake.of("977443949352472626"))
                .filter(channel -> channel.getType().equals(Type.GUILD_TEXT))
                .flatMap(channel -> ((TextChannel) channel).createMessage("updated"))
                .then();
    }
}
