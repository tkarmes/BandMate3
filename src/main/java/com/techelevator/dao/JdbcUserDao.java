package com.techelevator.dao;

import com.techelevator.exception.DaoException;
import com.techelevator.exception.UserCreationException;
import com.techelevator.exception.UserNotFoundException;
import com.techelevator.exception.UserDeletionException;
import com.techelevator.dto.RegisterUserDto;
import com.techelevator.model.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.lang.Nullable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Component
public class JdbcUserDao implements UserDao {

    private final JdbcTemplate jdbcTemplate;
    private final MusicianProfileDao musicianProfileDao;

    public JdbcUserDao(JdbcTemplate jdbcTemplate, MusicianProfileDao musicianProfileDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.musicianProfileDao = musicianProfileDao;
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
        String insertUserSql = "INSERT INTO users (username, password_hash, user_type, email) VALUES (LOWER(TRIM(?)), ?, ?, ?) RETURNING user_id";
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
        User userToDelete = getUserById(userId);
        deleteUser(userToDelete, actingUserId);
    }

    @Override
    public void deleteUserByUsername(String username, Long actingUserId) throws UserNotFoundException, UserDeletionException {
        User userToDelete = getUserByUsername(username);
        deleteUser(userToDelete, actingUserId);
    }

    private void deleteUser(User userToDelete, Long actingUserId) throws UserNotFoundException, UserDeletionException {
        if (userToDelete == null) {
            throw new UserNotFoundException("User not found");
        }

        if (!canDeleteUser(userToDelete, actingUserId)) {
            throw new UserDeletionException("User does not have permission to delete this profile");
        }

        // Now delete from user_authorities
        String sql = "DELETE FROM user_authorities WHERE user_id = ?";
        try {
            int rowsAffected = jdbcTemplate.update(sql, userToDelete.getUserId());
            if (rowsAffected == 0) {
                throw new UserNotFoundException("User not found or already deleted");
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new UserDeletionException("Cannot delete user due to database integrity constraints", e);
        }

        // Finally, delete the user
        sql = "DELETE FROM users WHERE user_id = ?";
        try {
            int rowsAffected = jdbcTemplate.update(sql, userToDelete.getUserId());
            if (rowsAffected == 0) {
                throw new UserNotFoundException("User not found or already deleted");
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new UserDeletionException("Cannot delete user due to database integrity constraints", e);
        }
    }

    private boolean canDeleteUser(User userToDelete, Long actingUserId) {
        return actingUserId.equals(userToDelete.getUserId()) || isAdmin(actingUserId);
    }

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

        String sqlAuthorities = "SELECT authority_name FROM user_authorities WHERE user_id = ?";
        List<String> roles = jdbcTemplate.queryForList(sqlAuthorities, String.class, user.getUserId());
        List<Authority> authorityList = roles.stream().map(Authority::new).toList();
        user.getAuthorities().addAll(authorityList);

        return user;
    }

    private class UserRowMapper implements RowMapper<User> {
        @Override
        public User mapRow(@Nullable java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
            assert rs != null;
            return mapRowToUser((SqlRowSet) rs);
        }
    }

    @Override
    public String uploadProfilePicture(Long userId, MultipartFile file) throws UserNotFoundException {
        User user = getUserById(userId);

        if (user == null) {
            throw new UserNotFoundException("User not found with ID: " + userId);
        }

        if (user.getUserType() != User.UserType.Musician) {
            throw new UnsupportedOperationException("Profile pictures can only be uploaded for musicians");
        }

        try {
            // Generate a unique filename
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

            // Define the path where you want to store the file
            Path uploadsDir = Paths.get("src", "main", "resources", "uploads");
            if (!Files.exists(uploadsDir)) {
                Files.createDirectories(uploadsDir);
            }
            Path path = uploadsDir.resolve(fileName);

            // Write the file to the specified path
            Files.write(path, file.getBytes());

            // Update the musician's profile picture URL in the database through MusicianProfileDao
            MusicianProfile profile = musicianProfileDao.getMusicianProfileByUserId(userId);
            profile.setProfilePictureUrl(fileName);
            musicianProfileDao.updateMusicianProfile(userId, profile);

            return fileName; // Return the filename here
        } catch (IOException e) {
            throw new DaoException("Could not store the file. Error: " + e.getMessage(), e);
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
    }

    @Override
    public void addInstrumentToMusician(Long userId, String instrumentName) {
        // Since profiles are removed, we'll need to handle instruments differently, possibly storing in user table
        // For now, we'll assume this is not implemented
        throw new UnsupportedOperationException("Instrument addition not supported without profile entities");
    }

    // Removed methods related to profiles since they are no longer applicable
    @Override
    public void saveMusicianProfile(Long userId, MusicianProfile profile) {
        throw new UnsupportedOperationException("Musician profile operations not supported without profile entities");
    }

    @Override
    public void updateMusicianProfile(Long userId, MusicianProfile profile) {
        throw new UnsupportedOperationException("Musician profile operations not supported without profile entities");
    }

    @Override
    public void addGenrePreferenceToVenue(Long userId, String genrePreference) {
        throw new UnsupportedOperationException("Venue profile operations not supported without profile entities");
    }

    @Override
    public MusicianProfile getMusicianProfileByUserId(Long userId) {
        throw new UnsupportedOperationException("Musician profile operations not supported without profile entities");
    }

    @Override
    public VenueProfile getVenueProfileByUserId(Long userId) {
        throw new UnsupportedOperationException("Venue profile operations not supported without profile entities");
    }

    @Override
    public void saveVenueProfile(Long userId, VenueProfile profile) {
        throw new UnsupportedOperationException("Venue profile operations not supported without profile entities");
    }

    @Override
    public void updateVenueProfile(Long userId, VenueProfile profile) {
        throw new UnsupportedOperationException("Venue profile operations not supported without profile entities");
    }
}