package com.techelevator.dao;

import com.techelevator.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

@Repository
public class JdbcMessageDao implements MessageDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcMessageDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<Message> messageRowMapper = (rs, rowNum) -> {
        Message message = new Message();
        message.setMessageId(rs.getLong("message_id"));
        message.setConversationId(rs.getLong("conversation_id"));
        message.setSenderId(rs.getLong("sender_id"));
        message.setReceiverId(rs.getLong("receiver_id"));
        message.setContent(rs.getString("content"));
        message.setParentMessageId(rs.getLong("parent_message_id"));
        Timestamp sentAt = rs.getTimestamp("sent_at");
        message.setSentAt(sentAt != null ? sentAt.toString() : null); // Convert Timestamp to String
        return message;
    };

    @Override
    @Transactional
    public Message sendMessage(Long conversationId, Long senderId, Long receiverId, String content, Long parentMessageId) {
        String sql = "INSERT INTO messages (conversation_id, sender_id, receiver_id, content, parent_message_id, sent_at) " +
                "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP) RETURNING message_id";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"message_id"});
            ps.setLong(1, conversationId);
            ps.setLong(2, senderId);
            ps.setObject(3, receiverId);
            ps.setString(4, content);
            ps.setObject(5, parentMessageId);
            return ps;
        }, keyHolder);

        Message message = new Message();
        message.setMessageId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        message.setConversationId(conversationId);
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setParentMessageId(parentMessageId);
        message.setSentAt(new Timestamp(System.currentTimeMillis()).toString()); // Convert to String
        return message;
    }

    @Override
    public List<Message> getMessagesByConversation(Long conversationId) {
        String sql = "SELECT * FROM messages WHERE conversation_id = ? ORDER BY sent_at ASC";
        return jdbcTemplate.query(sql, messageRowMapper, conversationId);
    }

    @Override
    @Transactional
    public boolean markMessageAsRead(Long messageId, Long userId) {
        String sql = "UPDATE messages SET read_at = CURRENT_TIMESTAMP WHERE message_id = ? AND (receiver_id = ? OR ? IN (SELECT user_id FROM conversation_participants WHERE conversation_id = (SELECT conversation_id FROM messages WHERE message_id = ?)))";
        int updatedCount = jdbcTemplate.update(sql, messageId, userId, userId, messageId);
        return updatedCount > 0;
    }

    @Override
    @Transactional
    public boolean deleteMessage(Long messageId, Long userId) {
        String sql = "DELETE FROM messages WHERE message_id = ? AND sender_id = ?";
        int deletedCount = jdbcTemplate.update(sql, messageId, userId);
        return deletedCount > 0;
    }

    @Override
    @Transactional
    public Message updateMessage(Long messageId, Long userId, String newContent) {
        String sql = "UPDATE messages SET content = ? WHERE message_id = ? AND sender_id = ?";
        int updatedCount = jdbcTemplate.update(sql, newContent, messageId, userId);
        if (updatedCount > 0) {
            Message updatedMessage = new Message();
            updatedMessage.setMessageId(messageId);
            updatedMessage.setContent(newContent);
            return updatedMessage; // Note: sentAt isn’t set here—might need to fetch full message
        }
        return null;
    }
}