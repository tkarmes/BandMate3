package com.techelevator.dao;

import com.techelevator.exception.DaoException;
import com.techelevator.exception.UserCreationException;
import com.techelevator.exception.UserNotFoundException;
import com.techelevator.exception.UserDeletionException;
import com.techelevator.model.RegisterUserDto;
import com.techelevator.model.User;
import com.techelevator.model.Authority;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
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
        String sql = "SELECT user_id, username, email, password_hash, user_type, created_at FROM users WHERE user_id = ?";
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
        String sql = "SELECT user_id, username, email, password_hash, user_type, created_at FROM users";
        try {
            return jdbcTemplate.query(sql, new UserRowMapper());
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
    }



    @Override
    public User getUserByUsername(String username) {
        if (username == null) throw new IllegalArgumentException("Username cannot be null");
        User user = null;
        String sql = "SELECT user_id, username, email, password_hash, user_type, created_at FROM users WHERE username = LOWER(TRIM(?));";
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
    public User createUser(RegisterUserDto user) throws UserCreationException {
        String insertUserSql = "INSERT INTO users (username, password_hash, user_type, email) values (LOWER(TRIM(?)), ?, ?, ?) RETURNING user_id";
        String password_hash = new BCryptPasswordEncoder().encode(user.getPassword());
        User.UserType userType = User.UserType.valueOf(user.getUserType());
        try {
            Long newUserId = jdbcTemplate.queryForObject(insertUserSql, Long.class, user.getUsername(), password_hash, userType.toString(), user.getEmail());

            // Insert into user_authorities table
            String insertAuthoritySql = "INSERT INTO user_authorities (user_id, authority_name) VALUES (?, ?)";
            String role = user.getRole().toUpperCase().startsWith("ROLE_") ? user.getRole().toUpperCase() : "ROLE_" + user.getRole().toUpperCase();
            jdbcTemplate.update(insertAuthoritySql, newUserId, role);

            return getUserById(newUserId); // Retrieve the newly created user
        } catch (DataIntegrityViolationException e) {
            throw new UserCreationException("User creation failed due to data integrity violation", e);
        } catch (Exception e) {
            throw new UserCreationException("Failed to create user", e);
        }
    }

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

        String sql = "DELETE FROM user_authorities WHERE user_id = ?";
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

        sql = "DELETE FROM users WHERE user_id = ?";
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
        String sql = "SELECT COUNT(*) FROM user_authorities WHERE user_id = ? AND authority_name = 'ROLE_ADMIN'";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId);
        return results.next() && results.getInt(1) > 0;
    }

    private User mapRowToUser(SqlRowSet rs) {
        User user = new User();
        user.setUserId(rs.getLong("user_id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setUserType(User.UserType.valueOf(rs.getString("user_type")));
        user.setCreatedAt(rs.getTimestamp("created_at"));

        // Fetch authorities from the user_authorities table
        String sqlAuthorities = "SELECT authority_name FROM user_authorities WHERE user_id = ?";
        List<String> roles = jdbcTemplate.queryForList(sqlAuthorities, String.class, user.getUserId());
        for (String role : roles) {
            user.getAuthorities().add(new Authority(role));
        }

        return user;
    }

    // Custom RowMapper for User
    private class UserRowMapper implements RowMapper<User> {
        @Override
        public User mapRow(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
            return mapRowToUser((SqlRowSet) rs);
        }
    }


}