
package at.naurandir.discord.clem.bot.repository;

import at.naurandir.discord.clem.bot.model.news.News;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Naurandir
 */
@Repository
public interface NewsRepository extends CrudRepository<News, Long> {
    
    List<News> findByEndDateIsNull();

    List<News> findByEndDateIsNotNull();
}
