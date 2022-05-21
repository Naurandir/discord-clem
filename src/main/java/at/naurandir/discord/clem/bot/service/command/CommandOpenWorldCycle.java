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
    private static final String HANDLE_MESSAGE = "Cetus: {stateCetus} for {hoursCetus}h {minutesCetus}m.\n"
                                               + "Vallis: {stateVallis} for {hoursVallis}h {minutesVallis}m.";
    private static final String CETUS_PUSH_MESSAGE = "Cetus cycle changed: {newState} for {hours}h {minutes}m.";
    private static final String VALLIS_PUSH_MESSAGE = "Vallis cycle changed: {newState} for {hours}h {minutes}m.";
    
    @Override
    public String getCommandWord() {
        return "world-cycle";
    }

    @Override
    public Mono<Void> handle(MessageCreateEvent event, WarframeState warframeState) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cetusDiffTime = LocalDateTimeUtil.getDiffTime(now, warframeState.getCetusCycle().getExpiry());
        LocalDateTime vallisDiffTime = LocalDateTimeUtil.getDiffTime(now, warframeState.getVallisCycle().getExpiry());
        
        return event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage(
                        HANDLE_MESSAGE
                                .replace("{stateCetus}", warframeState.getCetusCycle().getState())
                                .replace("{hoursCetus}", String.valueOf(cetusDiffTime.getHour()))
                                .replace("{minutesCetus}", String.valueOf(cetusDiffTime.getMinute())
                                .replace("{stateVallis}", warframeState.getVallisCycle().getState())
                                .replace("{hoursVallis}", String.valueOf(vallisDiffTime.getHour())))
                                .replace("{minutesVallis}", String.valueOf(vallisDiffTime.getMinute()))))
                .then();
    }

    @Override
    public void push(GatewayDiscordClient client, WarframeState warframeState) {
        if (warframeState.isCetusCycleChanged()) {
            LocalDateTime cetusDiffTime = LocalDateTimeUtil.getDiffTime(LocalDateTime.now(), warframeState.getCetusCycle().getExpiry());
            sendMessageIntoChannels(client, CETUS_PUSH_MESSAGE
                                .replace("{newState}", warframeState.getCetusCycle().getState())
                                .replace("{hours}", String.valueOf(cetusDiffTime.getHour()))
                                .replace("{minutes}", String.valueOf(cetusDiffTime.getMinute())));
            
        }
        
        if (warframeState.isVallisCycleChanged()) {
            LocalDateTime vallisDiffTime = LocalDateTimeUtil.getDiffTime(LocalDateTime.now(), warframeState.getVallisCycle().getExpiry());
            sendMessageIntoChannels(client, VALLIS_PUSH_MESSAGE
                                .replace("{newState}", warframeState.getVallisCycle().getState())
                                .replace("{hours}", String.valueOf(vallisDiffTime.getHour()))
                                .replace("{minutes}", String.valueOf(vallisDiffTime.getMinute())));
            
        }
    }
    
    private void sendMessageIntoChannels(GatewayDiscordClient client, String message) {
        for (String channelId : interestingChannels) {
            client.rest().getChannelById(Snowflake.of(channelId))
                    .createMessage(message)
                    .subscribe();
        }
    }
    
}
