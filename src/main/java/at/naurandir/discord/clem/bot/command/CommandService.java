
package at.naurandir.discord.clem.bot.command;

import at.naurandir.discord.clem.bot.command.impl.CommandTest;
import at.naurandir.discord.clem.bot.world.WarframeClient;
import discord4j.core.GatewayDiscordClient;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 *
 * @author Naurandir
 */
@Slf4j
@Service
public class CommandService {
    
    private WarframeClient client;
    private List<Command> commands;
    
    @PostConstruct
    public void init() {
        client = new WarframeClient();
        
        commands = new ArrayList<>();
        commands.add(new CommandTest());
    }
    
    public List<Command> getAllCommands() {
        return commands;
    }
    
    public void updateWorldState(GatewayDiscordClient discordClient) {
        client.getCurrentWorldState();
        // update all commands with required information
        
        // if changes happened to last time create push notifications into discord
    }
}
