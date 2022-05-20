package at.naurandir.discord.clem.bot.command.impl;

import at.naurandir.discord.clem.bot.command.Command;
import at.naurandir.discord.clem.bot.command.CommandGenerator;

/**
 *
 * @author Naurandir
 */
public class CommandGeneratorTest implements CommandGenerator {

    @Override
    public Command getCommand() {
        return new CommandTest();
    }
    
}
