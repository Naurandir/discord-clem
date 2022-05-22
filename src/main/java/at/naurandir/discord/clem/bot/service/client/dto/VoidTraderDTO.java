package at.naurandir.discord.clem.bot.service.client.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;
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
    public class VoidTraderInventoryDTO {
        
        private String item;
        private Integer ducats;
        private Integer credits;
        
    }
}
