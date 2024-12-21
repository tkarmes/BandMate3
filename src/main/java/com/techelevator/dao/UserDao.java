package com.techelevator.dao;

import com.techelevator.exception.UserNotFoundException;
import com.techelevator.model.RegisterUserDto;
import com.techelevator.model.User;

import java.util.List;

public interface UserDao {

    List<User> getUsers();

    User getUserById(Long id) throws UserNotFoundException;

    User getUserByUsername(String username) throws UserNotFoundException;

    User createUser(RegisterUserDto user) throws UserCreationException;

    void deleteUserById(Long userId, Long actingUserId) throws UserNotFoundException, UserDeletionException;

    void deleteUserByUsername(String username, Long actingUserId) throws UserNotFoundException, UserDeletionException;
}