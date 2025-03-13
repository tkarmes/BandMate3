package com.techelevator.model;

import java.util.Date;
import java.util.List;
import java.util.Objects;

public class Conversation {

    private Long conversationId;
    private Date createdAt;
    private List<Message> messages;
    private List<User> participants; // Added field

    /**
     * Default constructor for frameworks like JPA or when deserializing from JSON.
     */
    public Conversation() {
    }

    /**
     * Constructor for creating a new conversation with initial messages.
     *
     * @param messages List of messages that belong to this conversation.
     */
    public Conversation(List<Message> messages) {
        this.createdAt = new Date(); // Sets the timestamp to now
        this.messages = messages;
    }

    // Getters and Setters
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Conversation that = (Conversation) o;
        return Objects.equals(conversationId, that.conversationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(conversationId);
    }

    @Override
    public String toString() {
        return "Conversation{" +
                "conversationId=" + conversationId +
                ", createdAt=" + createdAt +
                ", messages=" + messages +
                ", participants=" + participants + // Updated toString
                '}';
    }
}