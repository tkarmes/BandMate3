package com.techelevator.dao;

import com.techelevator.model.Conversation;
import com.techelevator.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

@Repository
public class ConversationDaoImpl implements ConversationDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ConversationDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public Conversation createConversation(User creator, List<User> participants) {
        String sql = "INSERT INTO conversations (created_at) VALUES (CURRENT_TIMESTAMP) RETURNING conversation_id";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"conversation_id"});
            return ps;
        }, keyHolder);

        Long conversationId = Objects.requireNonNull(keyHolder.getKey()).longValue();

        // Add participants to conversation_participants table
        String participantSql = "INSERT INTO conversation_participants (conversation_id, user_id) VALUES (?, ?)";
        for (User participant : participants) {
            jdbcTemplate.update(participantSql, conversationId, participant.getUserId());
        }

        // Here you might want to fetch the conversation details with participants but for now, we'll return a basic Conversation
        Conversation conversation = new Conversation();
        conversation.setConversationId(conversationId);
        conversation.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        return conversation;
    }

    @Override
    public List<Conversation> getAllConversationsForUser(Long userId) {
        String sql = "SELECT c.conversation_id, c.created_at FROM conversations c " +
                "JOIN conversation_participants cp ON c.conversation_id = cp.conversation_id " +
                "WHERE cp.user_id = ?";
        // Implementation for fetching conversations for a user would go here
        // For now, returning null as a placeholder
        return null;
    }

    @Override
    public Conversation getConversationById(Long conversationId) {
        String sql = "SELECT conversation_id, created_at FROM conversations WHERE conversation_id = ?";
        // Implementation for fetching a specific conversation would go here
        // For now, returning null as a placeholder
        return null;
    }

    @Override
    public boolean addUserToConversation(Long conversationId, Long userId) {
        return false;
    }

    @Override
    public boolean removeUserFromConversation(Long conversationId, Long userId) {
        return false;
    }

    @Override
    public boolean deleteConversation(Long conversationId, Long userId) {
        return false;
    }

    // Implement other methods from ConversationDao interface similarly

}
