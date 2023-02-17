/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.naurandir.discord.clem.bot.repository;

import at.naurandir.discord.clem.bot.model.chat.Conversation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Naurandir
 */
@Repository
public interface ConversationRepository extends CrudRepository<Conversation, Long> {
    
    List<Conversation> findByEndDateIsNull();

    List<Conversation> findByEndDateIsNotNull();

    Optional<Conversation> findByUserIdAndEndDateIsNull(Long userId);
}
