package at.naurandir.discord.clem.bot.model.enums;

import com.google.gson.annotations.SerializedName;

/**
 *
 * @author Naurandir
 */
public enum RelicState {
    
    @SerializedName("Intact")
    INTACT,
    @SerializedName("Exceptional")
    EXCEPTIONAL,
    @SerializedName("Flawless")
    FLAWLESS,
    @SerializedName("Radiant")
    RADIANT;
}
