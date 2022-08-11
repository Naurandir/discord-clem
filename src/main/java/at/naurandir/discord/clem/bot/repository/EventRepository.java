package at.naurandir.discord.clem.bot.repository;

import at.naurandir.discord.clem.bot.model.event.Event;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author Naurandir
 */
public interface EventRepository extends CrudRepository<Event, Long> {
    
}
