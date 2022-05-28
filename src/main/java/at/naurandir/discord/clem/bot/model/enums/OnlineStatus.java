package at.naurandir.discord.clem.bot.model.enums;

import com.google.gson.annotations.SerializedName;

/**
 *
 * @author Naurandir
 */
public enum OnlineStatus {
    
    @SerializedName("online")
    ONLINE,
    @SerializedName("offline")
    OFFLINE;
}
