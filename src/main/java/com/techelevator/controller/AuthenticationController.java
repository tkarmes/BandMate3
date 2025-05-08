package com.techelevator.controller;

import javax.validation.Valid;

import com.techelevator.dto.*;
import com.techelevator.exception.DaoException;
import com.techelevator.exception.UserCreationException;
import com.techelevator.exception.UserNotFoundException;
import com.techelevator.model.MusicianProfile;
import com.techelevator.model.User;
import com.techelevator.dao.UserDao;
import com.techelevator.dao.MusicianProfileDao;
import com.techelevator.dao.VenueProfileDao;
import com.techelevator.model.VenueProfile;
import com.techelevator.security.jwt.JWTFilter;
import com.techelevator.security.jwt.TokenProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@CrossOrigin(origins = "http://localhost:3000") // Updated for Vite port
@RequestMapping("/auth")
public class AuthenticationController {

    private final TokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final UserDao userDao;
    private final MusicianProfileDao musicianProfileDao;
    private final VenueProfileDao venueProfileDao;

    public AuthenticationController(TokenProvider tokenProvider, AuthenticationManagerBuilder authenticationManagerBuilder,
                                    UserDao userDao, MusicianProfileDao musicianProfileDao, VenueProfileDao venueProfileDao) {
        this.tokenProvider = tokenProvider;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.userDao = userDao;
        this.musicianProfileDao = musicianProfileDao;
        this.venueProfileDao = venueProfileDao;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginDto loginDto) {
        try {
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword());
            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.createToken(authentication, false);

            User user = userDao.getUserByUsername(loginDto.getUsername());
            if (user == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Username or password is incorrect.");
            }

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add(JWTFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);
            LoginResponseDto responseDto = new LoginResponseDto(jwt, user); // Reverted to User
            return new ResponseEntity<>(responseDto, httpHeaders, HttpStatus.OK);
        } catch (UserNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Username or password is incorrect.");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Login failed due to server error.", e);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterUserDto newUser) {
        try {
            if (userDao.getUserByUsername(newUser.getUsername()) != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User already exists.");
            }
            if (!newUser.getPassword().equals(newUser.getConfirmPassword())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match.");
            }

            User createdUser = userDao.createUser(newUser);
            if (createdUser.getUserType() == User.UserType.Musician) {
                MusicianProfile createdProfile = musicianProfileDao.createMusicianProfile(createdUser.getUserId());
                if (newUser.getMusicianName() != null && !newUser.getMusicianName().isEmpty()) {
                    createdProfile.setName(newUser.getMusicianName());
                    musicianProfileDao.updateMusicianProfile(createdUser.getUserId(), createdProfile);
                }
                return ResponseEntity.status(HttpStatus.CREATED).body(MusicianProfileDto.fromEntity(createdProfile));
            } else if (createdUser.getUserType() == User.UserType.VenueOwner) {
                if (newUser.getVenueName() == null || newUser.getVenueName().isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Venue name is required for venue owners.");
                }
                VenueProfile createdProfile = venueProfileDao.createVenueProfile(createdUser.getUserId(), newUser.getVenueName());
                return ResponseEntity.status(HttpStatus.CREATED).body(VenueProfileDto.fromEntity(createdProfile));
            } else {
                return ResponseEntity.status(HttpStatus.CREATED).body(UserDto.fromUser(createdUser));
            }
        } catch (DaoException | UserCreationException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "User registration failed.", e);
        }
    }
}