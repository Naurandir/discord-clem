package at.naurandir.discord.clem.bot.service.client.dto.chat;

import com.google.gson.annotations.SerializedName;
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
public class ChatGptCompletionRequestDTO {
    
    private String model;
    
    private String prompt;
    
    private Double temperature;
    
    @SerializedName("max_tokens")
    private Integer maxTokens;
}
