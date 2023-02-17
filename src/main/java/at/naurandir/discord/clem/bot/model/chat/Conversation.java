package at.naurandir.discord.clem.bot.model.chat;

import at.naurandir.discord.clem.bot.model.DbEntity;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
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
@Table(name = "conversation")
public class Conversation extends DbEntity {
    
    @OneToMany(mappedBy="message", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    List<ConversationMessage> messages;
}
