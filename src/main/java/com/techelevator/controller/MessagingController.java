package com.techelevator.controller;

import com.techelevator.dto.ConversationDto;
import com.techelevator.dto.MessageDto;
import com.techelevator.exception.DaoException;
import com.techelevator.exception.UserNotFoundException;
import com.techelevator.model.Conversation;
import com.techelevator.model.Message;
import com.techelevator.model.User;
import com.techelevator.dao.UserDao;
import com.techelevator.dao.MessageDao;
import com.techelevator.dao.ConversationDao;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
@RequestMapping("/messaging")
public class MessagingController {

    private final UserDao userDao;
    private final MessageDao messageDao;
    private final ConversationDao conversationDao;

    public MessagingController(UserDao userDao, MessageDao messageDao, ConversationDao conversationDao) {
        this.userDao = userDao;
        this.messageDao = messageDao;
        this.conversationDao = conversationDao;
    }

    @PostMapping("/conversations")
    public ResponseEntity<Conversation> createConversation(@RequestBody ConversationDto conversationDto, Principal principal) {
        try {
            User sender = userDao.getUserByUsername(principal.getName());
            List<User> participants = conversationDto.getParticipants();

            // Ensure sender is in participants
            if (!participants.stream().anyMatch(p -> p.getUserId().equals(sender.getUserId()))) {
                participants.add(sender);
            }

            Conversation newConversation = conversationDao.createConversation(sender, participants);
            return new ResponseEntity<>(newConversation, HttpStatus.CREATED);
        } catch (UserNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create conversation");
        }
    }

    @PostMapping("/messages")
    public ResponseEntity<Message> sendMessage(@RequestBody MessageDto messageDto, Principal principal) {
        System.out.println("Received message from: " + principal.getName() + ", DTO: " + messageDto);
        try {
            User sender = userDao.getUserByUsername(principal.getName());
            Message message = messageDao.sendMessage(messageDto.getConversationId(), sender.getUserId(),
                    messageDto.getReceiverId(), messageDto.getContent(),
                    messageDto.getParentMessageId());
            System.out.println("Message saved: " + message);
            return new ResponseEntity<>(message, HttpStatus.CREATED);
        } catch (UserNotFoundException e) {
            System.out.println("User not found: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to send message");
        }
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<List<Message>> getMessagesForConversation(@PathVariable Long conversationId, Principal principal) {
        try {
            List<Message> messages = messageDao.getMessagesByConversation(conversationId);
            return new ResponseEntity<>(messages, HttpStatus.OK);
        } catch (DaoException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found");
        }
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationDto>> getUserConversations(Principal principal) {
        try {
            User user = userDao.getUserByUsername(principal.getName());
            List<Conversation> conversations = conversationDao.getAllConversationsForUser(user.getUserId());
            List<ConversationDto> conversationDtos = conversations.stream().map(conv -> {
                ConversationDto dto = new ConversationDto();
                dto.setConversationId(conv.getConversationId());
                dto.setCreatedAt(conv.getCreatedAt());
                List<String> participantUsernames = conversationDao.getParticipants(conv.getConversationId())
                        .stream()
                        .filter(u -> !u.getUserId().equals(user.getUserId()))
                        .map(User::getUsername)
                        .collect(Collectors.toList());
                dto.setParticipantNames(participantUsernames);
                return dto;
            }).collect(Collectors.toList());
            return new ResponseEntity<>(conversationDtos, HttpStatus.OK);
        } catch (UserNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
    }
}