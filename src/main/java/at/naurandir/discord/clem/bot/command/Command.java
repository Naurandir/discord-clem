package at.naurandir.discord.clem.bot.command;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

/**
 *
 * @author Naurandir
 */
public interface Command {
    Mono<Void> execute(MessageCreateEvent event);
    // push method
    String getCommandWord();
}
