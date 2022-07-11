
package at.naurandir.discord.clem.bot.utils;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 *
 * @author Naurandir
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LocalDateTimeUtil {
    
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    
    public static Duration getDiffTime(String time1, LocalDateTime time2) {
        return getDiffTime(LocalDateTime.parse(time1, formatter), time2);
    }
    
    public static Duration getDiffTime(LocalDateTime time1, String time2) {
        return getDiffTime(time1, LocalDateTime.parse(time2, formatter));
    }
    
    public static Duration getDiffTime(String time1, String time2) {
        return getDiffTime(LocalDateTime.parse(time1, formatter), LocalDateTime.parse(time2, formatter));
    }
    
    /**
     * time2 - time1 is calculated, if time2 is before time1 the zero value is returned
     * @param time1
     * @param time2
     * @return 
     */
    public static Duration getDiffTime(LocalDateTime time1, LocalDateTime time2) {
        if (time2.isBefore(time1)) {
            return Duration.ZERO;
        }
        
        Timestamp timestamp1 = Timestamp.valueOf(time1);
        Timestamp timestamp2 = Timestamp.valueOf(time2);
        
        return Duration.of(timestamp2.getTime()-timestamp1.getTime(), ChronoUnit.MILLIS);
    }
    
    public static LocalDateTime getTime(String timeString) {
        return LocalDateTime.parse(timeString, formatter);
    }
}
