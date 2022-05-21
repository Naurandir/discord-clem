
package at.naurandir.discord.clem.bot.service;

import at.naurandir.discord.clem.bot.service.command.Command;
import at.naurandir.discord.clem.bot.service.command.CommandTest;
import at.naurandir.discord.clem.bot.service.client.WarframeClient;
import at.naurandir.discord.clem.bot.service.client.dto.WorldStateDTO;
import discord4j.core.GatewayDiscordClient;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Naurandir
 */
@Slf4j
@Service
public class WarframeService {
    
    @Autowired
    private List<Command> commands;
    
    private WarframeClient client;
    
    @PostConstruct
    public void init() {
        client = new WarframeClient();
    }
    
    public void updateWorldState(GatewayDiscordClient discordClient) {
        try {
            WorldStateDTO dto = client.getCurrentWorldState();
            log.info("getCurrentWorldState: received dto [{}]", dto);
            // update all commands with required information
            
            // if changes happened to last time create push notifications into discord
        } catch (IOException ex) {
            log.error("getCurrentWorldState: update throwed exception: ", ex);
        }
    }
}
