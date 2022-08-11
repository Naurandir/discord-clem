package at.naurandir.discord.clem.bot.model.channel;

import at.naurandir.discord.clem.bot.model.DbEntity;
import at.naurandir.discord.clem.bot.model.enums.PushType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Naurandir
 */
@Data
@Getter
@Setter
@Entity
@Table(name = "interesting_channel")
public class InterestingChannel extends DbEntity {
    
    @Column
    private Long channelId;
    
    @Column
    private Long stickyMessageId;
    
    @Column
    private String creator;
    
    @Enumerated(EnumType.STRING)
    private PushType pushType;
}
