package at.naurandir.discord.clem.bot.service.command;

import at.naurandir.discord.clem.bot.service.WarframeState;
import at.naurandir.discord.clem.bot.utils.LocalDateTimeUtil;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import java.time.LocalDateTime;
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
    
    private final String pushCetusChanged = "Cetus cycle changed: {newState} for {hours}h {minutes}m.";
    private final String worldCycleState = "Cetus: {state} for {hours}h {minutes}m.";
    
    @Override
    public String getCommandWord() {
        return "world-cycle";
    }

    @Override
    public Mono<Void> handle(MessageCreateEvent event, WarframeState warframeState) {
        LocalDateTime cetusDiffTime = LocalDateTimeUtil.getDiffTime(LocalDateTime.now(), warframeState.getCetusCycle().getExpiry());
        
        return event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage(
                        worldCycleState
                                .replace("{state}", warframeState.getCetusCycle().getState())
                                .replace("{hours}", String.valueOf(cetusDiffTime.getHour()))
                                .replace("{minutes}", String.valueOf(cetusDiffTime.getMinute()))))
                .then();
    }

    @Override
    public void push(GatewayDiscordClient client, WarframeState warframeState) {
        if (!warframeState.isCetusCycleChanged()) {
            return;
        }
        
        LocalDateTime cetusDiffTime = LocalDateTimeUtil.getDiffTime(LocalDateTime.now(), warframeState.getCetusCycle().getExpiry());
        
        for (String channelId : interestingChannels) {
            client.rest().getChannelById(Snowflake.of(channelId))
                    .createMessage(pushCetusChanged
                                .replace("{hours}", String.valueOf(cetusDiffTime.getHour()))
                                .replace("{minutes}", String.valueOf(cetusDiffTime.getMinute())))
                    .subscribe();
        }
    }
    
}
