package at.naurandir.discord.clem.bot.model.enums;

import com.google.gson.annotations.SerializedName;

/**
 *
 * @author Naurandir
 */
public enum OrderType {
    
    @SerializedName("sell")
    SELL,
    @SerializedName("buy")
    BUY;
}
