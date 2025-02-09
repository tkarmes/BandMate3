package com.techelevator.controller;

import javax.validation.Valid;

import com.techelevator.dto.*;
import com.techelevator.exception.DaoException;
import com.techelevator.exception.UserNotFoundException;
import com.techelevator.exception.UserDeletionException;
import com.techelevator.exception.UserCreationException;
import com.techelevator.model.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.techelevator.dao.UserDao;
import com.techelevator.security.jwt.JWTFilter;
import com.techelevator.security.jwt.TokenProvider;

import com.techelevator.dao.MessageDao;
import com.techelevator.dao.ConversationDao;

import java.security.Principal;
import java.util.List;

@RestController
@CrossOrigin
public class AuthenticationController {

    private final TokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final UserDao userDao;
    private final MessageDao messageDao;
    private final ConversationDao conversationDao;

    public AuthenticationController(TokenProvider tokenProvider, AuthenticationManagerBuilder authenticationManagerBuilder, UserDao userDao, MessageDao messageDao, ConversationDao conversationDao) {
        this.tokenProvider = tokenProvider;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.userDao = userDao;
        this.messageDao = messageDao;
        this.conversationDao = conversationDao;
    }

    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginDto loginDto) {

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword());

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.createToken(authentication, false);

        User user;
        try {
            user = userDao.getUserByUsername(loginDto.getUsername());
        } catch (UserNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Username or password is incorrect.");
        }

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(JWTFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);
        return new ResponseEntity<>(new LoginResponseDto(jwt, user), httpHeaders, HttpStatus.OK);
    }

    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterUserDto newUser) {
        try {
            if (userDao.getUserByUsername(newUser.getUsername()) != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User already exists.");
            }
            if (!newUser.getPassword().equals(newUser.getConfirmPassword())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match.");
            }

            // Create the user
            User createdUser = userDao.createUser(newUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(UserDto.fromUser(createdUser));
        } catch (DaoException | UserCreationException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "User registration failed.");
        }
    }

    @DeleteMapping("/users/{username}")
    public ResponseEntity<Void> deleteUserByUsername(@PathVariable String username, Principal principal) {
        try {
            User actingUser = userDao.getUserByUsername(principal.getName());
            Long actingUserId = actingUser.getUserId();
            userDao.deleteUserByUsername(username, actingUserId);

            return ResponseEntity.noContent().build();
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (UserDeletionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PostMapping("/conversations")
    public ResponseEntity<Conversation> createConversation(@RequestBody ConversationDto conversationDto, Principal principal) {
        try {
            User sender = userDao.getUserByUsername(principal.getName());
            List<User> participants = conversationDto.getParticipants(); // Assuming you pass participants in the DTO
            Conversation newConversation = conversationDao.createConversation(sender, participants);
            return new ResponseEntity<>(newConversation, HttpStatus.CREATED);
        } catch (UserNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create conversation");
        }
    }

    @PostMapping("/messages")
    public ResponseEntity<Message> sendMessage(@RequestBody MessageDto messageDto, Principal principal) {
        try {
            User sender = userDao.getUserByUsername(principal.getName());
            Message message = messageDao.sendMessage(messageDto.getConversationId(), sender.getUserId(), messageDto.getReceiverId(), messageDto.getContent(), messageDto.getParentMessageId());
            return new ResponseEntity<>(message, HttpStatus.CREATED);
        } catch (UserNotFoundException e) {
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

    @PostMapping("/users/{userId}/profile-picture")
    public ResponseEntity<UserDto> uploadProfilePicture(@PathVariable Long userId, @RequestParam("file") MultipartFile file, Principal principal) {
        if (file.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            User user = userDao.getUserById(userId);
            if (user == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            if (!principal.getName().equals(user.getUsername())) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }

            String fileName = userDao.uploadProfilePicture(userId, file);
            User updatedUser = userDao.getUserById(userId); // Refresh user object to get updated profile info if needed
            return ResponseEntity.ok(UserDto.fromUser(updatedUser));
        } catch (UserNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
    }

    @PostMapping("/users/{userId}/instruments")
    public ResponseEntity<Void> addInstrumentToUser(
            @PathVariable Long userId,
            @RequestBody String instrumentName,
            Principal principal) {
        try {
            User user = userDao.getUserById(userId);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            if (!principal.getName().equals(user.getUsername())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            if (user.getUserType() != User.UserType.Musician) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            userDao.addInstrumentToMusician(userId, instrumentName);
            return ResponseEntity.ok().build();
        } catch (UserNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
    }
}