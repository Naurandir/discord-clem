package at.naurandir.discord.clem.bot.service.command;

import at.naurandir.discord.clem.bot.service.ChatBotService;
import discord4j.core.event.domain.message.MessageCreateEvent;
import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

/**
 *
 * @author Naurandir
 */
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
                + "Note This Version currently does not 'remember' a conversation what means you cannot use context here for older questions. \n"
                + "Note also that this Version does not convert the Bots formatting into according discord formatting and maybe look weird.\n"
                + "Also depending on the current Load of the ChatBot the answer can take a bit time. The Bot is using ChatGPT currently.\n"
                + "Usage: *<bot-prefix> chat <your question to the bot>*.\n"
                + "Example: *!clem chat What do you know about Warframe?*\n";
    }

    @Override
    public Mono<Void> handle(MessageCreateEvent event) {
        String message = getMessage(event.getMessage().getContent());
        
        if (message.length() < 5) {
            return event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage("The given input [" + message + "] is too short. Please provide a longer text for the chat."))
                .then();
        }
        
        return event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage(chatBotService.chat(message)))
                .then();
    }

    private String getMessage(String content) {
        String[] splitted = content.split(" "); // 'prefix chat <message>'
        String[] words = Arrays.copyOfRange(splitted, 2, splitted.length);
        return StringUtils.join(words, " ").trim();
    }
}
