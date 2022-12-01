package at.naurandir.discord.clem.bot.service;

import at.naurandir.discord.clem.bot.model.alert.Alert;
import at.naurandir.discord.clem.bot.model.news.News;
import at.naurandir.discord.clem.bot.model.notify.Notification;
import java.sql.Timestamp;
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
            + " *expires in:* <t:{timestamp}:R>";
    
    private static final String NEWS_TEXT = "*News*\n"
            + "{message}\n"
            + "Source: {link}";
    
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
        
        Integer minLevel = Objects.requireNonNullElse(alert.getAlertMission().getMinLevelEnemy(), 1);
        Integer maxLevel = Objects.requireNonNullElse(alert.getAlertMission().getMaxLevelEnemy(), 999);
        
        Notification notification = new Notification(
                ALERT_TOPIC, 
                ALERT_TEXT.replace("{type}", alert.getAlertMission().getType())
                          .replace("{reward}", alert.getRewards())
                          .replace("{enemyType}", alert.getAlertMission().getFaction())
                          .replace("{minLevel}", String.valueOf(minLevel))
                          .replace("{maxLevel}", String.valueOf(maxLevel))
                          .replace("{timestamp}", String.valueOf(Timestamp.valueOf(alert.getExpiry()))), 
                ALERT_THUMBNAIL);
        
        log.debug("addAlertNotify: generated alert notification [{}]", notification);
        
        addNotification(notification);
    }
    
    void addNewsNotify(News news) {
        Notification notification = new Notification(
                news.getMessage(), 
                NEWS_TEXT.replace("{message}", news.getMessage())
                         .replace("{link}", news.getLink()), 
                news.getImageLink());
        
        log.debug("addNewsNotify: generated news notification [{}]", notification);
        
        addNotification(notification);
    }
    
    private void addNotification(Notification notification) {
        synchronized(toNotifyMessages) {
            toNotifyMessages.add(notification);
        }
    }
}
