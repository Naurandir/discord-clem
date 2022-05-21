package at.naurandir.discord.clem.bot.service;

import at.naurandir.discord.clem.bot.service.client.dto.CetusCycleDTO;
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
    private boolean isCetusCycleChanged;
}
