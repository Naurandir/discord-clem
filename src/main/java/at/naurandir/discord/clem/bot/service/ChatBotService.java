package at.naurandir.discord.clem.bot.service;

import at.naurandir.discord.clem.bot.service.client.WarframeClient;
import at.naurandir.discord.clem.bot.service.client.dto.chat.ChatGptDTO;
import at.naurandir.discord.clem.bot.service.client.dto.chat.ChatGptRequestDTO;
import java.io.IOException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 *
 * @author Naurandir
 */
@Slf4j
@Service
public class ChatBotService {
    
    @Value("${discord.clem.chat.url}")
    private String url;
    
    @Value("#{${discord.clem.chat.headers}}")
    private Map<String, String> apiHeaders;
    
    @Value("${discord.clem.chat.model}")
    private String model;
    
    @Value("${discord.clem.chat.tokens.response}")
    private Integer tokensRepsonse;
    
    @Value("${discord.clem.chat.tokens.total}")
    private Integer tokensTotal;
    
    @Value("${discord.clem.chat.temperature}")
    private Double temperature;
    
    @Value("${discord.clem.chat.prompt.prefix}")
    private String prefix;
    
    private final WarframeClient warframeClient = new WarframeClient();
    
    public String chat(String message) {
        try {
            String finalMessage = prefix + message;
            Integer numberOfTokens = finalMessage.split(" ").length;
            log.debug("chat: having [{}] tokens used, [{}] tokens left for message", 
                    numberOfTokens, tokensTotal-tokensRepsonse-numberOfTokens);
            
            ChatGptRequestDTO body = createRequestBody(finalMessage);
            ChatGptDTO answer = warframeClient.getDataByPost(url, apiHeaders, body, ChatGptDTO.class);
            return answer.getChoices().get(0).getText();
        } catch (IOException ex) {
            log.error("chat: could not receive a valid answer from chat bot: ", ex);
            return "I am sorry, something went wrong with calling the chatbot with your request, maybe try later again.\n" +
                    "The error was: " + ex.getMessage();
        }
    }

    private ChatGptRequestDTO createRequestBody(String message) {
        return ChatGptRequestDTO.builder()
                .model(model)
                .prompt(message)
                .maxTokens(tokensRepsonse)
                .temperature(temperature)
                .build();
    }
}
