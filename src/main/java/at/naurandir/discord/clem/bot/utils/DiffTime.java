package at.naurandir.discord.clem.bot.utils;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Naurandir
 */
@Getter
@Setter
public class DiffTime {
    
    public DiffTime(long diffTimeMilliSeconds) {
        
    }
    
    private int minutes;
    private int hours;
    private int days;
    
}
