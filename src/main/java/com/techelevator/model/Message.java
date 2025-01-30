package com.techelevator.model;

import java.util.Date;
import java.util.Objects;

public class Message {

    private Long messageId;
    private Long conversationId;
    private Long senderId;
    private Long receiverId; // This might be null for group conversations
    private String content;
    private Long parentMessageId; // For threaded replies, this could be null for top-level messages
    private Date sentAt;

    /**
     * Default constructor for frameworks like JPA or when deserializing from JSON.
     */
    public Message() {
    }

    /**
     * Constructor for creating a new message.
     *
     * @param conversationId The ID of the conversation this message belongs to.
     * @param senderId The ID of the user sending the message.
     * @param receiverId The ID of the user receiving the message (can be null for group chats).
     * @param content The content of the message.
     * @param parentMessageId The ID of the message this message is replying to, or null if it's a top-level message.
     */
    public Message(Long conversationId, Long senderId, Long receiverId, String content, Long parentMessageId) {
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.parentMessageId = parentMessageId;
        this.sentAt = new Date(); // Sets the timestamp to now
    }

    // Getters and Setters
    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getParentMessageId() {
        return parentMessageId;
    }

    public void setParentMessageId(Long parentMessageId) {
        this.parentMessageId = parentMessageId;
    }

    public Date getSentAt() {
        return sentAt;
    }

    public void setSentAt(Date sentAt) {
        this.sentAt = sentAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return Objects.equals(messageId, message.messageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageId);
    }

    @Override
    public String toString() {
        return "Message{" +
                "messageId=" + messageId +
                ", conversationId=" + conversationId +
                ", senderId=" + senderId +
                ", receiverId=" + receiverId +
                ", content='" + content + '\'' +
                ", parentMessageId=" + parentMessageId +
                ", sentAt=" + sentAt +
                '}';
    }
}