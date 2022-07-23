package at.naurandir.discord.clem.bot.service.client.dto;

import at.naurandir.discord.clem.bot.service.client.dto.droptable.MissionDropTableDTO;
import at.naurandir.discord.clem.bot.service.client.dto.droptable.RelicDTO;
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
public class DropTableDTO {
    
    private List<RelicDTO> relics;
    private List<MissionDropTableDTO> missions;
}
