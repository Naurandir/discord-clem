package at.naurandir.discord.clem.bot.repository;

import at.naurandir.discord.clem.bot.model.Alert;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author Naurandir
 */
public interface AlertRepository extends CrudRepository<Alert, Long> {
    
    List<Alert> findByEndDateIsNull();
    
    List<Alert> findByEndDateAfter(LocalDateTime time);
}
