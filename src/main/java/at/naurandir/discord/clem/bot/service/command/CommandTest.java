package at.naurandir.discord.clem.bot.service.command;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 *
 * @author Naurandir
 */
@Slf4j
@Service
public class CommandTest implements Command {

    @Override
    public Mono<Void> execute(MessageCreateEvent event) {
        log.info("execute: received message [id:{}, guildId:{}, channelId:{}, content:{}]", 
                event.getMessage().getId(), event.getGuildId(), event.getMessage().getChannelId(), event.getMessage().getContent());
        Message message = event.getMessage();
        
        return event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage(generateEmbed(message)))
                .then();
    }

    @Override
    public String getCommandWord() {
        return "test";
    }
    
    private EmbedCreateSpec generateEmbed(Message message) { 
        String id;
        String user;
        try {
            user = message.getAuthorAsMember().toFuture().get().getDisplayName();
            id = message.getAuthorAsMember().toFuture().get().getId().asString();
        } catch (Exception ex) {
            log.warn("generateEmbed: could not obtain author as member: [{}], fallback to simple author", ex.getMessage());
            user = message.getAuthor().get().getUsername();
            id = message.getAuthor().get().getUsername();
        }
        
        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .color(Color.BLUE)
                .title("Title")
                .url("https://discord4j.com")
                .author("Some Name", "https://discord4j.com", "https://i.imgur.com/F9BhEoz.png")
                .description("a description")
                .thumbnail("https://i.imgur.com/F9BhEoz.png")
                .addField("field title", "value", false)
                .addField("\u200B", "\u200B", false)
                .addField("inline field", "value", true)
                .addField("inline field", "value", true)
                .addField("inline field", "value", true)
                .image("https://i.imgur.com/F9BhEoz.png")
                .timestamp(Instant.now())
                .footer("footer", "https://i.imgur.com/F9BhEoz.png")
                .build();
        
        log.info("generateEmbed: generated [test for {} - {}]", user, id);
        
        return embed;
    }
}
