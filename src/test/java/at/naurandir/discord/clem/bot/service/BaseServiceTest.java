package at.naurandir.discord.clem.bot.service;

import org.springframework.boot.test.mock.mockito.MockBean;

/**
 *
 * @author Naurandir
 */
public abstract class BaseServiceTest {
    
    @MockBean
    private BotService botService;
}
