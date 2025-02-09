package com.techelevator.dao;

import com.techelevator.exception.UserNotFoundException;
import com.techelevator.dto.RegisterUserDto;
import com.techelevator.model.MusicianProfile;
import com.techelevator.model.User;
import com.techelevator.model.VenueProfile;
import com.techelevator.exception.UserDeletionException;
import com.techelevator.exception.UserCreationException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserDao {

    List<User> getUsers();

    User getUserById(Long id) throws UserNotFoundException;

    User getUserByUsername(String username) throws UserNotFoundException;

    User createUser(RegisterUserDto user) throws UserCreationException;

    void deleteUserById(Long userId, Long actingUserId) throws UserNotFoundException, UserDeletionException;

    void deleteUserByUsername(String username, Long actingUserId) throws UserNotFoundException, UserDeletionException;

    String uploadProfilePicture(Long userId, MultipartFile file) throws UserNotFoundException;

    // Methods specific to musicians
    void addInstrumentToMusician(Long userId, String instrumentName);

    MusicianProfile getMusicianProfileByUserId(Long userId);

    void saveMusicianProfile(Long userId, MusicianProfile profile);

    void updateMusicianProfile(Long userId, MusicianProfile profile);

    // Methods specific to venues
    void addGenrePreferenceToVenue(Long userId, String genrePreference);

    VenueProfile getVenueProfileByUserId(Long userId);

    void saveVenueProfile(Long userId, VenueProfile profile);

    void updateVenueProfile(Long userId, VenueProfile profile);

    // Note: Removed the general Profile methods since we now have specific types
}