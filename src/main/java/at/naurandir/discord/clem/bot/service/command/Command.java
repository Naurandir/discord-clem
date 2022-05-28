package at.naurandir.discord.clem.bot.service.command;

import at.naurandir.discord.clem.bot.model.WarframeState;
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

/**
 *
 * @author Naurandir
 */
public interface Command {
    
    String getCommandWord();
    String getDescription();
    Mono<Void> handle(MessageCreateEvent event, WarframeState warframeState);
}
