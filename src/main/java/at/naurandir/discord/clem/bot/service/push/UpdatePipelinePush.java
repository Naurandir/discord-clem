package at.naurandir.discord.clem.bot.service.push;

import at.naurandir.discord.clem.bot.model.WarframeState;
import at.naurandir.discord.clem.bot.service.client.dto.worldstate.AlertDTO;
import at.naurandir.discord.clem.bot.service.client.dto.worldstate.EventDTO;
import at.naurandir.discord.clem.bot.utils.LocalDateTimeUtil;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.discordjson.json.MessageData;
import discord4j.rest.entity.RestMessage;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import nonapi.io.github.classgraph.utils.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author Naurandir
 */
@Slf4j
@Component
public class UpdatePipelinePush extends Push {
    
    @Value("#{'${discord.clem.push.channels.update}'.split(',')}")
    private List<String> interestingChannels;
    
    private static final String EVENT_EXISTING = "***New Event:***\n{event}";
    private static final String EVENTS_GONE = "***Events:***\nNo more active Events existing.";
    
    private static final String ALERT_EXISTING = "***New Alert:***\n{alert}";
    private static final String ALERTS_GONE = "***Alerts:***\nNo more active Alerts existing.";
    
    private static final String VOID_TRADER_HERE = "***Void Trader:***\nBaro Ki'Teer arrived at *{location}*, he will leave in {days}d {hours}h {minutes}m.";
    private static final String VOID_TRADER_GONE = "***Void Trader:***\nBaro Ki'Teer went back into the void.";

    @Override
    MessageData doNewPush(GatewayDiscordClient client, WarframeState warframeState, Snowflake channelId) {
        voidTraderChangeNotify(warframeState, client, channelId);
        alertsChangeNotify(warframeState, client, channelId);
        eventsChangeNotify(warframeState, client, channelId);
        return null;
    }
    
    private void voidTraderChangeNotify(WarframeState warframeState, GatewayDiscordClient client, Snowflake channelId) {
        if (warframeState.isVoidTraderStateChanged() && warframeState.getVoidTrader().getActive()) {
            Duration diffTimeExpiry = LocalDateTimeUtil.getDiffTime(LocalDateTime.now(), warframeState.getVoidTrader().getExpiry());
            String message = VOID_TRADER_HERE.replace("{location}", warframeState.getVoidTrader().getLocation())
                    .replace("{days}", String.valueOf(diffTimeExpiry.toDays()))
                    .replace("{hours}", String.valueOf(diffTimeExpiry.toHoursPart()))
                    .replace("{minutes}", String.valueOf(diffTimeExpiry.toMinutesPart()));
            
            client.rest().getChannelById(channelId)
                    .createMessage(message)
                    .subscribe();
        } else if (warframeState.isVoidTraderStateChanged()) {
            client.rest().getChannelById(channelId)
                    .createMessage(VOID_TRADER_GONE)
                    .subscribe();
        }
    }

    private void alertsChangeNotify(WarframeState warframeState, GatewayDiscordClient client, Snowflake channelId) {
        if (warframeState.getAlerts().isEmpty() && !warframeState.getOldAlertIds().isEmpty()) {
            client.rest().getChannelById(channelId)
                    .createMessage(ALERTS_GONE)
                    .subscribe();
        }
        
        for (AlertDTO alert : warframeState.getAlerts()) {
            if (!alert.getActive()) {
                continue;
            }
            
            if (warframeState.getOldAlertIds().contains(alert.getId())) {
                continue;
            }
            
            String alertMessage = "*" + alert.getMission() + "* - " + StringUtils.join(", ", alert.getRewardTypes()) +"\n";
            
            client.rest().getChannelById(channelId)
                        .createMessage(ALERT_EXISTING.replace("{alert}", alertMessage))
                        .subscribe();
            
        }
    }
    
    private void eventsChangeNotify(WarframeState warframeState, GatewayDiscordClient client, Snowflake channelId) {
        if (warframeState.getEvents().isEmpty() && !warframeState.getOldEventIds().isEmpty()) {
            client.rest().getChannelById(channelId)
                    .createMessage(EVENTS_GONE)
                    .subscribe();
        }
        
        for (EventDTO event : warframeState.getEvents()) {
            if (!event.getActive()) {
                continue;
            }
            
            if (warframeState.getOldEventIds().contains(event.getId())) {
                continue;
            }
            
            String eventMessage = "*" + event.getDescription() + "* - " + event.getTooltip() + "\n";
            
            client.rest().getChannelById(channelId)
                        .createMessage(EVENT_EXISTING.replace("{event}", eventMessage))
                        .subscribe();
            
        }
    }
    
    @Override
    void doUpdatePush(RestMessage message, WarframeState warframeState) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    List<String> getInterestingChannels() {
        return interestingChannels;
    }

    @Override
    boolean isOwnMessage(MessageData messageData) {
        return false;
    }

    @Override
    boolean isSticky() {
        return false;
    }
}
