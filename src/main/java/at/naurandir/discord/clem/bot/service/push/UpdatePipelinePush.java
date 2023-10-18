package at.naurandir.discord.clem.bot.service.push;

import at.naurandir.discord.clem.bot.model.enums.PushType;
import at.naurandir.discord.clem.bot.model.notify.Notification;
import at.naurandir.discord.clem.bot.service.UpdatePipelineService;
import discord4j.common.util.Snowflake;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.json.MessageData;
import discord4j.rest.entity.RestMessage;
import discord4j.rest.util.Color;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 *
 * @author Naurandir
 */
@Slf4j
@Component
public class UpdatePipelinePush extends Push {
    
    @Autowired
    private UpdatePipelineService updatePipelineService;
    
    private List<Notification> notifications;

    @Override
    void doNewPush(Snowflake channelId) {
        
        EmbedCreateSpec.Builder embedBuilder = generateEmbedBuilder();
        
        for (Notification notification : notifications) {
            try {
                EmbedCreateSpec embed = embedBuilder
                        .title(notification.getTitle())
                        .description(notification.getText())
                        .thumbnail(notification.getThumbnail())
                        .build();
                
                getClient().rest().getChannelById(channelId)
                    .createMessage(embed.asRequest())
                    .timeout(Duration.ofSeconds(60)).subscribe();
            } catch (Exception ex) {
                log.warn("doNewPush: error for notification [{}]: ", notification.getUuid(), ex);
            }
        }
    }
    
    @Override
    void doUpdatePush(RestMessage message) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    boolean isOwnMessage(MessageData messageData) {
        return false;
    }

    @Override
    boolean isSticky() {
        return false;
    }
    
    @Override
    PushType getPushType() {
        return PushType.UPDATE_PIPELINE;
    }
    
    public EmbedCreateSpec.Builder generateEmbedBuilder() {
        return EmbedCreateSpec.builder()
                .color(Color.DARK_GRAY)
                .timestamp(Instant.now());
    }

    @Scheduled(initialDelay = 60 * 1_000, fixedRate = 60 * 1_000)
    @Override
    void refresh() {
        notifications = updatePipelineService.getNotifications();
        updatePipelineService.removeNotifications(notifications);
        push();
    }
}
