package at.naurandir.discord.clem.bot.service.command;

import at.naurandir.discord.clem.bot.model.enums.OnlineStatus;
import at.naurandir.discord.clem.bot.model.item.MarketLichWeapon;
import at.naurandir.discord.clem.bot.service.MarketService;
import at.naurandir.discord.clem.bot.service.client.dto.market.MarketLichAuctionDTO;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
public class MarketLichSearchCommand implements Command {
    
    @Autowired
    private MarketService marketService;
    
    private static final String ASSETS_BASE_URL = "https://warframe.market/static/assets/";
    private static final String DESCRIPTION = "List of found Lich Auctions";
    
    private static final String TITLE_NOT_FOUND = "No Lich Auctions found";
    private static final String DESCRIPTION_NOT_FOUND = "No Lich Auctions could be found to given Search. Please look if the input is misspelled.";
    
    private static final String TITLE_TOO_SHORT = "Too Short Search Input";
    private static final String DESCRIPTION_TOO_SHORT = "In order to make a decent search, please input minimum 3 characters for the item.";

    private static final String TITLE_ELEMENT_INVALID = "Element is Invalid";
    private static final String DESCRIPTION_ELEMENT_INVALID = "The given element not existing, "
            + "please use one of the following:\nimpact :hammer:, heat :fire:, cold :snowflake:, electricity :zap:, toxin :skull_crossbones:, magnetic :magnet:, radiation :radiactive:";
    
    private static final String EMBED_TITLE = "{user} Reputation: {reputation}";
    private static final String EMBED_DESCRIPTION = "[Auction](auctionUrl)\n"
            + "*Start:* {startingPrice} plat\n*Current:* {topBid} plat\n*Direct:* {buyoutPrice} plat\n"
            + "*Element:* {damage}% {damageType}\n*Quirk:* {quirk}\n*Ephemera:* {ephemera}";
    
    private static final String AUCTION_BASE_URL = "https://warframe.market/auction/";
    
    private static final String INVALID_ELEMENT = "Invalid_Element";
    
    private final List<String> elements = List.of("impact", "heat", "cold", "electricity", "toxin", "magnetic", "radiation");
    private final Map<String, String> elementEmojis = Map.of(
            "impact", ":hammer:",
            "heat", ":fire:",
            "cold", ":snowflake:",
            "electricity", ":zap:",
            "toxin", ":skull_crossbones:",
            "magnetic", ":magnet:",
            "radiation", ":radiactive:");
    
    @Override
    public String getCommandWord() {
        return "market-lich-search";
    }

    @Override
    public String getDescription() {
        return "Shows current auctions of lichs on warframe market.\n"
                + "Note it shows only max 5 different items and max 5 current auctions. \n"
                + "Usage: *<bot-prefix> market-lich-search <optional --element> <item>*.\n"
                + "Example: *!clem market-lich-search --heat Nukor*\n"
                + "Valid elements: impact :hammer:, heat :fire:, cold :snowflake:, electricity :zap:, toxin :skull_crossbones:, magnetic :magnet:, radiation :radioactive:";
    }

    @Override
    public Mono<Void> handle(MessageCreateEvent event) {
        Optional<String> selectedElement = getPossibleElement(event.getMessage().getContent());
        if (selectedElement.isPresent() && selectedElement.get().equals(INVALID_ELEMENT)) {
            return event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage(createErrorResponse(TITLE_ELEMENT_INVALID, DESCRIPTION_ELEMENT_INVALID)))
                .then();
        }
        
