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

    private List<String> participantNames; // Add this for names
    private List<User> participants;
    private List<Message> messages;

    public ConversationDto() {}

    public ConversationDto(Date createdAt, List<User> participants, List<Message> messages) {
        this.createdAt = createdAt;
        this.participants = participants;
        this.messages = messages;
    }

    // Getters and Setters
    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public List<String> getParticipantNames() { return participantNames; }
    public void setParticipantNames(List<String> participantNames) { this.participantNames = participantNames; }
    public List<User> getParticipants() { return participants; }
    public void setParticipants(List<User> participants) { this.participants = participants; }
    public List<Message> getMessages() { return messages; }
    public void setMessages(List<Message> messages) { this.messages = messages; }

    @Override
    public String toString() {
        return "ConversationDto{" +
                "conversationId=" + conversationId +
                ", createdAt=" + createdAt +
                ", participantNames=" + participantNames +
                ", participants=" + participants +
                ", messages=" + messages +
                '}';
    }
}