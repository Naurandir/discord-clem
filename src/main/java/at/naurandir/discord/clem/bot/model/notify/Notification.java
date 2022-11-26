package at.naurandir.discord.clem.bot.model.notify;

import java.util.UUID;
import lombok.Getter;
import lombok.ToString;

/**
 *
 * @author Naurandir
 */
@ToString
@Getter
public class Notification {
    
    private final String uuid;
    private final String title;
    private final String text;
    private final String thumbnail;
    
    public Notification(String title, String text, String thumbnail) {
        this.uuid = UUID.randomUUID().toString();
        
        this.title = title;
        this.text = text;
        this.thumbnail = thumbnail;
    }
}
