package at.naurandir.discord.clem.bot.model.enums;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;

/**
 *
 * @author Naurandir
 */
@AllArgsConstructor
public enum Rarity {
    
    @SerializedName("Common")
    COMMON,
    @SerializedName("Uncommon")
    UNCOMMON,
    @SerializedName("Rare")
    RARE,
    @SerializedName("Legendary")
    LEGENDARY;
}
