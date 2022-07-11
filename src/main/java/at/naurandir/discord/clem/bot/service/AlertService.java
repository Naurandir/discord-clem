package at.naurandir.discord.clem.bot.service;

import at.naurandir.discord.clem.bot.model.AlertMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author aspiel
 */
@Service
public class AlertService {
    
    @Autowired
    private AlertMapper mapper;
}
