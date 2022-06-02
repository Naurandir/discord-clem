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
    private static final String DESCRIPTION = "{auctionUrl}\n"
            + "***{damageType} {damage}*** - quirk: {quirk}, has ephemera: {ephemera}\n"
            + "Start Price ***{startingPrice} plat***, Current Top Bid ***{topBid} plat***, Buy Out Price: ***{buyoutPrice} plat***.\n"
            + "*User {user}* (Reputation {reputation})\n\n";
    
    private static final String ASSETS_BASE_URL = "https://warframe.market/static/assets/";
    private static final String AUCTION_BASE_URL = "https://warframe.market/auction/";
    
    private final WarframeClient warframeClient = new WarframeClient();
    
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
                + "Valid elements: impact, heat, cold, electricity, toxin, magnetic, radiation.";
    }

    @Override
    public Mono<Void> handle(MessageCreateEvent event, WarframeState warframeState) {
        Optional<String> selectedElement = getPossibleElement(event.getMessage().getContent());
        String item = getItem(event.getMessage().getContent(), selectedElement);
        if (item.length() < 3) {
            return event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage("The given input [" + item + "] is too short. Please provide a longer name to search"))
                .then();
        }
        
        List<MarketLichWeaponDTO> foundLichWeapons = getLichWeapons(item, warframeState.getMarketLichWeapons());
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
            return Optional.of(element.substring(2));
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
        List<MarketLichWeaponDTO> interestingWeapons = marketLichWeapons.stream().limit(5).collect(Collectors.toList());
        
        for (MarketLichWeaponDTO lichWeapon : interestingWeapons) {
            try {
                List<MarketLichAuctionDTO> foundAuctions = warframeClient.getCurrentLichAuctions(lichWeapon.getUrlName(), element)
                        .getPayload().get("auctions");
                
                foundAuctions = foundAuctions.stream()
                        .filter(auction -> !auction.getClosed() && auction.getVisible())
                        .sorted((auction1, auction2) -> 
                                auction1.getStartingPrice().compareTo(auction2.getStartingPrice()))
                        .limit(5)
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
            StringBuilder description = new StringBuilder("");
            
            entry.getValue().forEach(auction -> description.append(DESCRIPTION
                .replace("{auctionUrl}", AUCTION_BASE_URL + auction.getId())
                .replace("{user}", auction.getOwner().getName())
                .replace("{reputation}", String.valueOf(auction.getOwner().getReputation()))
                .replace("{damageType}", auction.getItem().getElement())
                .replace("{damage}", String.valueOf(auction.getItem().getDamage()))
                .replace("{quirk}", Objects.requireNonNullElseGet(auction.getItem().getQuirk(), () -> "-"))
                .replace("{ephemera}", auction.getItem().getHasEphemera() ? "yes" : "no")
                .replace("{startingPrice}", String.valueOf(auction.getStartingPrice()))
                .replace("{topBid}", auction.getTopBid() != null ? String.valueOf(auction.getTopBid()) : "-")
                .replace("{buyoutPrice}", auction.getBuyoutPrice() != null ? String.valueOf(auction.getBuyoutPrice()) : "-")));
            
            EmbedCreateSpec embed= EmbedCreateSpec.builder()
                .color(Color.RUST)
                .title(entry.getKey().getItemName())
                .description(description.toString())
                .thumbnail(ASSETS_BASE_URL + entry.getKey().getThumb())
                .timestamp(Instant.now())
                .build();
            
            embeds[i] = embed;
            i++;
        }
        
        return embeds;
    }
}
