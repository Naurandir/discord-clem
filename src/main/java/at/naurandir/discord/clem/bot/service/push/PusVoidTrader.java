package at.naurandir.discord.clem.bot.service.push;

import at.naurandir.discord.clem.bot.service.WarframeState;
import at.naurandir.discord.clem.bot.service.client.dto.VoidTraderDTO;
import at.naurandir.discord.clem.bot.service.client.dto.VoidTraderDTO.VoidTraderInventoryDTO;
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
 * @author Naurandir
 */
@Slf4j
@Component
public class PusVoidTrader extends Push {
    
    @Value("#{'${discord.clem.push.channels.void-trader}'.split(',')}")
    private List<String> interestingChannels;
    
    private static final String TITLE = "Void Trader Deals";
    private static final String DESCRIPTION_ACTIVE = "Baro Ki'Teer is currently at ***{location}***. Will leave in {days}d {hours}h {minutes}m.";
    private static final String DESCRIPTION_INACTIVE = "Baro Ki'Teer is currently in the Void. Return in {days}d {hours}h {minutes}m.";
    private static final String ITEM_DESCRIPTION = " *ducats:* {ducats}\n *credits:* {credits}";

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
                messageData.embeds().get(0).title().get().equals(TITLE);
    }

    @Override
    boolean isSticky() {
        return true;
    }
    
    private EmbedCreateSpec generateEmbed(WarframeState warframeState) {
        VoidTraderDTO voidTrader = warframeState.getVoidTraderDTO();
        
        String description;
        if (voidTrader.getActive()) {
            Duration diffTimeExpiry = LocalDateTimeUtil.getDiffTime(LocalDateTime.now(), voidTrader.getExpiry());
            description = DESCRIPTION_ACTIVE.replace("{location}", voidTrader.getLocation())
                    .replace("{days}", String.valueOf(diffTimeExpiry.toDays()))
                    .replace("{hours}", String.valueOf(diffTimeExpiry.toHoursPart()))
                    .replace("{minutes}", String.valueOf(diffTimeExpiry.toMinutesPart()));
        } else {
            Duration diffTimeActiveAgain = LocalDateTimeUtil.getDiffTime(LocalDateTime.now(), voidTrader.getActivation());
            description = DESCRIPTION_INACTIVE.replace("{days}", String.valueOf(diffTimeActiveAgain.toDays()))
                    .replace("{hours}", String.valueOf(diffTimeActiveAgain.toHoursPart()))
                    .replace("{minutes}", String.valueOf(diffTimeActiveAgain.toMinutesPart()));
        }
        Builder embedBuilder = EmbedCreateSpec.builder()
                .color(Color.CYAN)
                .title(TITLE)
                .description(description)
                .thumbnail("https://static.wikia.nocookie.net/warframe/images/a/a7/TennoCon2020BaroCropped.png/revision/latest?cb=20200712232455")
                .timestamp(Instant.now());
        
        for (VoidTraderInventoryDTO inventory : voidTrader.getInventory()) {
            
            embedBuilder.addField(inventory.getItem(), 
                    ITEM_DESCRIPTION.replace("{ducats}", String.valueOf(inventory.getDucats()))
                                    .replace("{credits}", String.valueOf(inventory.getCredits())), 
                    true);
        }
        
        return embedBuilder.build();
    }
}
