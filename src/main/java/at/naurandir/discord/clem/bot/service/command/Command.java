package at.naurandir.discord.clem.bot.service.command;

import at.naurandir.discord.clem.bot.service.WarframeState;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

/**
 *
 * @author Naurandir
 */
public interface Command {
    
    String getCommandWord();
    Mono<Void> handle(MessageCreateEvent event, WarframeState warframeState);
    void push(GatewayDiscordClient client, WarframeState warframeState);
}
