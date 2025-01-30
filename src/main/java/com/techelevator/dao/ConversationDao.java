package com.techelevator.dao;

import com.techelevator.model.Conversation;
import com.techelevator.model.Message;
import com.techelevator.model.User;

import java.util.List;

public interface ConversationDao {

    /**
     * Creates a new conversation with the specified participants.
     * @param creator The user who initiates the conversation.
     * @param participants List of users participating in the conversation, including the creator.
     * @return The newly created Conversation object.
     */
    Conversation createConversation(User creator, List<User> participants);

    /**
     * Retrieves all conversations for a user.
     * @param userId The ID of the user whose conversations are to be fetched.
     * @return A list of Conversation objects where the user is a participant.
     */
    List<Conversation> getAllConversationsForUser(Long userId);

    /**
     * Fetches a specific conversation by its ID.
     * @param conversationId The ID of the conversation to retrieve.
     * @return The Conversation object or null if not found.
     */
    Conversation getConversationById(Long conversationId);

    /**
     * Adds a user to an existing conversation.
     * @param conversationId The ID of the conversation to add the user to.
     * @param userId The ID of the user to be added to the conversation.
     * @return true if the user was successfully added, false otherwise (e.g., if already in the conversation).
     */
    boolean addUserToConversation(Long conversationId, Long userId);

    /**
     * Removes a user from a conversation.
     * @param conversationId The ID of the conversation to remove the user from.
     * @param userId The ID of the user to be removed from the conversation.
     * @return true if the user was successfully removed, false otherwise (e.g., if not in the conversation).
     */
    boolean removeUserFromConversation(Long conversationId, Long userId);

    /**
     * Deletes a conversation if the user has permission to do so.
     * @param conversationId The ID of the conversation to delete.
     * @param userId The ID of the user attempting to delete the conversation.
     * @return true if the conversation was deleted, false otherwise (e.g., if permission was denied).
     */
    boolean deleteConversation(Long conversationId, Long userId);
}
