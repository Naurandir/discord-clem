package at.naurandir.discord.clem.bot.model.enums;

import com.google.gson.annotations.SerializedName;
import java.util.Arrays;
import java.util.stream.StreamSupport;

/**
 *
 * @author Naurandir
 */
public enum RelicTier {
    
    @SerializedName("Lith")
    LITH("Lith"),
    
    @SerializedName("Meso")
    MESO("Meso"),
    
    @SerializedName("Axi")
    AXI("Axi"),
    
    @SerializedName("Neo")
    NEO("Neo"),
    
    @SerializedName("Requiem")
    REQUIEM("Requiem");
    
    private String value;
    
    private RelicTier(String value) {
        this.value = value;
    }
    
    public String getTierString() {
        return value;
    }
    
    public static RelicTier parse(String value) {
        return Arrays.stream(values())
                .filter(val -> val.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Value " + value + " not existing!"));
    }
}
