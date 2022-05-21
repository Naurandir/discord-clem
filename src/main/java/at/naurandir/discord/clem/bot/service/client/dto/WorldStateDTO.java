package at.naurandir.discord.clem.bot.service.client.dto;

import com.google.gson.annotations.SerializedName;
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
    
    @SerializedName("cetusCycle")
    private CetusCycleDTO cetusCycleDTO;
}
