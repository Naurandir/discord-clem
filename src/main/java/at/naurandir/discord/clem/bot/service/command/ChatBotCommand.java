package at.naurandir.discord.clem.bot.service.command;

import java.time.Duration;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import at.naurandir.discord.clem.bot.service.ChatBotService;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.MessageEditSpec;
import lombok.extern.slf4j.Slf4j;
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
                + "Note this version ***remembers*** (max 15 questions).\n"
                + "Usage: *<bot-prefix> chat <question>*.\n"
                + "Example: *!clem chat What is Warframe?*\n"
                + "To clear a user conversation use *!clem chat clear*\n"
                + "To talk offtopic use *!clem chat offtopic <question>*";
    }

    @Override
    public Mono<Void> handle(MessageCreateEvent event) {
        String message = getMessage(event.getMessage().getContent());
        
        if (message.length() < 2) {
            return event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage("The given input [" + message + "] is too short. Please provide a longer text for the chat."))
                .timeout(Duration.ofSeconds(60))
                .then();
        }
        
        if (message.equals("clear")) {
            chatBotService.deleteConversation(event.getMessage().getAuthor());
            return event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage("The conversation with you has been cleared. I wont remember anything and we can start with a new conversation."))
                .timeout(Duration.ofSeconds(60))
                .then();
        }
        
        String finalMessage = message;
        boolean isOfftopic = false;
        if (finalMessage.startsWith("offtopic")) {
            isOfftopic = true;
            finalMessage = finalMessage.replace("offtopic", "");
        }
        
        MessageChannel channel = event.getMessage().getChannel().block();
        Message processingMessage = channel.createMessage(":knot: processing...").block();
        
        String response = chatBotService.chat(finalMessage, event.getMessage().getAuthor(), isOfftopic);
        
        return processingMessage
                .edit(MessageEditSpec.builder().contentOrNull(response).build())
                .timeout(Duration.ofSeconds(60))
                .then();
    }

    private String getMessage(String content) {
        String[] splitted = content.split(" "); // 'prefix chat <message>'
        String[] words = Arrays.copyOfRange(splitted, 2, splitted.length);
        return StringUtils.join(words, " ").trim();
    }
}
