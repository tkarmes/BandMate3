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
import com.techelevator.dao.MusicianProfileDao;
import com.techelevator.dao.VenueProfileDao;
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
    private final MusicianProfileDao musicianProfileDao;
    private final VenueProfileDao venueProfileDao;

    public AuthenticationController(TokenProvider tokenProvider, AuthenticationManagerBuilder authenticationManagerBuilder, UserDao userDao, MessageDao messageDao, ConversationDao conversationDao, MusicianProfileDao musicianProfileDao, VenueProfileDao venueProfileDao) {
        this.tokenProvider = tokenProvider;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.userDao = userDao;
        this.messageDao = messageDao;
        this.conversationDao = conversationDao;
        this.musicianProfileDao = musicianProfileDao;
        this.venueProfileDao = venueProfileDao;
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
    public ResponseEntity<?> register(@Valid @RequestBody RegisterUserDto newUser) {
        try {
            if (userDao.getUserByUsername(newUser.getUsername()) != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User already exists.");
            }
            if (!newUser.getPassword().equals(newUser.getConfirmPassword())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match.");
            }

            // Create the user
            User createdUser = userDao.createUser(newUser);

            // Create profile based on user type
            if (createdUser.getUserType() == User.UserType.Musician) {
                musicianProfileDao.createMusicianProfile(createdUser.getUserId());
                return ResponseEntity.status(HttpStatus.CREATED).body(new MusicianProfileDto(null));
            } else if (createdUser.getUserType() == User.UserType.VenueOwner) {
                // Check if venue name is provided
                if (newUser.getVenueName() == null || newUser.getVenueName().isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Venue name is required for venue owners.");
                }
                venueProfileDao.createVenueProfile(createdUser.getUserId(), newUser.getVenueName()); // Here we pass the venueName
                return ResponseEntity.status(HttpStatus.CREATED).body(new VenueProfileDto(null));
            } else {
                // For admin or other types if needed
                return ResponseEntity.status(HttpStatus.CREATED).body(UserDto.fromUser(createdUser));
            }
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
    public ResponseEntity<?> uploadProfilePicture(@PathVariable Long userId, @RequestParam("file") MultipartFile file, Principal principal) {
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

            if (user.getUserType() == User.UserType.Musician) {
                MusicianProfile profile = musicianProfileDao.getMusicianProfileByUserId(userId);
                profile.setProfilePictureUrl(fileName);
                musicianProfileDao.updateMusicianProfile(userId, profile);
                return ResponseEntity.ok(MusicianProfileDto.fromEntity(profile));
            } else if (user.getUserType() == User.UserType.VenueOwner) {
                VenueProfile profile = venueProfileDao.getVenueProfileByUserId(userId);
                profile.setProfilePictureUrl(fileName);
                venueProfileDao.updateVenueProfile(userId, profile);
                return ResponseEntity.ok(VenueProfileDto.fromEntity(profile));
            } else {
                return ResponseEntity.ok(UserDto.fromUser(updatedUser));
            }
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

            MusicianProfile profile = musicianProfileDao.getMusicianProfileByUserId(userId);
            String currentInstruments = profile.getInstruments();
            String newInstruments = currentInstruments.isEmpty() ? instrumentName : currentInstruments + ", " + instrumentName;
            profile.setInstruments(newInstruments);
            musicianProfileDao.updateMusicianProfile(userId, profile);

            return ResponseEntity.ok().build();
        } catch (UserNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
    }

    @PostMapping("/users/{userId}/genres")
    public ResponseEntity<Void> addGenreToMusicianProfile(
            @PathVariable Long userId,
            @RequestBody GenreDto genreDto,  // Change here to use a DTO
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

            MusicianProfile profile = musicianProfileDao.getMusicianProfileByUserId(userId);
            String currentGenres = profile.getGenres();
            String newGenres = (currentGenres == null || currentGenres.isEmpty()) ? genreDto.getGenreName() : currentGenres + ", " + genreDto.getGenreName();
            profile.setGenres(newGenres);
            musicianProfileDao.updateMusicianProfile(userId, profile);

            return ResponseEntity.ok().build();
        } catch (UserNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
    }
    // New endpoints for profile management

    @GetMapping("/users/{userId}/musician-profile")
    public ResponseEntity<MusicianProfileDto> getMusicianProfile(@PathVariable Long userId, Principal principal) {
        try {
            User user = userDao.getUserById(userId);
            if (user == null || user.getUserType() != User.UserType.Musician) {
                return ResponseEntity.notFound().build();
            }

            if (!principal.getName().equals(user.getUsername())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            MusicianProfile profile = musicianProfileDao.getMusicianProfileByUserId(userId);
            return ResponseEntity.ok(MusicianProfileDto.fromEntity(profile));
        } catch (UserNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User or profile not found");
        }
    }

    @PutMapping("/users/{userId}/musician-profile")
    public ResponseEntity<Void> updateMusicianProfile(@PathVariable Long userId, @RequestBody MusicianProfileDto profileDto, Principal principal) {
        try {
            User user = userDao.getUserById(userId);
            if (user == null || user.getUserType() != User.UserType.Musician) {
                return ResponseEntity.notFound().build();
            }

            if (!principal.getName().equals(user.getUsername())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            MusicianProfile profile = musicianProfileDao.getMusicianProfileByUserId(userId);
            profile.setBio(profileDto.getBio());
            profile.setLocation(profileDto.getLocation());
            profile.setGenres(profileDto.getGenres());
            profile.setInstruments(profileDto.getInstruments());
            profile.setProfilePictureUrl(profileDto.getProfilePictureUrl());
            musicianProfileDao.updateMusicianProfile(userId, profile);

            return ResponseEntity.ok().build();
        } catch (UserNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User or profile not found");
        }
    }

    @GetMapping("/users/{userId}/venue-profile")
    public ResponseEntity<VenueProfileDto> getVenueProfile(@PathVariable Long userId, Principal principal) {
        try {
            User user = userDao.getUserById(userId);
            if (user == null || user.getUserType() != User.UserType.VenueOwner) {
                return ResponseEntity.notFound().build();
            }

            if (!principal.getName().equals(user.getUsername())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            VenueProfile profile = venueProfileDao.getVenueProfileByUserId(userId);
            return ResponseEntity.ok(VenueProfileDto.fromEntity(profile));
        } catch (UserNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User or profile not found");
        }
    }

    @PutMapping("/users/{userId}/venue-profile")
    public ResponseEntity<Void> updateVenueProfile(@PathVariable Long userId, @RequestBody VenueProfileDto profileDto, Principal principal) {
        try {
            User user = userDao.getUserById(userId);
            if (user == null || user.getUserType() != User.UserType.VenueOwner) {
                return ResponseEntity.notFound().build();
            }

            if (!principal.getName().equals(user.getUsername())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            VenueProfile profile = new VenueProfile();
            profile.setVenueProfileId(userId); // Assuming this is the profile ID
            profile.setName(profileDto.getVenueName());
            profile.setAddress(profileDto.getAddress());
            profile.setCity(profileDto.getCity());
            profile.setState(profileDto.getState());
            profile.setZipCode(profileDto.getZipCode());
            profile.setCapacity(profileDto.getCapacity());
            profile.setVenueType(profileDto.getVenueType());
            profile.setGenrePreferences(profileDto.getGenrePreferences());
            profile.setPhone(profileDto.getPhone());
            profile.setEmail(profileDto.getEmail());
            profile.setWebsiteUrl(profileDto.getWebsiteUrl());
            profile.setOperatingHours(profileDto.getOperatingHours());
            profile.setAmenities(profileDto.getAmenities());
            profile.setProfilePictureUrl(profileDto.getProfilePictureUrl());
            venueProfileDao.updateVenueProfile(userId, profile);

            return ResponseEntity.ok().build();
        } catch (UserNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User or profile not found");
        }
    }
}