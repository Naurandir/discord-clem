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
public class WorldStateDTO {
    
    @SerializedName("alerts")
    private List<AlertDTO> alertsDTO;
    
    @SerializedName("cetusCycle")
    private CetusCycleDTO cetusCycleDTO;
    
    @SerializedName("vallisCycle")
    private VallisCycleDTO vallisCycleDTO;
    
    @SerializedName("voidTrader")
    private VoidTraderDTO voidTraderDTO;
}
