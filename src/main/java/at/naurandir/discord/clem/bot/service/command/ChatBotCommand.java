package at.naurandir.discord.clem.bot.service.command;

import at.naurandir.discord.clem.bot.service.ChatBotService;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.spec.MessageCreateSpec;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 *
 * @author Naurandir
 */
@Slf4j
@Component
public class ChatBotCommand implements Command {
    
    @Autowired
    ChatBotService chatBotService;

    @Override
    public String getCommandWord() {
        return "chat";
    }

    @Override
    public String getDescription() {
                return "Ask the Bot something Warframe related.\n"
                + "Note this version ***remembers*** the context of a conversation for around 20 questions.\n"
                + "As ChatGPT can be under high load the answer sometimes take longer.\n"
                + "Usage: *<bot-prefix> chat <your question to the bot>*.\n"
                + "Example: *!clem chat What do you know about Warframe?*\n"
                + "To clear a user conversation use *!clem chat clear*";
    }

    @Override
    public Mono<Void> handle(MessageCreateEvent event) {
        String message = getMessage(event.getMessage().getContent());
        
        if (message.length() < 2) {
            return event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage("The given input [" + message + "] is too short. Please provide a longer text for the chat."))
                .then();
        }
        
        if (message.equals("clear")) {
            chatBotService.deleteConversation(event.getMessage().getAuthor());
            return event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage("The conversation with you has been cleared. I wont remember anything and we can start with a new conversation."))
                .then();
        }
        
        String response = chatBotService.chat(message, event.getMessage().getAuthor());
        
        return event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage(response))
                .then();
    }

    private String getMessage(String content) {
        String[] splitted = content.split(" "); // 'prefix chat <message>'
        String[] words = Arrays.copyOfRange(splitted, 2, splitted.length);
        return StringUtils.join(words, " ").trim();
    }
}
