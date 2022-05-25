package at.naurandir.discord.clem.bot.service.push;

import at.naurandir.discord.clem.bot.model.WarframeState;
import at.naurandir.discord.clem.bot.utils.LocalDateTimeUtil;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.json.MessageData;
import discord4j.discordjson.json.MessageEditRequest;
import discord4j.rest.entity.RestMessage;
import discord4j.rest.util.Color;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
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
public class PushWorldCycle extends Push {
    
    @Value("#{'${discord.clem.push.channels.worldcycle}'.split(',')}")
    private List<String> interestingChannels;
    
    private static final String TITLE = "Current World Cycles";
    private static final String STATE_MESSAGE = "{state} for {hours}h {minutes}m.";

    @Override
    public void doNewPush(GatewayDiscordClient client, WarframeState warframeState, Snowflake channelId) {
        EmbedCreateSpec embed = generateEmbed(warframeState);

        client.rest().getChannelById(channelId)
                    .createMessage(embed.asRequest())
                    .subscribe();
    }
    
    @Override
    void doUpdatePush(RestMessage message, WarframeState warframeState) {
        MessageEditRequest editRequest = MessageEditRequest.builder()
                .embedOrNull(generateEmbed(warframeState).asRequest())
                .build();
        message.edit(editRequest).subscribe();
    }
    
    @Override
    List<String> getInterestingChannels() {
        return interestingChannels;
    }
    
    @Override
    public boolean isOwnMessage(MessageData messageData) {
        return messageData.embeds() != null &&
                messageData.embeds().size() > 0 &&
                !messageData.embeds().get(0).title().isAbsent() &&
                messageData.embeds().get(0).title().get().equals(TITLE);
    }

    @Override
    public boolean isSticky() {
        return true;
    }
    
    private EmbedCreateSpec generateEmbed(WarframeState warframeState) {         
        EmbedCreateSpec.Builder embedBuilder = EmbedCreateSpec.builder()
                .color(Color.DEEP_SEA)
                .title(TITLE)
                .thumbnail("https://content.invisioncic.com/Mwarframe/pages_media/1_CherryTreeGlyph.png")
                .timestamp(Instant.now());
        
        LocalDateTime now = LocalDateTime.now();
        
        // earth
        
        // cetus
        Duration diffTime = LocalDateTimeUtil.getDiffTime(now, warframeState.getCetusCycle().getExpiry());
        embedBuilder.addField("Cetus",
                STATE_MESSAGE.replace("{state}", warframeState.getCetusCycle().getState())
                             .replace("{hours}", String.valueOf(diffTime.toHours()))
                             .replace("{minutes}", String.valueOf(diffTime.toMinutesPart())),
                true);
        
        // vallis
        diffTime = LocalDateTimeUtil.getDiffTime(now, warframeState.getVallisCycle().getExpiry());
        embedBuilder.addField("Vallis",
                STATE_MESSAGE.replace("{state}", warframeState.getVallisCycle().getState())
                             .replace("{hours}", String.valueOf(diffTime.toHours()))
                             .replace("{minutes}", String.valueOf(diffTime.toMinutesPart())),
                true);
        
        // cambion
        diffTime = LocalDateTimeUtil.getDiffTime(now, warframeState.getCambionCycle().getExpiry());
        embedBuilder.addField("Cambion",
                STATE_MESSAGE.replace("{state}", warframeState.getCambionCycle().getActive())
                             .replace("{hours}", String.valueOf(diffTime.toHours()))
                             .replace("{minutes}", String.valueOf(diffTime.toMinutesPart())),
                true);
        
        return embedBuilder.build();
    }
}
