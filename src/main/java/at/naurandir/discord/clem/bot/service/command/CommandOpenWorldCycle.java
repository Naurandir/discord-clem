package at.naurandir.discord.clem.bot.service.command;

import at.naurandir.discord.clem.bot.service.WarframeState;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 *
 * @author Naurandir
 */
@Slf4j
@Component
public class CommandOpenWorldCycle implements Command {
    
    @Value("#{'${discord.clem.channels.worldcycle}'.split(',')}")
    private List<String> interestingChannels;
    
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    private final String pushCetusChanged = "Cetus cycle changed: {newState} - {minutes}m.";
    private final String worldCycleState = "Cetus: {state} - {minutes}m.";
    
    @Override
    public String getCommandWord() {
        return "world-cycle";
    }

    @Override
    public Mono<Void> handle(MessageCreateEvent event, WarframeState warframeState) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiry = LocalDateTime.parse(warframeState.getCetusCycle().getExpiry(), formatter);
        String cetusDiffMinutes = String.valueOf(now.until(expiry, ChronoUnit.MINUTES));
        
        return event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage(
                        worldCycleState
                                .replace("{state}", warframeState.getCetusCycle().getState())
                                .replace("{minutes}", cetusDiffMinutes)))
                .then();
    }

    @Override
    public void push(GatewayDiscordClient client, WarframeState warframeState) {
        if (!warframeState.isCetusCycleChanged()) {
            return;
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiry = LocalDateTime.parse(warframeState.getCetusCycle().getExpiry(), formatter);
        String cetusDiffMinutes = String.valueOf(now.until(expiry, ChronoUnit.MINUTES));
        
        for (String channelId : interestingChannels) {
            client.rest().getChannelById(Snowflake.of(channelId))
                    .createMessage(pushCetusChanged
                            .replace("{newState}", warframeState.getCetusCycle().getState())
                            .replace("{minutes}", cetusDiffMinutes))
                    .subscribe();
        }
    }
    
}
