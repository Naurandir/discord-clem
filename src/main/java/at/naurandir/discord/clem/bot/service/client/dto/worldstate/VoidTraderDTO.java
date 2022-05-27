package at.naurandir.discord.clem.bot.service.client.dto.worldstate;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author Naurandir
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class VoidTraderDTO {
    
    private String id;
    private String activation;
    private String expiry;
    private String character;
    private String location;
    private Boolean active;
    
    @SerializedName("inventory")
    private List<VoidTraderInventoryDTO> inventory;
    
    
    @Getter
    @Setter
    @ToString
    @EqualsAndHashCode
    public class VoidTraderInventoryDTO {
        
        private String item;
        private Integer ducats;
        private Integer credits;
        
    }
}
