package com.techelevator.dao;

import com.techelevator.exception.UserNotFoundException;
import com.techelevator.dto.RegisterUserDto;
import com.techelevator.model.Profile;
import com.techelevator.model.User;
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

    void addInstrumentToUser(Long userId, String instrumentName);

    void saveUserWithProfile(User createdUser);

    void updateUserProfile(Long userId, Profile profile);
}