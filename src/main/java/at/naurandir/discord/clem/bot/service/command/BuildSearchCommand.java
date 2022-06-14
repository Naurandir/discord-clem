package at.naurandir.discord.clem.bot.service.command;

import at.naurandir.discord.clem.bot.model.WarframeState;
import at.naurandir.discord.clem.bot.service.client.dto.overframe.OverframeDTO;
import at.naurandir.discord.clem.bot.service.client.dto.overframe.OverframeItemDTO;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 *
 * @author Naurandir
 */
@Slf4j
@Component
public class BuildSearchCommand implements Command {
    
    private static final String DESCRIPTION = "List of found Overframe Builds";
    private static final String EMBED_TITLE = "***{title}*** ({votes})";
    private static final String EMBED_DESCRIPTION = "{url}\n {forma} - {guide}";

    @Override
    public String getCommandWord() {
        return "build-search";
    }

    @Override
    public String getDescription() {
        return "Shows current top build for given warframe / weapon from overframe.\n"
                + "Note it shows only max 5 different warframe / items with similar name and best 5 builds of them. \n"
                + "Usage: *<bot-prefix> build-search <item>*.";
    }

    @Override
    public Mono<Void> handle(MessageCreateEvent event, WarframeState warframeState) {
        String item = getItem(event.getMessage().getContent());
        if (item.length() < 3) {
            return event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage("The given input [" + item + "] is too short. Please provide a longer name to search"))
                .then();
        }
        
        List<OverframeItemDTO> foundItems = getBuildItems(item, warframeState.getOverframeBuilds());
        
        EmbedCreateSpec[] embeddedMessages = getEmbeddedResult(foundItems);
        return event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage(embeddedMessages))
                .then();
    }
    
    private String getItem(String content) {
        String[] splitted = content.split(" "); // 'prefix command <item>'
        String[] words = Arrays.copyOfRange(splitted, 2, splitted.length);
        return StringUtils.join(words, " ").trim();
    }

    private List<OverframeItemDTO> getBuildItems(String item, OverframeDTO overframeBuilds) {
        List<OverframeItemDTO> items = new ArrayList<>();
        
        items.addAll(overframeBuilds.getWarframes().stream()
                .filter(foundItem -> foundItem.getName().contains(item))
                .collect(Collectors.toList()));
        
        items.addAll(overframeBuilds.getPrimaryWeapons().stream()
                .filter(foundItem -> foundItem.getName().contains(item))
                .collect(Collectors.toList()));
        
        items.addAll(overframeBuilds.getSecondaryWeapons().stream()
                .filter(foundItem -> foundItem.getName().contains(item))
                .collect(Collectors.toList()));
        
        items.addAll(overframeBuilds.getMeeleWeapons().stream()
                .filter(foundItem -> foundItem.getName().contains(item))
                .collect(Collectors.toList()));
        
        return items.stream()
                .limit(5)
                .collect(Collectors.toList());
    }

    private EmbedCreateSpec[] getEmbeddedResult(List<OverframeItemDTO> foundItems) {
        List<EmbedCreateSpec> embeds = new ArrayList<>();
        
        for (OverframeItemDTO foundItem : foundItems) {
            EmbedCreateSpec.Builder builder = EmbedCreateSpec.builder()
                .color(Color.ENDEAVOUR)
                .title(foundItem.getName())
                .description(DESCRIPTION)
                .thumbnail(foundItem.getPictureUrl())
                .timestamp(Instant.now());
            
            foundItem.getBuilds().stream().limit(5).forEach(build -> builder.addField(
                    EMBED_TITLE
                            .replace("{title}", build.getTitle())
                            .replace("{votes}", build.getVotes()),
                    EMBED_DESCRIPTION
                            .replace("{url}", build.getUrl())
                            .replace("{forma}", build.getFormasUsed())
                            .replace("{guide}", build.getGuideLength()),
                    true));
            
            embeds.add(builder.build());
        }
        
        EmbedCreateSpec[] embedArray = new EmbedCreateSpec[foundItems.size()];
        return embeds.toArray(embedArray);
    }
    
}
