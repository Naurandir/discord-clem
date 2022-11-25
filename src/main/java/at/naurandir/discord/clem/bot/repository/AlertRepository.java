package at.naurandir.discord.clem.bot.repository;

import at.naurandir.discord.clem.bot.model.alert.Alert;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Naurandir
 */
@Repository
public interface AlertRepository extends CrudRepository<Alert, Long> {
    
    List<Alert> findByEndDateIsNull();

    List<Alert> findByEndDateIsNotNull();
}
