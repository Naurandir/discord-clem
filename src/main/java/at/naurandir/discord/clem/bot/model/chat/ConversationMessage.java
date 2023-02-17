package at.naurandir.discord.clem.bot.model.chat;

import at.naurandir.discord.clem.bot.model.DbEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author Naurandir
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "conversation_message")
public class ConversationMessage extends DbEntity {
    
    @ManyToOne
    @JoinColumn(name="conversation_id")
    private Conversation conversation;
    
    @Column(length = 250, nullable = false)
    private String message;
    
    @Enumerated(EnumType.STRING)
    private ChatMember chatMember;
    
    public String getGeneratedMessage() {
        return chatMember == ChatMember.HUMAN ? "H: " + message : "AI: " + message;
    }
    
    public enum ChatMember {
        HUMAN, AI;
    }
}
