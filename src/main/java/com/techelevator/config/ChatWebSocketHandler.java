package com.techelevator.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techelevator.dao.MessageDao;
import com.techelevator.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {
    private final List<WebSocketSession> sessions = new ArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MessageDao messageDao;

    @Autowired
    public ChatWebSocketHandler(MessageDao messageDao) {
        this.messageDao = messageDao;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        System.out.println("WebSocket connection established: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        Message msg = objectMapper.readValue(payload, Message.class);
        System.out.println("Received: " + msg);

        // Save to DB
        Message savedMsg = messageDao.sendMessage(
                msg.getConversationId(),
                msg.getSenderId(),
                msg.getReceiverId(),
                msg.getContent(),
                null
        );
        System.out.println("Saved to DB: " + savedMsg);

        // Broadcast to all sessions except the sender
        TextMessage response = new TextMessage(objectMapper.writeValueAsString(savedMsg));
        for (WebSocketSession s : sessions) {
            if (s.isOpen() && !s.equals(session)) { // Skip the sender's session
                try {
                    s.sendMessage(response);
                } catch (IOException e) {
                    System.out.println("Failed to send to session " + s.getId() + ": " + e.getMessage());
                }
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        System.out.println("WebSocket connection closed: " + session.getId());
    }
}