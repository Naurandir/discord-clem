package at.naurandir.discord.clem.bot.service.client.dto;

import at.naurandir.discord.clem.bot.service.client.dto.droptable.MissionDropDTO;
import at.naurandir.discord.clem.bot.service.client.dto.droptable.RelicDropDTO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author Naurandir
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DropTableDTO {
    
    private List<RelicDropDTO> relics;  
    private List<MissionDropDTO> missions;
    
}
