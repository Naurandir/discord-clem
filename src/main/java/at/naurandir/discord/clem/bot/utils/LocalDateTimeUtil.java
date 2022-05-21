
package at.naurandir.discord.clem.bot.utils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author Naurandir
 */
public class LocalDateTimeUtil {
    
    private LocalDateTimeUtil() {
        //
    }
    
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    
    public static LocalDateTime getDiffTime(String time1, LocalDateTime time2) {
        return getDiffTime(LocalDateTime.parse(time1, formatter), time2);
    }
    
    public static LocalDateTime getDiffTime(LocalDateTime time1, String time2) {
        return getDiffTime(time1, LocalDateTime.parse(time2, formatter));
    }
    
    public static LocalDateTime getDiffTime(String time1, String time2) {
        return getDiffTime(LocalDateTime.parse(time1, formatter), LocalDateTime.parse(time2, formatter));
    }
    
    public static LocalDateTime getDiffTime(LocalDateTime time1, LocalDateTime time2) {
        Timestamp timestamp1 = Timestamp.valueOf(time1);
        Timestamp timestamp2 = Timestamp.valueOf(time2);
        
        long diffTime = timestamp2.getTime() - timestamp1.getTime();
        
        LocalDateTime diff = (new Timestamp(diffTime)).toLocalDateTime();
        if (diff.getYear() < 0) {
            diff = LocalDateTime.of(0, Month.JANUARY, 0, 0, 0);
        }
        
        return diff;
    }
}
