package at.naurandir.discord.clem.bot.service.client.dto;

import at.naurandir.discord.clem.bot.service.client.dto.worldstate.VoidTraderDTO;
import at.naurandir.discord.clem.bot.service.client.dto.worldstate.CetusCycleDTO;
import at.naurandir.discord.clem.bot.service.client.dto.worldstate.AlertDTO;
import at.naurandir.discord.clem.bot.service.client.dto.worldstate.VallisCycleDTO;
import at.naurandir.discord.clem.bot.service.client.dto.worldstate.VoidFissureDTO;
import at.naurandir.discord.clem.bot.service.client.dto.worldstate.CambionCycleDTO;
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
public class WorldStateDTO {
    
    @SerializedName("alerts")
    private List<AlertDTO> alertsDTO;
    
    @SerializedName("fissures")
    private List<VoidFissureDTO> fissuresDTO;
    
    @SerializedName("cetusCycle")
    private CetusCycleDTO cetusCycleDTO;
    
    @SerializedName("vallisCycle")
    private VallisCycleDTO vallisCycleDTO;
    
    @SerializedName("cambionCycle")
    private CambionCycleDTO cambionCycleDTO;
    
    @SerializedName("voidTrader")
    private VoidTraderDTO voidTraderDTO;
}
