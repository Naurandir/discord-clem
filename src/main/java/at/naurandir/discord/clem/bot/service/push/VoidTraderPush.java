package at.naurandir.discord.clem.bot.service.push;

import at.naurandir.discord.clem.bot.model.trader.VoidTrader;
import at.naurandir.discord.clem.bot.model.trader.VoidTraderItem;
import at.naurandir.discord.clem.bot.service.VoidTraderService;
import at.naurandir.discord.clem.bot.utils.LocalDateTimeUtil;
import discord4j.common.util.Snowflake;
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
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 *
 * @author Naurandir
 */
@Slf4j
@Component
public class VoidTraderPush extends Push {
    
    @Autowired
    VoidTraderService voidTraderService;
    
    @Value("#{'${discord.clem.push.channels.void-trader}'.split(',')}")
    private List<String> interestingChannels;
    
    private static final String TITLE = "Void Trader Deals";
    private static final String DESCRIPTION_ACTIVE = "Baro Ki'Teer is currently at ***{location}***. Will leave in {days}d {hours}h {minutes}m.";
    private static final String DESCRIPTION_INACTIVE = "Baro Ki'Teer is currently in the Void. Return in {days}d {hours}h {minutes}m.";
    private static final String DESCRIPTION_NOT_EXISTING = "Baro Ki'Teer is currently in the Void.";
    private static final String ITEM_DESCRIPTION = " *ducats:* {ducats}\n *credits:* {credits}";

    @Override
    MessageData doNewPush(Snowflake channelId) {
        EmbedCreateSpec embed = generateEmbed(voidTraderService.getVoidTrader());

        return getClient().rest().getChannelById(channelId)
                    .createMessage(embed.asRequest())
                    .block(Duration.ofSeconds(10L));
    }

    @Override
    void doUpdatePush(RestMessage message) {
        MessageEditRequest editRequest = MessageEditRequest.builder()
                .embedOrNull(generateEmbed(voidTraderService.getVoidTrader()).asRequest())
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
    
    private EmbedCreateSpec generateEmbed(Optional<VoidTrader> voidTraderOpt) {
        
        if (voidTraderOpt.isEmpty()) {
            return EmbedCreateSpec.builder()
                .color(Color.CYAN)
                .title(TITLE)
                .description(DESCRIPTION_NOT_EXISTING)
                .thumbnail("https://static.wikia.nocookie.net/warframe/images/a/a7/TennoCon2020BaroCropped.png/revision/latest?cb=20200712232455")
                .timestamp(Instant.now())
                .build();
        }

        VoidTrader voidTrader = voidTraderOpt.get();
        LocalDateTime now = LocalDateTime.now();
        String description;
        
        if (now.isAfter(voidTrader.getActivation()) && now.isBefore(voidTrader.getExpiry())) {
            Duration diffTimeExpiry = LocalDateTimeUtil.getDiffTime(now, voidTrader.getExpiry());
            description = DESCRIPTION_ACTIVE.replace("{location}", voidTrader.getLocation())
                    .replace("{days}", String.valueOf(diffTimeExpiry.toDays()))
                    .replace("{hours}", String.valueOf(diffTimeExpiry.toHoursPart()))
                    .replace("{minutes}", String.valueOf(diffTimeExpiry.toMinutesPart()));
        } else {
            Duration diffTimeActiveAgain = LocalDateTimeUtil.getDiffTime(now, voidTrader.getActivation());
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
        
        log.debug("generateEmbed: got [{}] items from Void Trader", voidTrader.getInventory().size());
        for (VoidTraderItem item : voidTrader.getInventory()) {
            embedBuilder.addField(item.getItem(), 
                    ITEM_DESCRIPTION.replace("{ducats}", String.valueOf(item.getDucats()))
                                    .replace("{credits}", String.valueOf(item.getCredits())), 
                    true);
        }
        
        return embedBuilder.build();
    }
    
    @Scheduled(initialDelay = 60 * 1_000, fixedRate = 60 * 1_000)
    @Override
    void refresh() {
        push();
    }
}
