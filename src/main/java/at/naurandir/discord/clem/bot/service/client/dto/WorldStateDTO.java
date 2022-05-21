package at.naurandir.discord.clem.bot.service.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("cetusCycle")
    private CetusCycleDTO cetusCycleDTO;
}
