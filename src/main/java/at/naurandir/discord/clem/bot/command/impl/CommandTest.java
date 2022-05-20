package at.naurandir.discord.clem.bot.command.impl;

import at.naurandir.discord.clem.bot.command.Command;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 *
 * @author Naurandir
 */
@Slf4j
@Setter
public class CommandTest implements Command {

    @Override
    public Mono<Void> execute(MessageCreateEvent event) {
        log.info("execute: received message [id:{}, guildId:{}, channelId:{}, content:{}]", 
                event.getMessage().getId(), event.getGuildId(), event.getMessage().getChannelId(), event.getMessage().getContent());
        return event.getMessage().getChannel()
                .flatMap(channel -> channel.createEmbed(spec -> generateEmbed(spec, event.getMessage())))
                .then();
    }

    @Override
    public String getCommandWord() {
        return "test";
    }
    
    private void generateEmbed(EmbedCreateSpec spec, Message message) { 
        
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
        
        spec.setTitle("Testmessage");
        spec.setDescription("Content of test message");
        
        log.info("generateEmbed: generated [test for {} - {}]", user, id);
    }
}
