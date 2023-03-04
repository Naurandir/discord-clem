package at.naurandir.discord.clem.bot.service.client.dto.chat;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author Naurandir
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class ChatGptChatMessageRequestDTO {
    
    private ChatGptRole role;
    
    private String content;
    
    public enum ChatGptRole {
        system, user, assistant;
    }
}
