package at.naurandir.discord.clem.bot.service.push;

import at.naurandir.discord.clem.bot.service.WarframeState;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.discordjson.json.MessageData;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;

/**
 *
 * @author Naurandir
 */
public class PushUpdatePipeline extends Push {
    
    @Value("#{'${discord.clem.push.channels.update-pipeline}'.split(',')}")
    private List<String> interestingChannels;

    @Override
    void doNewPush(GatewayDiscordClient client, WarframeState warframeState, Snowflake channelId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        
        // void trader
        
        // alerts
    }

    @Override
    void doUpdatePush(GatewayDiscordClient client, WarframeState warframeState, Snowflake channelId, Snowflake messageId) {
        throw new UnsupportedOperationException("This operation does not need update logic!");
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
