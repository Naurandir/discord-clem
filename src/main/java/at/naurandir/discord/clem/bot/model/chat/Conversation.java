package at.naurandir.discord.clem.bot.model.chat;

import at.naurandir.discord.clem.bot.model.DbEntity;
import at.naurandir.discord.clem.bot.model.chat.ConversationMessage.ChatMember;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author Naurandir
 */
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "conversation")
public class Conversation extends DbEntity {
    
    @Column
    private Long userId;
    
    @OneToMany(mappedBy="message", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<ConversationMessage> messages;

    public Conversation(Long userId) {
        this.userId = userId;
        messages = new ArrayList<>();
    }
    
    public void addMessage(String message, ChatMember chatMember) {
        ConversationMessage conversationMessage = new ConversationMessage(this, message, chatMember);
        conversationMessage.setStartDate(LocalDateTime.now());
        conversationMessage.setModifyDate(conversationMessage.getStartDate());
        
        messages.add(conversationMessage);
    }
}
