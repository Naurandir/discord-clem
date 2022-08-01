package at.naurandir.discord.clem.bot.model.item;

import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Named;

/**
 *
 * @author Naurandir
 */
public interface ItemMapper {

    @Named("getMarketCost")
    default Double getMarketCost(String marketCost) {
        if (StringUtils.isEmpty(marketCost)) {
            return null;
        }
        
        return Double.parseDouble(marketCost);
    }
    
    @Named("getOverframeUrlName")
    default String getOverframeUrlName(String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        
        return name.toLowerCase().replace(" ", "-").replace("-&-", "-");
    }
    
    default String getThumbnailUrl(Item item, String newUrl) {
        if (!StringUtils.isEmpty(item.getWikiaThumbnail()) && StringUtils.isEmpty(newUrl)) {
            return item.getWikiaThumbnail(); // we keep the old name
        }
        
        return newUrl;
    }
}
