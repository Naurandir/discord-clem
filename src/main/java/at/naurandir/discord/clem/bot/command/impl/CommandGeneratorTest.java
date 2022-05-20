package at.naurandir.discord.clem.bot.command.impl;

import at.naurandir.discord.clem.bot.command.Command;
import at.naurandir.discord.clem.bot.command.CommandGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 *
 * @author Naurandir
 */

@Slf4j
@Component
public class CommandGeneratorTest implements CommandGenerator {

    @Override
    public Command getCommand() {
        return new CommandTest();
    }
}
