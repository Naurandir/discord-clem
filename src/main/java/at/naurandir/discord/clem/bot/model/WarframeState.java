package at.naurandir.discord.clem.bot.model;

import at.naurandir.discord.clem.bot.service.client.dto.AlertDTO;
import at.naurandir.discord.clem.bot.service.client.dto.CambionCycleDTO;
import at.naurandir.discord.clem.bot.service.client.dto.CetusCycleDTO;
import at.naurandir.discord.clem.bot.service.client.dto.VallisCycleDTO;
import at.naurandir.discord.clem.bot.service.client.dto.VoidTraderDTO;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Naurandir
 */
@Getter
@Setter
public class WarframeState {
    
    private CetusCycleDTO cetusCycle;
    private VallisCycleDTO vallisCycle;
    private CambionCycleDTO cambionCycle;
    
    private VoidTraderDTO voidTraderDTO;
    private boolean isVoidTraderStateChanged;
    
    private List<AlertDTO> alerts;
    private boolean isAlertsStateChanged;
}