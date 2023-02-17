package at.naurandir.discord.clem.bot.service.client.dto.chat;

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
public class ChatGptChoiceDTO {
    private String text;
}
