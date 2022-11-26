package at.naurandir.discord.clem.bot.service;

import at.naurandir.discord.clem.bot.model.alert.Alert;
import at.naurandir.discord.clem.bot.model.notify.Notification;
import at.naurandir.discord.clem.bot.utils.LocalDateTimeUtil;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 *
 * @author Naurandir
 */
@Slf4j
@Service
public class UpdatePipelineService {
    
    // TODO: make notification an entity, save "activationTime" when it should be earliest notified, let the push service get only the one with activationTime <= now time
    
    private static final String ALERT_TOPIC = "New Alert in Warframe!";
    private static final String ALERT_THUMBNAIL = "https://cutewallpaper.org/21/warframe-desktop-icon/Warframe-Alerts-7.2.1-Download-APK-for-Android-Aptoide.png";
    private static final String ALERT_TEXT =  "*type:* {type}\n"
            + " *enemies:* {enemyType} ({minLevel} - {maxLevel})\n"
            + " *reward:* {reward}\n"
            + " *expires in:* {days}d {hours}h {minutes}m";
    
    private final List<Notification> toNotifyMessages = new ArrayList<>();
    
    public List<Notification> getNotifications() {
        List<Notification> currentNotifications = new ArrayList<>();
        synchronized(toNotifyMessages) {
            currentNotifications.addAll(toNotifyMessages);
        }
        
        return currentNotifications;
    }
    
    public void removeNotifications(List<Notification> notifications) {
        synchronized(toNotifyMessages) {
            toNotifyMessages.removeAll(notifications);
        }
    }
    
    public void addAlertNotify(Alert alert) {
        if (LocalDateTime.now().isAfter(alert.getExpiry())) {
            return; // already finished, nothing to notify
        }
        
        Duration diffTime = LocalDateTimeUtil.getDiffTime(LocalDateTime.now(), alert.getExpiry());
        Integer minLevel = Objects.requireNonNullElse(alert.getAlertMission().getMinLevelEnemy(), 1);
        Integer maxLevel = Objects.requireNonNullElse(alert.getAlertMission().getMaxLevelEnemy(), 999);
        
        Notification notification = new Notification(
                ALERT_TOPIC, 
                ALERT_TEXT.replace("{type}", alert.getAlertMission().getType())
                          .replace("{reward}", alert.getRewards())
                          .replace("{enemyType}", alert.getAlertMission().getFaction())
                          .replace("{minLevel}", String.valueOf(minLevel))
                          .replace("{maxLevel}", String.valueOf(maxLevel))
                          .replace("{days}", String.valueOf(diffTime.toDays()))
                          .replace("{hours}", String.valueOf(diffTime.toHoursPart()))
                          .replace("{minutes}", String.valueOf(diffTime.toMinutesPart())), 
                ALERT_THUMBNAIL);
        
        log.debug("addAlertNotify: generated alert notification [{}]", notification);
        
        addNotification(notification);
    }
    
    private void addNotification(Notification notification) {
        synchronized(toNotifyMessages) {
            toNotifyMessages.add(notification);
        }
    }
}
