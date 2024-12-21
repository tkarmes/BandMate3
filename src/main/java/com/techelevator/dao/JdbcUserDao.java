package com.techelevator.dao;

import com.techelevator.exception.DaoException;
import com.techelevator.exception.UserNotFoundException;
import com.techelevator.exception.UserDeletionException;
import com.techelevator.model.RegisterUserDto;
import com.techelevator.model.User;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcUserDao implements UserDao {

    private final JdbcTemplate jdbcTemplate;

    public JdbcUserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User getUserById(Long userId) {
        User user = null;
        String sql = "SELECT user_id, username, email, password_hash, user_type, created_at, role FROM users WHERE user_id = ?";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId);
            if (results.next()) {
                user = mapRowToUser(results);
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
        return user;
    }

    @Override
    public List<User> getUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id, username, email, password_hash, user_type, created_at, role FROM users";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql);
            while (results.next()) {
                User user = mapRowToUser(results);
                users.add(user);
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
        return users;
    }

    @Override
    public User getUserByUsername(String username) {
        if (username == null) throw new IllegalArgumentException("Username cannot be null");
        User user = null;
        String sql = "SELECT user_id, username, email, password_hash, user_type, created_at, role FROM users WHERE username = LOWER(TRIM(?));";
        try {
            SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, username);
            if (rowSet.next()) {
                user = mapRowToUser(rowSet);
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
        return user;
    }

    @Override
    public User createUser(RegisterUserDto user) {
        User newUser = null;
        String insertUserSql = "INSERT INTO users (username, password_hash, user_type, role) values (LOWER(TRIM(?)), ?, ?, ?) RETURNING user_id";
        String password_hash = new BCryptPasswordEncoder().encode(user.getPassword());
        String ssRole = user.getRole().toUpperCase().startsWith("ROLE_") ? user.getRole().toUpperCase() : "ROLE_" + user.getRole().toUpperCase();
        User.UserType userType = user.getRole().equalsIgnoreCase("Musician") ? User.UserType.Musician : User.UserType.VenueOwner;
        try {
            Long newUserId = jdbcTemplate.queryForObject(insertUserSql, Long.class, user.getUsername(), password_hash, userType.toString(), ssRole);
            newUser = getUserById(newUserId);
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        }
        return newUser;

        @Override
        public void deleteUserById(Long userId, Long actingUserId) throws UserNotFoundException, UserDeletionException {
            // Retrieve the user to be deleted
            User userToDelete = getUserById(userId);
            if (userToDelete == null) {
                throw new UserNotFoundException("User with ID " + userId + " not found");
            }

            // Check if the acting user has permission to delete this user
            if (!canDeleteUser(userToDelete, actingUserId)) {
                throw new UserDeletionException("User does not have permission to delete this profile");
            }

            String sql = "DELETE FROM users WHERE user_id = ?";
            try {
                int rowsAffected = jdbcTemplate.update(sql, userId);
                if (rowsAffected == 0) {
                    throw new UserNotFoundException("User not found or already deleted");
                }
            } catch (CannotGetJdbcConnectionException e) {
                throw new DaoException("Unable to connect to server or database", e);
            } catch (DataIntegrityViolationException e) {
                throw new UserDeletionException("Cannot delete user due to database integrity constraints", e);
            }
        }

        @Override
        public void deleteUserByUsername(String username, Long actingUserId) throws UserNotFoundException, UserDeletionException {
            // Retrieve the user to be deleted
            User userToDelete = getUserByUsername(username);
            if (userToDelete == null) {
                throw new UserNotFoundException("User with username " + username + " not found");
            }

            // Use the already implemented deleteUserById method
            deleteUserById(userToDelete.getUserId(), actingUserId);
        }

        // Helper method to check if the acting user can delete the target user
        private boolean canDeleteUser(User userToDelete, Long actingUserId) {
            // User can delete their own account or an admin can delete any account
            return actingUserId.equals(userToDelete.getUserId()) || isAdmin(actingUserId);
        }

        // Method to check if the user is an admin
        private boolean isAdmin(Long userId) {
            String sql = "SELECT COUNT(*) FROM users WHERE user_id = ? AND user_type = 'Admin'";
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId);
            return results.next() && results.getInt(1) > 0;
        }
    }

    private User mapRowToUser(SqlRowSet rs) {
        User user = new User();
        user.setUserId(rs.getLong("user_id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setUserType(User.UserType.valueOf(rs.getString("user_type")));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        user.setAuthorities(rs.getString("role")); // Assuming 'role' is how you store authorities in string format
        return user;
    }
}