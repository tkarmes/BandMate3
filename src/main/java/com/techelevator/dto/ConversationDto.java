package com.techelevator.dto;

import com.techelevator.model.User;
import com.techelevator.model.Message;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

public class ConversationDto {

    private Long conversationId;

    @NotNull(message = "Creation date must not be null")
    private Date createdAt;

    private List<User> participants;
    private List<Message> messages;

    // Default constructor for serialization/deserialization
    public ConversationDto() {
    }

    // Constructor for creating a new conversation DTO with initial messages and participants
    public ConversationDto(Date createdAt, List<User> participants, List<Message> messages) {
        this.createdAt = createdAt;
        this.participants = participants;
        this.messages = messages;
    }

    // Getters and setters

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public List<User> getParticipants() {
        return participants;
    }

    public void setParticipants(List<User> participants) {
        this.participants = participants;
    }

    @Override
    public String toString() {
        return "ConversationDto{" +
                "conversationId=" + conversationId +
                ", createdAt=" + createdAt +
                ", participants=" + participants +
                ", messages=" + messages +
                '}';
    }
}