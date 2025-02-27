package at.naurandir.discord.clem.bot.model.item;

import at.naurandir.discord.clem.bot.model.DbEntity;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Naurandir
 */
@Getter
@Setter
@MappedSuperclass
public abstract class Item extends DbEntity {
    
    @Column
    private String overframeUrlName;
    
    @Column
    private String uniqueName;
    
    @Column
    private String wikiaUrl;
    
    @Column
    private String wikiaThumbnail;
    
    public abstract Set<ItemBuild> getBuilds();
}
