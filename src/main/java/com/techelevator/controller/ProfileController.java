package com.techelevator.controller;

import com.techelevator.dto.*;
import com.techelevator.exception.UserNotFoundException;
import com.techelevator.exception.UserDeletionException;
import com.techelevator.model.*;
import com.techelevator.dao.UserDao;
import com.techelevator.dao.MusicianProfileDao;
import com.techelevator.dao.VenueProfileDao;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.UUID;

@RestController
@CrossOrigin
@RequestMapping("/users")
public class ProfileController {

    private final UserDao userDao;
    private final MusicianProfileDao musicianProfileDao;
    private final VenueProfileDao venueProfileDao;

    public ProfileController(UserDao userDao, MusicianProfileDao musicianProfileDao, VenueProfileDao venueProfileDao) {
        this.userDao = userDao;
        this.musicianProfileDao = musicianProfileDao;
        this.venueProfileDao = venueProfileDao;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long userId, Principal principal) {
        try {
            User user = userDao.getUserById(userId);
            if (user == null) return ResponseEntity.notFound().build();
            UserDto dto = UserDto.fromUser(user);
            if (user.getUserType() == User.UserType.Musician) {
                MusicianProfile profile = musicianProfileDao.getMusicianProfileByUserId(userId);
                if (profile != null) dto.setProfilePictureUrl(profile.getProfilePictureUrl());
            } else if (user.getUserType() == User.UserType.VenueOwner) {
                VenueProfile profile = venueProfileDao.getVenueProfileByUserId(userId);
                if (profile != null) dto.setProfilePictureUrl(profile.getProfilePictureUrl());
            }
            return ResponseEntity.ok(dto);
        } catch (UserNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
    }

    @DeleteMapping("/{username}")
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

    @GetMapping("/{userId}/musician-profile")
    public ResponseEntity<MusicianProfileDto> getMusicianProfile(@PathVariable Long userId, Principal principal) {
        try {
            User user = userDao.getUserById(userId);
            if (user == null || user.getUserType() != User.UserType.Musician) return ResponseEntity.notFound().build();
            if (!principal.getName().equals(user.getUsername())) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            MusicianProfile profile = musicianProfileDao.getMusicianProfileByUserId(userId);
            return ResponseEntity.ok(MusicianProfileDto.fromEntity(profile));
        } catch (UserNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User or profile not found");
        }
    }

    @PutMapping(value = "/{userId}/musician-profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MusicianProfile> updateMusicianProfile(
            @PathVariable Long userId,
            @RequestParam(value = "profilePicture", required = false) MultipartFile profilePicture,
            @RequestParam(value = "bio", required = false) String bio,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "genres", required = false) String genres,
            @RequestParam(value = "instruments", required = false) String instruments,
            @RequestParam(value = "profilePictureUrl", required = false) String profilePictureUrl,
            Principal principal
    ) {
        try {
            User user = userDao.getUserById(userId);
            if (user == null || user.getUserType() != User.UserType.Musician) return ResponseEntity.notFound().build();
            if (!principal.getName().equals(user.getUsername())) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

            MusicianProfile profile = musicianProfileDao.getMusicianProfileByUserId(userId);
            profile.setBio(bio != null ? bio : profile.getBio());
            profile.setLocation(location != null ? location : profile.getLocation());
            profile.setGenres(genres != null ? genres : profile.getGenres());
            profile.setInstruments(instruments != null ? instruments : profile.getInstruments());

            if (profilePicture != null && !profilePicture.isEmpty()) {
                String uploadDir = "C:/workspace/capstone/java/uploads/"; // Absolute path
                String fileName = UUID.randomUUID().toString() + "-" + profilePicture.getOriginalFilename();
                Path filePath = Paths.get(uploadDir, fileName);
                System.out.println("Saving picture to: " + filePath.toAbsolutePath());
                try {
                    Files.createDirectories(filePath.getParent());
                    profilePicture.transferTo(filePath.toFile());
                    System.out.println("File saved successfully: " + fileName);
                    profile.setProfilePictureUrl(fileName);
                } catch (IOException e) {
                    System.out.println("IOException during file save: " + e.getMessage());
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload picture", e);
                }
            } else if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
                profile.setProfilePictureUrl(profilePictureUrl);
            }

            musicianProfileDao.updateMusicianProfile(userId, profile);
            return ResponseEntity.ok(profile);
        } catch (UserNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User or profile not found", e);
        }
    }

    @GetMapping("/{userId}/venue-profile")
    public ResponseEntity<VenueProfileDto> getVenueProfile(@PathVariable Long userId, Principal principal) {
        try {
            User user = userDao.getUserById(userId);
            if (user == null || user.getUserType() != User.UserType.VenueOwner) return ResponseEntity.notFound().build();
            if (!principal.getName().equals(user.getUsername())) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            VenueProfile profile = venueProfileDao.getVenueProfileByUserId(userId);
            return ResponseEntity.ok(VenueProfileDto.fromEntity(profile));
        } catch (UserNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User or profile not found");
        }
    }

    @PutMapping("/{userId}/venue-profile")
    public ResponseEntity<Void> updateVenueProfile(@PathVariable Long userId, @RequestBody VenueProfileDto profileDto, Principal principal) {
        try {
            User user = userDao.getUserById(userId);
            if (user == null || user.getUserType() != User.UserType.VenueOwner) return ResponseEntity.notFound().build();
            if (!principal.getName().equals(user.getUsername())) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

            VenueProfile profile = new VenueProfile();
            profile.setVenueProfileId(userId);
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

    @GetMapping("/uploads/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            Path filePath = Paths.get("./uploads").resolve(filename);
            System.out.println("Looking for file at: " + filePath.toAbsolutePath());
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                System.out.println("File found and readable: " + filename);
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(resource);
            }
            System.out.println("File not found or not readable: " + filename);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.out.println("Error serving file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}