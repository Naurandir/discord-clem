package at.naurandir.discord.clem.bot.service;

import at.naurandir.discord.clem.bot.model.alert.Alert;
import at.naurandir.discord.clem.bot.model.channel.InterestingChannel;
import at.naurandir.discord.clem.bot.model.fissure.VoidFissure;
import at.naurandir.discord.clem.bot.model.trader.VoidTrader;
import at.naurandir.discord.clem.bot.repository.AlertRepository;
import at.naurandir.discord.clem.bot.repository.VoidFissureRepository;
import at.naurandir.discord.clem.bot.repository.VoidTraderItemRepository;
import at.naurandir.discord.clem.bot.repository.VoidTraderRepository;
import discord4j.common.util.Snowflake;
import discord4j.rest.http.client.ClientException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 *
 * @author Naurandir
 */
@Slf4j
@Service
public class CleanupService {
    
    @Autowired
    private BotService botService;
    
    @Autowired
    private InterestingChannelService interestingChannelService;
    
    @Autowired
    private VoidTraderRepository voidTraderRepository;
    
    @Autowired
    private VoidTraderItemRepository voidTraderItemRepository;
    
    @Autowired
    private VoidFissureRepository voidFissureRepository;
    
    @Autowired
    private AlertRepository alertRepository;
    
    @Scheduled(cron = "${discord.clem.cleanup}")
    public void cleanup() {
        removeNonExistendStickyPushMessages();
        removeOldAlerts();
        removeOldFissures();
        removeOldVoidTrader();
    }
    public void removeNonExistendStickyPushMessages() {
        List<InterestingChannel> channels = interestingChannelService.getAllChannels();
        
        for(InterestingChannel channel : channels) {
            if (channel.getStickyMessageId() == null) {
                continue;
            }  
            
            try {
                botService.getClient().getMessageById(Snowflake.of(channel.getChannelId()), 
                        Snowflake.of(channel.getStickyMessageId())).block();
            } catch (ClientException ex) {
                log.error("removeNonExistendStickyPushMessages: did not receive message [{}], reason: {}", 
                        channel.getStickyMessageId(), ex.getMessage());
                log.info("removeNonExistendStickyPushMessages: removing sticky message id from channel [{}]", channel);
                interestingChannelService.removeStickyMessageId(channel);
            }
        }
    }

    private void removeOldFissures() {
        log.info("removeOldFissures: deleting old fissures...");
        List<VoidFissure> fissures = voidFissureRepository.findByEndDateIsNotNull();
        fissures.forEach(fissure -> voidFissureRepository.delete(fissure));
        log.info("removeOldFissures: deleted [{}] old fissures.", fissures.size());
    }

    private void removeOldVoidTrader() {
        log.info("removeOldVoidTrader: deleting old traders...");
        List<VoidTrader> inactiveTraders = voidTraderRepository.findByEndDateIsNotNull();
        
        for (VoidTrader trader : inactiveTraders) {
            trader.getInventory().forEach(inventory -> voidTraderItemRepository.delete(inventory));
            voidTraderRepository.delete(trader);
        }
        log.info("removeOldVoidTrader: deleted [{}] old traders.", inactiveTraders.size());
    }
    
    private void removeOldAlerts() {
        log.info("removeOldAlerts: deleting old fissures...");
        List<Alert> alerts = alertRepository.findByEndDateIsNotNull();
        alerts.forEach(alert -> alertRepository.delete(alert));
        log.info("removeOldAlerts: deleted [{}] old fissures.", alerts.size());
    }
}
