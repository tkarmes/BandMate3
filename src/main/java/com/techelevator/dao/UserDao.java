package com.techelevator.dao;

import com.techelevator.exception.UserNotFoundException;
import com.techelevator.dto.RegisterUserDto;
import com.techelevator.model.User;
import com.techelevator.exception.UserDeletionException;
import com.techelevator.exception.UserCreationException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserDao {

    List getUsers();

    User getUserById(Long id) throws UserNotFoundException;

    User getUserByUsername(String username) throws UserNotFoundException;

    User createUser(RegisterUserDto user) throws UserCreationException;

    void deleteUserById(Long userId, Long actingUserId) throws UserNotFoundException, UserDeletionException;

    void deleteUserByUsername(String username, Long actingUserId) throws UserNotFoundException, UserDeletionException;

    // New method for uploading profile picture
    String uploadProfilePicture(Long userId, MultipartFile file) throws UserNotFoundException;
}