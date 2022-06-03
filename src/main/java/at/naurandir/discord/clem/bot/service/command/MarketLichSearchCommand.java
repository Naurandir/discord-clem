package at.naurandir.discord.clem.bot.service.command;

import at.naurandir.discord.clem.bot.model.WarframeState;
import at.naurandir.discord.clem.bot.service.client.WarframeClient;
import at.naurandir.discord.clem.bot.service.client.dto.market.MarketLichAuctionDTO;
import at.naurandir.discord.clem.bot.service.client.dto.market.MarketLichWeaponDTO;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
public class MarketLichSearchCommand implements Command {
    
    private static final String DESCRIPTION = "List of found Lich Auctions";
    
    private static final String TITLE_NOT_FOUND = "No Lich Auctions found";
    private static final String DESCRIPTION_NOT_FOUND = "No Lich Auctions could be found to given Search. Please look if the input is misspelled.";
    
    private static final String TITLE_TOO_SHORT = "Too Short Search Input";
    private static final String DESCRIPTION_TOO_SHORT = "In order to make a decent search, please input minimum 3 characters for the item.";

    private static final String TITLE_ELEMENT_INVALID = "Element is Invalid";
    private static final String DESCRIPTION_ELEMENT_INVALID = "The given element not existing, "
            + "please use one of the following:\nimpact :hammer:, heat :fire:, cold :snowflake:, electricity :zap:, toxin :skull_crossbones:, magnetic :magnet:, radiation :radiactive:";
    
    private static final String EMBED_TITLE = "{auctionUrl}";
    private static final String EMBED_DESCRIPTION = "*Start:* {startingPrice} plat\n*Current:* {topBid} plat\n*Direct:* {buyoutPrice} plat\n"
            + "*Element:* {damage}% {damageType}\n*Quirk:* {quirk}\n*Ephemera:* {ephemera}\n*User:* {user}\nReputation: {reputation}";
    
    private static final String ASSETS_BASE_URL = "https://warframe.market/static/assets/";
    private static final String AUCTION_BASE_URL = "https://warframe.market/auction/";
    
    private static final String INVALID_ELEMENT = "Invalid_Element";
    
    private final WarframeClient warframeClient = new WarframeClient();
    
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
        return "Shows current auctions of a lich on the warframe market.\n"
                + "Note it shows only max 5 different items with similar name and max 5 current auctions. \n"
                + "Usage: *<bot-prefix> market-lich-search <optional --element> <item>*.\n"
                + "Example: *!clem market-lich-search --heat Nukor\n"
                + "Valid elements: impact :hammer:, heat :fire:, cold :snowflake:, electricity :zap:, toxin :skull_crossbones:, magnetic :magnet:, radiation :radiactive:";
    }

    @Override
    public Mono<Void> handle(MessageCreateEvent event, WarframeState warframeState) {
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
        
        List<MarketLichWeaponDTO> foundLichWeapons = getLichWeapons(item, warframeState.getMarketLichWeapons());
        if (foundLichWeapons.isEmpty()) {
            return event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage(createErrorResponse(TITLE_NOT_FOUND, DESCRIPTION_NOT_FOUND)))
                .then();
        }
        
        Map<MarketLichWeaponDTO, List<MarketLichAuctionDTO>> foundLichAuctions = getAuctions(foundLichWeapons, selectedElement);
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
    
    private List<MarketLichWeaponDTO> getLichWeapons(String item, List<MarketLichWeaponDTO> marketLichWeapons) {
        return marketLichWeapons.stream()
                .filter(weapon -> weapon.getItemName().contains(item))
                .collect(Collectors.toList());
    }
    
    private Map<MarketLichWeaponDTO, List<MarketLichAuctionDTO>> getAuctions(List<MarketLichWeaponDTO> marketLichWeapons, 
            Optional<String> element) {
        Map<MarketLichWeaponDTO, List<MarketLichAuctionDTO>> lichAuctions = new HashMap<>();
        
        // maximum 5 items will be analysed
        List<MarketLichWeaponDTO> interestingWeapons = marketLichWeapons.stream().limit(3).collect(Collectors.toList());
        
        for (MarketLichWeaponDTO lichWeapon : interestingWeapons) {
            try {
                List<MarketLichAuctionDTO> foundAuctions = warframeClient.getCurrentLichAuctions(lichWeapon.getUrlName(), element)
                        .getPayload().get("auctions");
                
                foundAuctions = foundAuctions.stream()
                        .filter(auction -> !auction.getClosed() && auction.getVisible())
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

    private EmbedCreateSpec[] getEmbeddedResult(Map<MarketLichWeaponDTO, List<MarketLichAuctionDTO>> foundLichAuctions) {
        
        EmbedCreateSpec[] embeds = new EmbedCreateSpec[foundLichAuctions.size()];
        int i=0;
        
        for (Map.Entry<MarketLichWeaponDTO, List<MarketLichAuctionDTO>> entry : foundLichAuctions.entrySet()) {
            
            EmbedCreateSpec.Builder builder = EmbedCreateSpec.builder()
                .color(Color.RUST)
                .title(entry.getKey().getItemName())
                .description(DESCRIPTION)
                .thumbnail(ASSETS_BASE_URL + entry.getKey().getThumb())
                .timestamp(Instant.now());
            
            entry.getValue().forEach(auction -> builder.addField(
                    EMBED_TITLE
                            .replace("{auctionUrl}", AUCTION_BASE_URL + auction.getId()), 
                    EMBED_DESCRIPTION
                            .replace("{user}", auction.getOwner().getName())
                            .replace("{reputation}", String.valueOf(auction.getOwner().getReputation()))
                            .replace("{damageType}", elementEmojis.get(auction.getItem().getElement()))
                            .replace("{damage}", String.valueOf(auction.getItem().getDamage()))
                            .replace("{ephemera}", auction.getItem().getHasEphemera() ? "yes" : "no")
                            .replace("{quirk}", Objects.requireNonNullElseGet(auction.getItem().getQuirk(), () -> "-").replace("-", " "))
                            .replace("{startingPrice}", String.valueOf(auction.getStartingPrice()))
                            .replace("{topBid}", auction.getTopBid() != null ? String.valueOf(auction.getTopBid()) : "-")
                            .replace("{buyoutPrice}", auction.getBuyoutPrice() != null ? String.valueOf(auction.getBuyoutPrice()) : "-"),
                    true));
            
            embeds[i] = builder.build();
            i++;
        }
        
        return embeds;
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
