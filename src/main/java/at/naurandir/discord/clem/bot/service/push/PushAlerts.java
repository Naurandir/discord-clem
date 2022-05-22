package at.naurandir.discord.clem.bot.service.push;

import at.naurandir.discord.clem.bot.service.WarframeState;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.json.MessageData;
import discord4j.rest.util.Color;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author Naurandir
 */
@Slf4j
@Component
public class PushAlerts extends Push {
    
    @Value("#{'${discord.clem.push.channels.alerts}'.split(',')}")
    private List<String> interestingChannels;
    
    private static final String TITLE = "Current Alerts";
    private static final String DESCRIPTION = "Currently active alerts are presented here.";

    @Override
    void doNewPush(GatewayDiscordClient client, WarframeState warframeState, Snowflake channelId) {
        log.info("doNewPush: adding push message for alerts to channel [{}] ...", channelId);
        EmbedCreateSpec embed = generateEmbed(warframeState);

        client.rest().getChannelById(channelId)
                    .createMessage(embed.asRequest())
                    .subscribe();
        
        log.info("doNewPush: adding push message for alerts to channel [{}] done", channelId);
    }

    @Override
    void doUpdatePush(GatewayDiscordClient client, WarframeState warframeState, Snowflake channelId, Snowflake messageId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    List<String> getInterestingChannels() {
        return interestingChannels;
    }

    @Override
    boolean isOwnMessage(MessageData messageData) {
        return messageData.embeds() != null && 
                !messageData.embeds().get(0).title().isAbsent() &&
                messageData.embeds().get(0).title().get().equals(TITLE) &&
                !messageData.embeds().get(0).description().isAbsent() &&
                messageData.embeds().get(0).description().get().equals(DESCRIPTION);
    }

    @Override
    boolean isSticky() {
        return true;
    }
    
    private EmbedCreateSpec generateEmbed(WarframeState warframeState) {         
        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .color(Color.RED)
                .title(TITLE)
                .author("Clem Bot", "https://naurandir.net", "https://cutewallpaper.org/21/warframe-desktop-icon/Warframe-Alerts-7.2.1-Download-APK-for-Android-Aptoide.png")
                .description(DESCRIPTION)
                .thumbnail("https://cutewallpaper.org/21/warframe-desktop-icon/Warframe-Alerts-7.2.1-Download-APK-for-Android-Aptoide.png")
                .addField("alert a", "reward a", true)
                .addField("alert b", "reward b", true)
                .addField("alert c", "reward c", true)
                //.image("https://i.imgur.com/F9BhEoz.png")
                .timestamp(Instant.now())
                .footer("footer", "https://cutewallpaper.org/21/warframe-desktop-icon/Warframe-Alerts-7.2.1-Download-APK-for-Android-Aptoide.png")
                .build();
        return embed;
    }
}
