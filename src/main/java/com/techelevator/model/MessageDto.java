package com.techelevator.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

public class MessageDto {

    private Long messageId;

    @NotNull(message = "Conversation ID must not be null")
    private Long conversationId;

    @NotNull(message = "Sender ID must not be null")
    private Long senderId;

    // ReceiverId might be optional for group chats, so no @NotNull
    private Long receiverId;

    @NotBlank(message = "Content cannot be blank")
    @Size(max = 1000, message = "Content must be less than or equal to 1000 characters")
    private String content;

    private Long parentMessageId; // For threaded replies, this could be null for top-level messages

    private Date sentAt;  // This might be set by the server, not sent by the client

    // Default constructor for serialization/deserialization
    public MessageDto() {
    }

    // Constructor for creating a new message DTO
    public MessageDto(Long conversationId, Long senderId, Long receiverId, String content, Long parentMessageId) {
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.parentMessageId = parentMessageId;
        // Note: sentAt is not set here as it's typically set by the server
    }

    // Getters and setters for all fields

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
    public String toString() {
        return "MessageDto{" +
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