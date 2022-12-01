package at.naurandir.discord.clem.bot.model.news;

import at.naurandir.discord.clem.bot.model.DbEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Naurandir
 */
@Getter
@Setter
@Entity
@Table(name = "news")
public class News extends DbEntity {
    
    @Column(nullable = false)
    private String message;
    
    @Column(nullable = false)
    private String imageLink;
    
    @Column(nullable = false)
    private String link;
}
