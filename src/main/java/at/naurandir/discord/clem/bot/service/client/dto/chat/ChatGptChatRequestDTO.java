package at.naurandir.discord.clem.bot.service.client.dto.chat;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author Naurandir
 */
@Builder
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class ChatGptChatRequestDTO {
    
    private String model;
    
    private Double temperature;
    
    private List<ChatGptChatMessageRequestDTO> messages;
    
    @SerializedName("max_tokens")
    private Integer maxTokens;
}
