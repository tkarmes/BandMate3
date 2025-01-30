package com.techelevator.dao;

import com.techelevator.model.Message;
import com.techelevator.model.Conversation;
import com.techelevator.model.User;

import java.util.List;

public interface MessageDao {

    /**
     * Sends a new message within a conversation, optionally as a reply to another message.
     * @param conversationId The ID of the conversation where the message belongs.
     * @param senderId The ID of the user sending the message.
     * @param receiverId The ID of the user receiving the message (for one-to-one chats; can be null or ignored for group chats).
     * @param content The content of the message.
     * @param parentMessageId The ID of the message this message is replying to, or null if it's a top-level message.
     * @return The newly created Message object.
     */
    Message sendMessage(Long conversationId, Long senderId, Long receiverId, String content, Long parentMessageId);

    /**
     * Retrieves all messages from a specific conversation, ordered by timestamp.
     * @param conversationId The ID of the conversation to fetch messages from.
     * @return A list of Message objects within the conversation, with the oldest messages first.
     */
    List<Message> getMessagesByConversation(Long conversationId);

    /**
     * Marks a message as read by a specific user.
     * @param messageId The ID of the message to mark as read.
     * @param userId The ID of the user who is marking the message as read.
     * @return true if the message was successfully marked as read, false otherwise.
     */
    boolean markMessageAsRead(Long messageId, Long userId);

    /**
     * Deletes a message if the user has permission to do so.
     * @param messageId The ID of the message to delete.
     * @param userId The ID of the user attempting to delete the message.
     * @return true if the message was deleted, false otherwise (e.g., if permission was denied).
     */
    boolean deleteMessage(Long messageId, Long userId);

    /**
     * Updates the content of an existing message.
     * @param messageId The ID of the message to update.
     * @param userId The ID of the user who owns or is allowed to update the message.
     * @param newContent The new content for the message.
     * @return The updated Message object or null if the update failed (e.g., due to permissions).
     */
    Message updateMessage(Long messageId, Long userId, String newContent);
}
