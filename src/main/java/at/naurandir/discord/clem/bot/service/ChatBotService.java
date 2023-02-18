package at.naurandir.discord.clem.bot.service;

import at.naurandir.discord.clem.bot.model.chat.Conversation;
import at.naurandir.discord.clem.bot.model.chat.ConversationMessage;
import at.naurandir.discord.clem.bot.model.chat.ConversationMessage.ChatMember;
import at.naurandir.discord.clem.bot.repository.ConversationRepository;
import at.naurandir.discord.clem.bot.service.client.WarframeClient;
import at.naurandir.discord.clem.bot.service.client.dto.chat.ChatGptDTO;
import at.naurandir.discord.clem.bot.service.client.dto.chat.ChatGptRequestDTO;
import discord4j.core.object.entity.User;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
    
    @Autowired
    private ConversationRepository conversationRepository;
    
    private final WarframeClient warframeClient = new WarframeClient();
    
    @Transactional
    public String chat(String userMessage, Optional<User> user) {
        try {
            Conversation conversation = loadConversation(user);
            addToConversation(userMessage, ChatMember.HUMAN, conversation);
            
            String prompt = generatePrompt(userMessage, conversation);
            ChatGptRequestDTO body = createRequestBody(prompt);
            ChatGptDTO answerDTO = warframeClient.getDataByPost(url, apiHeaders, body, ChatGptDTO.class);
            
            if (answerDTO == null || answerDTO.getChoices() == null || answerDTO.getChoices().isEmpty()) {
                log.error("chat: did not receive a valid answer, answer was: [{}]", answerDTO);
                throw new IOException("It seems that a problem occured and the chatbot did not respond with a valid answer.");
            }
           
            String answer = answerDTO.getChoices().get(0).getText();
            answer = answer.replace("AI: ", "").replace("AI:", "")
                           .replace("A: ", "").replace("A:", "")
                           .replace("\n", "");
            
            if (answer.contains("?") && answer.indexOf("?") <= 10) {
                answer = answer.substring(answer.indexOf("?") + 1);
            }
            
            addToConversation(answer, ChatMember.AI, conversation);
            saveConversation(conversation);
            
            return answer;
        } catch (IOException ex) {
            log.error("chat: could not receive a valid answer from chat bot: ", ex);
            return "I am sorry, something went wrong with calling the chatbot with your request, maybe try later again or clear your conversation.\n" +
                    "The error was: " + ex.getMessage();
        }
    }
    
    @Transactional
    public void deleteConversation(Optional<User> user) {
        Conversation conversation = loadConversation(user);
        if (conversation == null) {
            return ; // no conversation to clear
        }
        log.debug("deleteConversation: deleting conversation [{}]", conversation.getId());
        conversationRepository.delete(conversation);
    }

    private ChatGptRequestDTO createRequestBody(String message) {
        return ChatGptRequestDTO.builder()
                .model(model)
                .prompt(message)
                .maxTokens(tokensRepsonse)
                .temperature(temperature)
                .build();
    }
    
    private String generatePrompt(String userMessage, Conversation conversation) {
        if (conversation == null) {
            return prefix + userMessage;
        }
        
        int maxTokensAllowed = tokensTotal - tokensRepsonse - StringUtils.countOccurrencesOf(prefix, " ");        
        String generatedPrompt = "H: " + userMessage;
        
        List<ConversationMessage> messagesToHandle = conversation.getMessages().stream()
                    .sorted(Comparator.comparing(ConversationMessage::getStartDate).reversed())
                    .limit(30L)
                    .collect(Collectors.toList());
        
        int numberOfPrompts = 0;
        for (ConversationMessage conversationMessage : messagesToHandle) {
            String newPrompt = conversationMessage.getGeneratedMessage() + generatedPrompt;
            int newPromptWordCount = StringUtils.countOccurrencesOf(generatedPrompt, " ");
            
            if (newPromptWordCount < maxTokensAllowed) {
                generatedPrompt = newPrompt;
                numberOfPrompts++;
            } else {
                break;
            }
        }
        
        if (log.isDebugEnabled()) {
            log.debug("generatePrompt: generated prompt with [{}] messages without prefix: [{}]", numberOfPrompts, generatedPrompt);
        }
        
        return prefix + generatedPrompt;
    }
    
    private Conversation loadConversation(Optional<User> user) {
        if (user.isEmpty()) {
            log.warn("loadConversation: no user found in request, cannot load conversation");
            return null;
        }
        
        Long userId = user.get().getId().asLong();
        Optional<Conversation> conversation = conversationRepository.findByUserIdAndEndDateIsNull(userId);
        if (conversation.isEmpty()) {
            Conversation conv = new Conversation(userId);
            return conversationRepository.save(conv);
        }
        return conversation.get();
    }
    
    private void addToConversation(String message, ChatMember member, Conversation conversation) {
        if (conversation == null) {
            log.warn("addToConversation: no conversation found to save the request [{}], skipping", message);
            return;
        }
        conversation.addMessage(message, member);
    }

    private void saveConversation(Conversation conversation) {
        if (conversation == null) {
            log.warn("saveConversation: no conversation found to save, skipping");
            return;
        }
        
        // remove older messages
        if (conversation.getMessages().size() > 30) {
            List<ConversationMessage> messagesToKeep = conversation.getMessages().stream()
                    .sorted(Comparator.comparing(ConversationMessage::getStartDate).reversed())
                    .limit(30L)
                    .collect(Collectors.toList());
            conversation.setMessages(messagesToKeep);
        }
        
        conversationRepository.save(conversation);
    }
}
