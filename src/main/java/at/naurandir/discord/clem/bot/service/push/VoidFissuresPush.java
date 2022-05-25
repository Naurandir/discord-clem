package at.naurandir.discord.clem.bot.service.push;

import at.naurandir.discord.clem.bot.model.WarframeState;
import at.naurandir.discord.clem.bot.service.client.dto.VoidFissureDTO;
import at.naurandir.discord.clem.bot.utils.LocalDateTimeUtil;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.EmbedCreateSpec.Builder;
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
 * @author aspiel
 */
@Slf4j
@Component
public class VoidFissuresPush extends Push {
    
    @Value("#{'${discord.clem.push.channels.fissures}'.split(',')}")
    private List<String> interestingChannels;

    private static final String TITLE = "Current Void Fissures";
    private static final String DESCRIPTION = "Currently active void fissures.";
    private static final String FISSURE_DESCRIPTION = " *Tier:* {tier}\n"
            + " {missionType} ({enemyType})\n"
            + " *expire:* {hours}h {minutes}m";

    @Override
    void doNewPush(GatewayDiscordClient client, WarframeState warframeState, Snowflake channelId) {
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
    boolean isOwnMessage(MessageData messageData) {
        return messageData.embeds() != null &&
                messageData.embeds().size() > 0 &&
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
        Builder embedBuilder = EmbedCreateSpec.builder()
                .color(Color.TAHITI_GOLD)
                .title(TITLE)
                .description(DESCRIPTION)
                .thumbnail("https://static.wikia.nocookie.net/warframe/images/0/0e/VoidProjectionsGoldD.png/revision/latest?cb=20160709035734")
                .timestamp(Instant.now());

        for (VoidFissureDTO fissure : warframeState.getFissures()) {

            if (fissure.getExpired() || fissure.getIsStorm()) {
                continue;
            }

            Duration diffTime = LocalDateTimeUtil.getDiffTime(LocalDateTime.now(), fissure.getExpiry());

            embedBuilder.addField(fissure.getNode(), 
                    FISSURE_DESCRIPTION.replace("{tier}", fissure.getTier())
                                     .replace("{enemyType}", fissure.getEnemy())
                                     .replace("{missionType}", fissure.getMissionType())
                                     .replace("{hours}", String.valueOf(diffTime.toHours()))
                                     .replace("{minutes}", String.valueOf(diffTime.toMinutesPart())), 
                    true);
        }

        return embedBuilder.build();
    }
}