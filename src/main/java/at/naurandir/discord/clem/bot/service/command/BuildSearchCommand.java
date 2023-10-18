package at.naurandir.discord.clem.bot.service.command;

import at.naurandir.discord.clem.bot.model.item.Item;
import at.naurandir.discord.clem.bot.service.WarframeService;
import at.naurandir.discord.clem.bot.service.WeaponService;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 *
 * @author Naurandir
 */
@Slf4j
@Component
public class BuildSearchCommand implements Command {
    
    @Autowired
    private WarframeService warframeService;
    
    @Autowired
    private WeaponService weaponService;
    
    private static final String DESCRIPTION = "List of found Overframe Builds";
    private static final String EMBED_TITLE = "***{title}***";
    private static final String EMBED_DESCRIPTION = "{url}\n {forma} - {guide} ({votes} Votes)";
    
    private static final String TITLE_NOT_FOUND = "No Warframe / Weapon found";
    private static final String DESCRIPTION_NOT_FOUND = "No Warframe / Weapon could be found to given Search. Please look if the input is misspelled.";

    @Override
    public String getCommandWord() {
        return "build-search";
    }

    @Override
    public String getDescription() {
        return "Shows top build for given warframe / weapon from overframe.\n"
                + "Note it shows only max 5 different items and best 5 builds of them. \n"
                + "Usage: *<bot-prefix> build-search <item>*.";
    }

    @Override
    public Mono<Void> handle(MessageCreateEvent event) {
        String item = getItem(event.getMessage().getContent());
        if (item.length() < 3) {
            return event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage("The given input [" + item + "] is too short. Please provide a longer name to search"))
                .timeout(Duration.ofSeconds(60)).then();
        }
        
        List<Item> foundItems = getBuildItems(item);
        log.debug("handle: found [{}] items for given input [{}]", foundItems.size(), item);
        
        if (foundItems.isEmpty()) {
            return event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage(createErrorResponse(TITLE_NOT_FOUND, DESCRIPTION_NOT_FOUND)))
                .timeout(Duration.ofSeconds(60)).then();
        }
        
        EmbedCreateSpec[] embeddedMessages = getEmbeddedResult(foundItems);
        return event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage(embeddedMessages))
                .timeout(Duration.ofSeconds(60)).then();
    }
    
    private String getItem(String content) {
        String[] splitted = content.split(" "); // 'prefix command <item>'
        String[] words = Arrays.copyOfRange(splitted, 2, splitted.length);
        return StringUtils.join(words, " ").trim();
    }
    
    private List<Item> getBuildItems(String name) {
        List<Item> items = new ArrayList<>();
        
        items.addAll(warframeService.findWarframesByName(name));
        items.addAll(weaponService.findWeaponsByName(name));
        
        return items.stream()
                .limit(5)
                .collect(Collectors.toList());
    }

    private EmbedCreateSpec[] getEmbeddedResult(List<Item> foundItems) {
        List<EmbedCreateSpec> embeds = new ArrayList<>();
        
        for (Item foundItem : foundItems) {
            EmbedCreateSpec.Builder builder = EmbedCreateSpec.builder()
                .color(Color.ENDEAVOUR)
                .title(foundItem.getName())
                .description(DESCRIPTION)
                .thumbnail(foundItem.getWikiaThumbnail())
                .timestamp(Instant.now());
            
            foundItem.getBuilds().stream().sorted((build1, build2) -> build2.getVotes() - build1.getVotes())
                    .limit(5)
                    .forEach(build -> builder.addField(
                        EMBED_TITLE
                            .replace("{title}", build.getTitle()),
                        EMBED_DESCRIPTION
                            .replace("{url}", build.getUrl())
                            .replace("{forma}", build.getFormasUsed())
                            .replace("{votes}", String.valueOf(build.getVotes()))
                            .replace("{guide}", build.getGuideLength()),
                        true));
            
            embeds.add(builder.build());
        }
        
        EmbedCreateSpec[] embedArray = new EmbedCreateSpec[foundItems.size()];
        return embeds.toArray(embedArray);
    }
    
    private EmbedCreateSpec createErrorResponse(String title, String description) {
        return EmbedCreateSpec.builder()
                .color(Color.ENDEAVOUR)
                .title(title)
                .description(description)
                .timestamp(Instant.now())
                .build();
    }
    
}
