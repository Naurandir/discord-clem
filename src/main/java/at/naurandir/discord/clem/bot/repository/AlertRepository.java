package at.naurandir.discord.clem.bot.repository;

import at.naurandir.discord.clem.bot.model.Alert;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author Naurandir
 */
public interface AlertRepository extends CrudRepository<Alert, Long> {
    
}
