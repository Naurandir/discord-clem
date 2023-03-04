package at.naurandir.discord.clem.bot.service.client.dto.chat;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author Naurandir
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class ChatGptChatResponseDTO {
    private String id;
    private String model;
    private List<ChatGptChatChoiceDTO> choices;
    private ChatGptErrorResponseDTO error;
}
