package at.naurandir.discord.clem.bot.repository;

import at.naurandir.discord.clem.bot.model.trader.VoidTrader;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author Naurandir
 */
public interface VoidTraderRepository extends CrudRepository<VoidTrader, Long> {

    Optional<VoidTrader> findByEndDateIsNull();
}
