package at.naurandir.discord.clem.bot.model;

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
@Table(name = "interesting_channel")
public class InterestingChannel extends DbEntity {
    
    @Column
    private Long channelId;
}
