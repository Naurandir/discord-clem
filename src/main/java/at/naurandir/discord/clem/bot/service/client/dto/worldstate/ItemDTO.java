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
public class ItemDTO {

    private String name;
    private String uniqueName;
    private Boolean tradable;
    private String marketCost;
    private String description;
    private String category;
    
//    private Long buildPrice;
//    private Integer buildQuantity;
//    private Long buildTime;
//    private List<ItemComponentDTO> components;
//    
//    @SerializedName("bpCost")
//    private Long bluePrintCost;
//    
//    // enemy specific
//    private String type;
//    private List<ItemDropDTO> drops;
    
}
