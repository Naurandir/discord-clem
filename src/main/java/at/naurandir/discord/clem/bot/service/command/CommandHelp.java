package at.naurandir.discord.clem.bot.service.command;

import at.naurandir.discord.clem.bot.model.WarframeState;
import discord4j.core.event.domain.message.MessageCreateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 *
 * @author Naurandir
 */
@Slf4j
@Component
public class CommandHelp implements Command {
    
    @Override
    public String getCommandWord() {
        return "help";
    }

    @Override
    public Mono<Void> handle(MessageCreateEvent event, WarframeState warframeState) {
        String message = "Help will be added sooner or later!";
        return event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage(message))
                .then();
    }
}
