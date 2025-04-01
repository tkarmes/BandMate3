package com.techelevator.dao;

import com.techelevator.model.Conversation;
import com.techelevator.model.User;

import java.util.List;

public interface ConversationDao {
    Conversation createConversation(User creator, List<User> participants);
    List<Conversation> getAllConversationsForUser(Long userId);
    Conversation getConversationById(Long conversationId);
    boolean addUserToConversation(Long conversationId, Long userId);
    boolean removeUserFromConversation(Long conversationId, Long userId);
    boolean deleteConversation(Long conversationId, Long userId);
    List<User> getParticipants(Long conversationId);

    // New method
    Conversation getConversationBetweenUsers(Long userId1, Long userId2);
}