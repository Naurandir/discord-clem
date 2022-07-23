package at.naurandir.discord.clem.bot.service.command;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

/**
 *
 * @author Naurandir
 */
public interface Command {
    
    String getCommandWord();
    String getDescription();
    Mono<Void> handle(MessageCreateEvent event);
}
