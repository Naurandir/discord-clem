package at.naurandir.discord.clem.bot.service.command;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 *
 * @author Naurandir
 */
@Slf4j
@Component
public class HelpCommand implements Command {
    
    private static final String COMMAND_DESCRIPTION = "***{command}*** - {description}\n\n";
    
    @Autowired
    private List<Command> commands;
    
    @Override
    public String getCommandWord() {
        return "help";
    }
    
    @Override
    public String getDescription() {
        return "This command shows all currently existing commands available.";
    }

    @Override
    public Mono<Void> handle(MessageCreateEvent event) {
        StringBuilder description = new StringBuilder();
        commands.forEach(command -> description.append(COMMAND_DESCRIPTION.replace("{command}", command.getCommandWord())
                        .replace("{description}", command.getDescription())));
        
        return event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage(generateEmbed(description.toString())))
                .then();
    }
    
    private EmbedCreateSpec generateEmbed(String description) {         
        return EmbedCreateSpec.builder()
                .color(Color.BISMARK)
                .title("List of available Commands")
                .description(description)
                .thumbnail("https://content.invisioncic.com/Mwarframe/pages_media/1_GlyphClem.png")
                .timestamp(Instant.now())
                .build();
    }
}
