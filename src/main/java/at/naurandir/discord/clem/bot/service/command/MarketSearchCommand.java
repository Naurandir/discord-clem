package at.naurandir.discord.clem.bot.service.command;

import at.naurandir.discord.clem.bot.model.enums.OnlineStatus;
import at.naurandir.discord.clem.bot.model.enums.OrderType;
import at.naurandir.discord.clem.bot.model.item.MarketItem;
import at.naurandir.discord.clem.bot.service.MarketService;
import at.naurandir.discord.clem.bot.service.client.dto.market.MarketOrderDTO;
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
import java.util.Map.Entry;
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
public class MarketSearchCommand implements Command {
    
    @Autowired
    private MarketService marketService;
    
    private static final String BUY_DESCRIPTION = "***{platinum} Platinum*** User *{user}* (Reputation {reputation})\n`/w {user} Hi! I want to buy: {item} for {platinum} platinum. Greetings :)`\n\n";
    private static final String ASSETS_BASE_URL = "https://warframe.market/static/assets/";
    
    @Override
    public String getCommandWord() {
        return "market-search";
    }

    @Override
    public String getDescription() {
        return "Shows current prices of an item on the warframe market.\n"
                + "Note it shows maximum up to 5 different items and max 5 current lowest prices. \n"
                + "Usage: *<bot-prefix> market-search <item>*.";
    }

    @Override
    public Mono<Void> handle(MessageCreateEvent event) {
        String item = getItem(event.getMessage().getContent());
        if (item.length() < 3) {
            return event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage("The given input [" + item + "] is too short. Please provide a longer name to search"))
                .then();
        }
        
        List<MarketItem> foundMarketItems = getMarketItems(item);
        Map<MarketItem, List<MarketOrderDTO>> foundSellOrders = getSellOrders(foundMarketItems);
        
        EmbedCreateSpec[] embeddedMessages = getEmbeddedResult(foundSellOrders);
        return event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage(embeddedMessages))
                .then();
    }
    
    private String getItem(String content) {
        String[] splitted = content.split(" "); // 'prefix command <item>'
        String[] words = Arrays.copyOfRange(splitted, 2, splitted.length);
        return StringUtils.join(words, " ").trim();
    }

    private List<MarketItem> getMarketItems(String item) {
        return marketService.getMarketItems(item).stream()
                .limit(5)
                .collect(Collectors.toList());
    }

    private Map<MarketItem, List<MarketOrderDTO>> getSellOrders(List<MarketItem> foundItems) {
        Map<MarketItem, List<MarketOrderDTO>> itemOrders = new HashMap<>();
        
        for (MarketItem marketItem : foundItems) {
            try {
                List<MarketOrderDTO> foundOrders = marketService.getCurrentOrders(marketItem);

                foundOrders = foundOrders.stream()
                        .filter(order -> order.getOrderType() == OrderType.SELL && order.getVisible())
                        .filter(order -> order.getUser().getStatus() == OnlineStatus.INGAME
                        || order.getUser().getStatus() == OnlineStatus.ONLINE)
                        .sorted((order1, order2) -> order1.getPlatinum().compareTo(order2.getPlatinum()))
                        .limit(5)
                        .collect(Collectors.toList());

                itemOrders.put(marketItem, foundOrders);
            } catch (IOException ex) {
                log.error("getSellOrders: coud not receive sell order for [{}], error: ", marketItem, ex);
            }
        }
        
        return itemOrders;
    }

    private EmbedCreateSpec[] getEmbeddedResult(Map<MarketItem, List<MarketOrderDTO>> foundSellOrders) {
        List<EmbedCreateSpec> embeds = new ArrayList<>();
        EmbedCreateSpec[] embedArray = new EmbedCreateSpec[foundSellOrders.size()];
        
        for (Entry<MarketItem, List<MarketOrderDTO>> entry : foundSellOrders.entrySet()) {
            StringBuilder description = new StringBuilder("");
            
            entry.getValue().forEach(order -> description.append(BUY_DESCRIPTION.replace("{item}", entry.getKey().getName())
                .replace("{platinum}", String.valueOf(order.getPlatinum()))
                .replace("{platinum}", String.valueOf(order.getPlatinum()))
                .replace("{user}", order.getUser().getName())
                .replace("{user}", order.getUser().getName())
                .replace("{reputation}", String.valueOf(order.getUser().getReputation()))));
            
            EmbedCreateSpec embed= EmbedCreateSpec.builder()
                .color(Color.DEEP_SEA)
                .title(entry.getKey().getName())
                .description(description.toString())
                .thumbnail(ASSETS_BASE_URL + entry.getKey().getThumb())
                .timestamp(Instant.now())
                .build();
            
            embeds.add(embed);
        }
        
        return embeds.toArray(embedArray);
    }
    
}