        String item = getItem(event.getMessage().getContent(), selectedElement);
        if (item.length() < 3) {
            return event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage(createErrorResponse(TITLE_TOO_SHORT, DESCRIPTION_TOO_SHORT)))
                .then();
        }
        
        List<MarketLichWeapon> foundLichWeapons = getLichWeapons(item);
        log.debug("handle: found [{}] lich weapons for given input [{}]", foundLichWeapons.size(), item);
        
        if (foundLichWeapons.isEmpty()) {
            return event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage(createErrorResponse(TITLE_NOT_FOUND, DESCRIPTION_NOT_FOUND)))
                .then();
        }
        
        Map<MarketLichWeapon, List<MarketLichAuctionDTO>> foundLichAuctions = getAuctions(foundLichWeapons, selectedElement);
        
        EmbedCreateSpec[] embeddedMessages = getEmbeddedResult(foundLichAuctions);
        return event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage(embeddedMessages))
                .then();
    }
    
    private Optional<String> getPossibleElement(String content) {
        String[] splitted = content.split(" ");
        
        if (splitted.length <= 3) { // only <prefix> command <item>
            return Optional.empty();
        }
        
        String element = splitted[2]; // <prefix> command <element> <item>
        if (element.startsWith("--")) {
            String foundElement = element.substring(2).toLowerCase();
            if (!elements.contains(foundElement)) {
                return Optional.of(INVALID_ELEMENT);
            }
            return Optional.of(foundElement);
        }
        
        return Optional.empty();
    }
    
    private String getItem(String content, Optional<String> selectedElement) {
        String[] splitted = content.split(" "); // 'prefix command <item>'
        
        int startIndex = selectedElement.isPresent() ? 3 : 2;
        String[] words = Arrays.copyOfRange(splitted, startIndex, splitted.length);
        
        return StringUtils.join(words, " ").trim();
    }
    
    private List<MarketLichWeapon> getLichWeapons(String item) {
        return marketService.getMarketLichWeapons(item).stream()
                .limit(3)
                .collect(Collectors.toList());
    }
    
    private Map<MarketLichWeapon, List<MarketLichAuctionDTO>> getAuctions(List<MarketLichWeapon> marketLichWeapons, 
            Optional<String> element) {
        Map<MarketLichWeapon, List<MarketLichAuctionDTO>> lichAuctions = new HashMap<>();
        
        for (MarketLichWeapon lichWeapon : marketLichWeapons) {
            try {
                List<MarketLichAuctionDTO> foundAuctions = marketService.getCurrentLichAuctions(lichWeapon, element);
                
                foundAuctions = foundAuctions.stream()
                        .filter(auction -> !auction.getClosed() && auction.getVisible())
                        .filter(auction -> auction.getOwner().getStatus() == OnlineStatus.INGAME || 
                                           auction.getOwner().getStatus() == OnlineStatus.ONLINE)
                        .sorted((auction1, auction2) -> 
                                auction1.getStartingPrice().compareTo(auction2.getStartingPrice()))
                        .limit(6)
                        .collect(Collectors.toList());
                
                lichAuctions.put(lichWeapon, foundAuctions);
                
            } catch (IOException ex) {
                log.error("getAuctions: coud not receive auctions for [{}], error: ", lichWeapon, ex);
            }
        }
        
        return lichAuctions;
    }

    private EmbedCreateSpec[] getEmbeddedResult(Map<MarketLichWeapon, List<MarketLichAuctionDTO>> foundLichAuctions) {
        List<EmbedCreateSpec> embeds = new ArrayList<>();
        EmbedCreateSpec[] embedArray = new EmbedCreateSpec[foundLichAuctions.size()];
        
        for (Map.Entry<MarketLichWeapon, List<MarketLichAuctionDTO>> entry : foundLichAuctions.entrySet()) {
            
            EmbedCreateSpec.Builder builder = EmbedCreateSpec.builder()
                .color(Color.RUST)
                .title(entry.getKey().getName())
                .description(DESCRIPTION)
                .thumbnail(ASSETS_BASE_URL + entry.getKey().getThumb())
                .timestamp(Instant.now());
            
            entry.getValue().forEach(auction -> builder.addField(
                    EMBED_TITLE
                            .replace("{user}", auction.getOwner().getName())
                            .replace("{reputation}", String.valueOf(auction.getOwner().getReputation())),
                    EMBED_DESCRIPTION
                            .replace("{auctionUrl}", AUCTION_BASE_URL + auction.getId())
                            .replace("{damageType}", elementEmojis.get(auction.getItem().getElement()))
                            .replace("{damage}", String.valueOf(auction.getItem().getDamage()))
                            .replace("{ephemera}", auction.getItem().getHasEphemera() ? "yes" : "no")
                            .replace("{quirk}", Objects.requireNonNullElseGet(auction.getItem().getQuirk(), () -> "-").replace("-", " "))
                            .replace("{startingPrice}", String.valueOf(auction.getStartingPrice()))
                            .replace("{topBid}", auction.getTopBid() != null ? String.valueOf(auction.getTopBid()) : "-")
                            .replace("{buyoutPrice}", auction.getBuyoutPrice() != null ? String.valueOf(auction.getBuyoutPrice()) : "-"),
                    true));
            
            embeds.add(builder.build());
        }
        
        return embeds.toArray(embedArray);
    }
    
    private EmbedCreateSpec createErrorResponse(String title, String description) {
        return EmbedCreateSpec.builder()
                .color(Color.RUST)
                .title(title)
                .description(description)
                .timestamp(Instant.now())
                .build();
    }
}
