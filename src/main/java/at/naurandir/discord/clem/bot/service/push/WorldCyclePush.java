package at.naurandir.discord.clem.bot.service.push;

import at.naurandir.discord.clem.bot.model.cycle.Cycle;
import at.naurandir.discord.clem.bot.model.cycle.CycleType;
import at.naurandir.discord.clem.bot.model.enums.PushType;
import at.naurandir.discord.clem.bot.service.WorldCycleService;
import at.naurandir.discord.clem.bot.utils.LocalDateTimeUtil;
import discord4j.common.util.Snowflake;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.json.MessageData;
import discord4j.discordjson.json.MessageEditRequest;
import discord4j.rest.entity.RestMessage;
import discord4j.rest.util.Color;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class WorldCyclePush extends Push {
    
    @Autowired
    private WorldCycleService worldCycleService;
    
    private static final String TITLE = "Current World Cycles";
    private static final String STATE_MESSAGE = "{state} for {hours}h {minutes}m.";
    
    private static final Map<CycleType, String> CYCLE_NAME = new HashMap<>();
    static {
        CYCLE_NAME.put(CycleType.EARTH, "Earth");
        CYCLE_NAME.put(CycleType.CETUS, "Cetus");
        CYCLE_NAME.put(CycleType.VALLIS, "Orb Vallis");
        CYCLE_NAME.put(CycleType.CAMBION, "Cambion Drift");
    }

    @Override
    void doNewPush(Snowflake channelId) {
        EmbedCreateSpec embed = generateEmbed(worldCycleService.getCycles());

        getClient().rest().getChannelById(channelId)
                .createMessage(embed.asRequest())
                .timeout(Duration.ofSeconds(60)).subscribe();
    }
    
    @Override
    void doUpdatePush(RestMessage message) {
        MessageEditRequest editRequest = MessageEditRequest.builder()
                .embedOrNull(generateEmbed(worldCycleService.getCycles()).asRequest())
                .build();
        message.edit(editRequest).timeout(Duration.ofSeconds(60)).subscribe();
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
    
    @Override
    PushType getPushType() {
        return PushType.WORLD_CYCLE_PUSH;
    }
    
    private EmbedCreateSpec generateEmbed(List<Cycle> cycles) {    
        EmbedCreateSpec.Builder embedBuilder = EmbedCreateSpec.builder()
                .color(Color.DEEP_SEA)
                .title(TITLE)
                .thumbnail("https://content.invisioncic.com/Mwarframe/pages_media/1_CherryTreeGlyph.png")
                .timestamp(Instant.now());
        
        LocalDateTime now = LocalDateTime.now();
        
        for (Cycle cycle : cycles) {
            Duration diffTime = LocalDateTimeUtil.getDiffTime(now, cycle.getExpiry());
            embedBuilder.addField(CYCLE_NAME.get(cycle.getCycleType()),
                STATE_MESSAGE.replace("{state}", cycle.getCurrentState())
                             .replace("{hours}", String.valueOf(diffTime.toHours()))
                             .replace("{minutes}", String.valueOf(diffTime.toMinutesPart())),
                true);
        }
        
        return embedBuilder.build();
    }

    @Scheduled(initialDelay = 60 * 1_000, fixedRate = 30 * 1_000)
    @Override
    void refresh() {
        push();
    }
}
