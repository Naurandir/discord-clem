package at.naurandir.discord.clem.bot.service;

import at.naurandir.discord.clem.bot.model.chat.Conversation;
import at.naurandir.discord.clem.bot.model.chat.ConversationMessage;
import at.naurandir.discord.clem.bot.model.chat.ConversationMessage.ChatMember;
import at.naurandir.discord.clem.bot.repository.ConversationRepository;
import at.naurandir.discord.clem.bot.service.client.WarframeClient;
import at.naurandir.discord.clem.bot.service.client.dto.chat.ChatGptChatMessageRequestDTO;
import at.naurandir.discord.clem.bot.service.client.dto.chat.ChatGptChatMessageRequestDTO.ChatGptRole;
import at.naurandir.discord.clem.bot.service.client.dto.chat.ChatGptChatRequestDTO;
import at.naurandir.discord.clem.bot.service.client.dto.chat.ChatGptChatResponseDTO;
import at.naurandir.discord.clem.bot.service.client.dto.chat.ChatGptCompletionResponseDTO;
import at.naurandir.discord.clem.bot.service.client.dto.chat.ChatGptCompletionRequestDTO;
import discord4j.core.object.entity.User;
import java.io.IOException;
import java.util.ArrayList;
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
    
    @Value("${discord.clem.chat.type:turbo}")
    private String chatType;
    
    private final List<String> toReplaceAnswerParts = List.of("?", "AI:", "A:", "A", "AI", ":", "\n");
    
    @Autowired
    private ConversationRepository conversationRepository;
    
    private final WarframeClient warframeClient = new WarframeClient();
    
    @Transactional
    public String chat(String userMessage, Optional<User> user, Boolean isOffTopic) {
        try {
            if (chatType.equals("turbo")) {
                return chatTurbo(userMessage, user, isOffTopic);
            } else {
                return chatCompletion(userMessage, user, isOffTopic);
            }
        } catch (IOException ex) {
            log.error("chat: could not receive a valid answer from chat bot: ", ex);
            return "I am sorry, something went wrong with calling the chatbot with your request, maybe try later again or clear your conversation.\n" +
                    "The error was: " + ex.getMessage();
        }
    }
    
    private String chatTurbo(String userMessage, Optional<User> user, Boolean isOffTopic) throws IOException {
        String finalUserMessage = replaceBadRequestCharacters(userMessage);
        Conversation conversation = loadConversation(user);
        addToConversation(finalUserMessage, ChatMember.HUMAN, conversation);
        
        List<ChatGptChatMessageRequestDTO> messages = generateChatMessages(finalUserMessage, conversation, isOffTopic);
        ChatGptChatRequestDTO body = createRequestBody(messages);
        
        ChatGptChatResponseDTO answerDTO = warframeClient.getDataByPost(url, apiHeaders, body, ChatGptChatResponseDTO.class);
        
        if (answerDTO == null || answerDTO.getChoices() == null || answerDTO.getChoices().isEmpty()) {
            log.error("chatTurbo: did not receive a valid answer, answer was: [{}]", answerDTO);
            throw new IOException("It seems that a problem occured and the chatbot did not respond with a valid answer.");
        }
        
        String answer = answerDTO.getChoices().get(0).getMessage().getContent();
        for (String toReplace : toReplaceAnswerParts) {
            if (answer.startsWith(toReplace) || answer.indexOf(toReplace) <= 5) {
                answer = answer.substring(answer.indexOf(toReplace) + 1);
            }
        }
        
        addToConversation(replaceBadRequestCharacters(answer), ChatMember.AI, conversation);
        saveConversation(conversation);
        
        return answer;
    }

    private String chatCompletion(String userMessage, Optional<User> user, Boolean isOffTopic) throws IOException {
        String finalUserMessage = replaceBadRequestCharacters(userMessage);
        Conversation conversation = loadConversation(user);
        addToConversation(finalUserMessage, ChatMember.HUMAN, conversation);
        
        String prompt = generatePrompt(finalUserMessage, conversation, isOffTopic);
        ChatGptCompletionRequestDTO body = createRequestBody(prompt);
        ChatGptCompletionResponseDTO answerDTO = warframeClient.getDataByPost(url, apiHeaders, body, ChatGptCompletionResponseDTO.class);
        
        if (answerDTO == null || answerDTO.getChoices() == null || answerDTO.getChoices().isEmpty()) {
            log.error("chatCompletion: did not receive a valid answer, answer was: [{}]", answerDTO);
            throw new IOException("It seems that a problem occured and the chatbot did not respond with a valid answer.");
        }
        
        String answer = answerDTO.getChoices().get(0).getText();
        for (String toReplace : toReplaceAnswerParts) {
            if (answer.startsWith(toReplace) || answer.indexOf(toReplace) <= 5) {
                answer = answer.substring(answer.indexOf(toReplace) + 1);
            }
        }
        
        addToConversation(replaceBadRequestCharacters(answer), ChatMember.AI, conversation);
        saveConversation(conversation);
        
        return answer;
    }

    private ChatGptCompletionRequestDTO createRequestBody(String message) {
        return ChatGptCompletionRequestDTO.builder()
                .model(model)
                .prompt(message)
                .maxTokens(tokensRepsonse)
                .temperature(temperature)
                .build();
    }
    
    private ChatGptChatRequestDTO createRequestBody(List<ChatGptChatMessageRequestDTO> messages) {
        return ChatGptChatRequestDTO.builder()
                .model(model)
                .messages(messages)
                .maxTokens(tokensRepsonse)
                .temperature(temperature)
                .build();
    }
    
    private String generatePrompt(String userMessage, Conversation conversation, boolean isOffTopic) { 
        if (conversation == null && isOffTopic) {
            return userMessage;
        } else if (conversation == null) {
            return prefix + userMessage;
        }
        
        int maxTokensAllowed = tokensTotal - tokensRepsonse - StringUtils.countOccurrencesOf(prefix, " ");        
        String generatedPrompt = "";
        
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
        
        if (isOffTopic) {
            return generatedPrompt;
        }
        return prefix + generatedPrompt;
    }
    
    private List<ChatGptChatMessageRequestDTO> generateChatMessages(String userMessage, Conversation conversation, boolean isOffTopic) {
        List<ChatGptChatMessageRequestDTO> messages = new ArrayList<>();
        ChatGptChatMessageRequestDTO systemMessage = new ChatGptChatMessageRequestDTO(ChatGptRole.system, prefix);
        
        if (!isOffTopic) {
            messages.add(systemMessage);
        }
        
        if (conversation == null) {
            messages.add(new ChatGptChatMessageRequestDTO(ChatGptRole.user, userMessage));
            return messages;
        }
        
        int maxTokensAllowed = tokensTotal - tokensRepsonse - StringUtils.countOccurrencesOf(prefix, " ");
        int currentTokens = 0;
        
        List<ConversationMessage> messagesToHandle = conversation.getMessages().stream()
                    .sorted(Comparator.comparing(ConversationMessage::getStartDate).reversed())
                    .limit(30L)
                    .collect(Collectors.toList());
        
        for (ConversationMessage conversationMessage : messagesToHandle) {
            int tokenCount = StringUtils.countOccurrencesOf(conversationMessage.getMessage(), " ");
            
            if (currentTokens + tokenCount > maxTokensAllowed) {
                break;
            }
            
            currentTokens = currentTokens + tokenCount;
            
            messages.add(new ChatGptChatMessageRequestDTO(
                    conversationMessage.getChatMember() == ChatMember.HUMAN ? ChatGptRole.user : ChatGptRole.assistant, 
                    conversationMessage.getMessage()));
        }
        
        if (log.isDebugEnabled()) {
            log.debug("generateChatMessages: generated messages with [{}] messages: [{}]", messages.size(), messages);
        }
        
        return messages;
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
    
    private String replaceBadRequestCharacters(String message) {
        return message.replace("ä", "ae").replace("ü", "ue").replace("ö", "oe")
                      .replace("Ä", "Ae").replace("Ü", "Ue").replace("Ö", "Oe")
                      .replace("ß", "ss");
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
}
