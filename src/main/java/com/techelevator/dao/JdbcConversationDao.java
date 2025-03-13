package com.techelevator.dao;

import com.techelevator.model.Conversation;
import com.techelevator.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcConversationDao implements ConversationDao {

    private final JdbcTemplate jdbcTemplate;

    public JdbcConversationDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Conversation> getAllConversationsForUser(Long userId) {
        List<Conversation> conversations = new ArrayList<>();
        String sql = "SELECT c.conversation_id, created_at " +
                "FROM conversations c " +
                "JOIN conversation_participants cp ON c.conversation_id = cp.conversation_id " +
                "WHERE cp.user_id = ?";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId);
        while (results.next()) {
            Conversation conv = new Conversation();
            conv.setConversationId(results.getLong("conversation_id"));
            conv.setCreatedAt(results.getTimestamp("created_at"));
            conversations.add(conv);
        }
        return conversations;
    }

    @Override
    public Conversation createConversation(User sender, List<User> participants) {
        String sql = "INSERT INTO conversations (created_at) VALUES (CURRENT_TIMESTAMP) RETURNING conversation_id";
        Long conversationId = jdbcTemplate.queryForObject(sql, Long.class);

        String participantSql = "INSERT INTO conversation_participants (conversation_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(participantSql, conversationId, sender.getUserId());
        for (User participant : participants) {
            jdbcTemplate.update(participantSql, conversationId, participant.getUserId());
        }

        Conversation conv = new Conversation();
        conv.setConversationId(conversationId);
        conv.setCreatedAt(new java.util.Date());
        conv.setParticipants(getParticipants(conversationId));
        return conv;
    }

    @Override
    public Conversation getConversationById(Long conversationId) {
        String sql = "SELECT conversation_id, created_at " +
                "FROM conversations " +
                "WHERE conversation_id = ?";

        Conversation conversation = null;
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, conversationId);
        if (results.next()) {
            conversation = new Conversation();
            conversation.setConversationId(results.getLong("conversation_id"));
            conversation.setCreatedAt(results.getTimestamp("created_at"));
            conversation.setParticipants(getParticipants(conversationId));
        }
        return conversation;
    }

    @Override
    public List<User> getParticipants(Long conversationId) {
        String sql = "SELECT u.user_id, u.username, u.email, u.user_type " +
                "FROM users u " +
                "JOIN conversation_participants cp ON u.user_id = cp.user_id " +
                "WHERE cp.conversation_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            User user = new User();
            user.setUserId(rs.getLong("user_id"));
            user.setUsername(rs.getString("username"));
            user.setEmail(rs.getString("email"));
            user.setUserType(User.UserType.valueOf(rs.getString("user_type")));
            return user;
        }, conversationId);
    }

    @Override
    public boolean addUserToConversation(Long conversationId, Long userId) {
        // First check if user is already in conversation
        String checkSql = "SELECT COUNT(*) FROM conversation_participants " +
                "WHERE conversation_id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, conversationId, userId);

        if (count != null && count > 0) {
            return false; // User already in conversation
        }

        // Check if conversation exists
        String convoCheckSql = "SELECT COUNT(*) FROM conversations WHERE conversation_id = ?";
        Integer convoCount = jdbcTemplate.queryForObject(convoCheckSql, Integer.class, conversationId);

        if (convoCount == null || convoCount == 0) {
            return false; // Conversation doesn't exist
        }

        String sql = "INSERT INTO conversation_participants (conversation_id, user_id) VALUES (?, ?)";
        int rowsAffected = jdbcTemplate.update(sql, conversationId, userId);
        return rowsAffected > 0;
    }

    @Override
    public boolean removeUserFromConversation(Long conversationId, Long userId) {
        String sql = "DELETE FROM conversation_participants " +
                "WHERE conversation_id = ? AND user_id = ?";
        int rowsAffected = jdbcTemplate.update(sql, conversationId, userId);
        return rowsAffected > 0;
    }

    @Override
    public boolean deleteConversation(Long conversationId, Long userId) {
        // First verify user is in the conversation
        String checkSql = "SELECT COUNT(*) FROM conversation_participants " +
                "WHERE conversation_id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, conversationId, userId);

        if (count == null || count == 0) {
            return false; // User not in conversation or conversation doesn't exist
        }

        // Delete all participants first (due to foreign key constraints)
        String deleteParticipantsSql = "DELETE FROM conversation_participants WHERE conversation_id = ?";
        jdbcTemplate.update(deleteParticipantsSql, conversationId);

        // Then delete the conversation
        String deleteSql = "DELETE FROM conversations WHERE conversation_id = ?";
        int rowsAffected = jdbcTemplate.update(deleteSql, conversationId);
        return rowsAffected > 0;
    }
}