package at.naurandir.discord.clem.bot.service;

import at.naurandir.discord.clem.bot.service.client.dto.AlertDTO;
import at.naurandir.discord.clem.bot.service.client.dto.CetusCycleDTO;
import at.naurandir.discord.clem.bot.service.client.dto.VallisCycleDTO;
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
    
    private List<AlertDTO> alerts;
    
    private CetusCycleDTO cetusCycle;
    private VallisCycleDTO vallisCycle;
}
