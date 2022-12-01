package at.naurandir.discord.clem.bot.service.client.dto.worldstate;

import lombok.Data;

/**
 *
 * @author Naurandir
 */
@Data
public class NewsDTO {
    
    private String id;
    private String message;
    private String link;
    private String imageLink;
    private String date;
}
