package at.naurandir.discord.clem.bot.service.push;

import at.naurandir.discord.clem.bot.service.WarframeState;
import at.naurandir.discord.clem.bot.service.client.dto.AlertDTO;
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
public class PushUpdatePipeline extends Push {
    
    @Value("#{'${discord.clem.push.channels.update}'.split(',')}")
    private List<String> interestingChannels;
    
    private static final String ALERTS_EXISTING = "***Alerts:*** Currently active Alerts existing:\n{alerts}";
    private static final String ALERTS_GONE = "***Alerts:*** No more active Alerts existing.";
    
    private static final String VOID_TRADER_HERE = "***Void Trader:*** Baro Ki'Teer arrived at *{location}*, he will leave in {days}d {hours}h {minutes}m.";
    private static final String VOID_TRADER_GONE = "***Void Trader:*** Baro Ki'Teer went back into the void, he will return in {days}d {hours}h {minutes}m.";

    @Override
    void doNewPush(GatewayDiscordClient client, WarframeState warframeState, Snowflake channelId) {
        
        voidTraderChangeNotify(warframeState, client, channelId);
        alertsChangeNotify(warframeState, client, channelId);
    }
    
    private void voidTraderChangeNotify(WarframeState warframeState, GatewayDiscordClient client, Snowflake channelId) {
        if (warframeState.isVoidTraderStateChanged() && warframeState.getVoidTraderDTO().getActive()) {
            Duration diffTimeExpiry = LocalDateTimeUtil.getDiffTime(LocalDateTime.now(), warframeState.getVoidTraderDTO().getExpiry());
            String message = VOID_TRADER_HERE.replace("{location}", warframeState.getVoidTraderDTO().getLocation())
                    .replace("{days}", String.valueOf(diffTimeExpiry.toDays()))
                    .replace("{hours}", String.valueOf(diffTimeExpiry.toHoursPart()))
                    .replace("{minutes}", String.valueOf(diffTimeExpiry.toMinutesPart()));
            
            client.rest().getChannelById(channelId)
                    .createMessage(message)
                    .subscribe();
        } else if (warframeState.isVoidTraderStateChanged()) {
            Duration diffTimeActivation = LocalDateTimeUtil.getDiffTime(LocalDateTime.now(), warframeState.getVoidTraderDTO().getActivation());
            String message = VOID_TRADER_GONE.replace("{days}", String.valueOf(diffTimeActivation.toDays()))
                    .replace("{hours}", String.valueOf(diffTimeActivation.toHoursPart()))
                    .replace("{minutes}", String.valueOf(diffTimeActivation.toMinutesPart()));
            
            client.rest().getChannelById(channelId)
                    .createMessage(message)
                    .subscribe();
        }
    }

    private void alertsChangeNotify(WarframeState warframeState, GatewayDiscordClient client, Snowflake channelId) {
        if (warframeState.isAlertsStateChanged() && warframeState.getAlerts() != null && warframeState.getAlerts().size() > 0) {
            String message = ALERTS_EXISTING;
            String alerts = "";
            
            for (AlertDTO alert : warframeState.getAlerts()) {
                if (!alert.getActive()) {
                    continue;
                }
                message += "*" + alert.getMission() + "* - " + StringUtils.join(", ", alert.getRewardTypes()) +"\n";
            }
            
            // we only send a message if we got minimum 1 active alert to be shown.
            if (!alerts.equals("")) {
                message = message.replace("{alerts}", alerts);
                client.rest().getChannelById(channelId)
                        .createMessage(message)
                        .subscribe();
            }
        } else if (warframeState.isAlertsStateChanged()) {
            client.rest().getChannelById(channelId)
                    .createMessage(ALERTS_GONE)
                    .subscribe();
        }
    }
    
    @Override
    void doUpdatePush(RestMessage message, WarframeState warframeState) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
