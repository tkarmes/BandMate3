package com.techelevator.dao;

import com.techelevator.exception.DaoException;
import com.techelevator.exception.UserCreationException;
import com.techelevator.exception.UserNotFoundException;
import com.techelevator.exception.UserDeletionException;
import com.techelevator.dto.RegisterUserDto;
import com.techelevator.model.User;
import com.techelevator.model.Profile;
import com.techelevator.model.Authority;
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
        String insertUserSql = "INSERT INTO users (username, password_hash, user_type, email) VALUES (LOWER(TRIM(?)), ?, ?, ?) RETURNING user_id";
        String password_hash = new BCryptPasswordEncoder().encode(user.getPassword());
        User.UserType userType = User.UserType.valueOf(user.getUserType());
        try {
            Long newUserId = jdbcTemplate.queryForObject(insertUserSql, Long.class, user.getUsername(), password_hash, userType.toString(), user.getEmail());

            // Insert into user_authorities table
            String insertAuthoritySql = "INSERT INTO user_authorities (user_id, authority_name) VALUES (?, ?)";
            String role = user.getRole().toUpperCase().startsWith("ROLE_") ? user.getRole().toUpperCase() : "ROLE_" + user.getRole().toUpperCase();
            jdbcTemplate.update(insertAuthoritySql, newUserId, role);

            // Create profile
            String insertProfileSql = "INSERT INTO profiles (user_id) VALUES (?)";
            jdbcTemplate.update(insertProfileSql, newUserId);

            return getUserById(newUserId); // Retrieve the newly created user with profile
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

        // Delete from profiles first to maintain referential integrity
        String sql = "DELETE FROM profiles WHERE user_id = ?";
        jdbcTemplate.update(sql, userToDelete.getUserId());

        // Now delete from user_authorities
        sql = "DELETE FROM user_authorities WHERE user_id = ?";
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

        // Fetch profile
        Profile profile = getProfileByUserId(user.getUserId());
        user.setProfile(profile);

        return user;
    }

    private Profile getProfileByUserId(Long userId) {
        String sql = "SELECT * FROM profiles WHERE user_id = ?";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sql, userId);
        if (rs.next()) {
            Profile profile = new Profile();
            profile.setProfileId(rs.getLong("profile_id"));
            profile.setUser(new User(userId)); // Just setting user_id here, full User might need to be fetched separately
            profile.setBio(rs.getString("bio"));
            profile.setLocation(rs.getString("location"));
            profile.setGenres(rs.getString("genres")); // Directly setting as a string
            profile.setInstruments(rs.getString("instruments")); // Changed to single string
            profile.setVenueName(rs.getString("venue_name"));
            profile.setCapacity(rs.getInt("capacity"));
            profile.setProfilePictureUrl(rs.getString("profile_picture_url"));
            return profile;
        }
        return null;
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

            // Update the user's profile picture URL in the database
            String sql = "UPDATE profiles SET profile_picture_url = ? WHERE user_id = ?";
            jdbcTemplate.update(sql, "uploads/" + fileName, userId); // Store relative path for simplicity

            return fileName; // Return the filename here

        } catch (IOException e) {
            throw new DaoException("Could not store the file. Error: " + e.getMessage(), e);
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
    }

    @Override
    public void addInstrumentToUser(Long userId, String instrumentName) {
        String getInstrumentsSql = "SELECT instruments FROM profiles WHERE user_id = ?";
        String updateInstrumentsSql = "UPDATE profiles SET instruments = ? WHERE user_id = ?";

        try {
            String currentInstruments = jdbcTemplate.queryForObject(getInstrumentsSql, String.class, userId);
            String newInstruments = currentInstruments != null
                    ? currentInstruments + (currentInstruments.isEmpty() ? "" : ", ") + instrumentName
                    : instrumentName;
            jdbcTemplate.update(updateInstrumentsSql, newInstruments, userId);

        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (Exception e) {
            throw new DaoException("Failed to add instrument to user", e);
        }
    }

    @Override
    public void saveUserWithProfile(User user) {
        if (user.getProfile() != null) {
            Profile profile = user.getProfile();
            String updateProfileSql = "UPDATE profiles SET bio = ?, location = ?, genres = ?, instruments = ?, venue_name = ?, capacity = ?, profile_picture_url = ? WHERE user_id = ?";

            jdbcTemplate.update(updateProfileSql,
                    profile.getBio(),
                    profile.getLocation(),
                    profile.getGenres(),
                    profile.getInstruments(), // Now directly using the string
                    profile.getVenueName(),
                    profile.getCapacity(),
                    profile.getProfilePictureUrl(),
                    user.getUserId()
            );
        }
    }

    @Override
    public void updateUserProfile(Long userId, Profile profile) {
        String updateProfileSql = "UPDATE profiles SET bio = ?, location = ?, genres = ?, instruments = ?, venue_name = ?, capacity = ?, profile_picture_url = ? WHERE user_id = ?";

        try {
            jdbcTemplate.update(updateProfileSql,
                    profile.getBio(),
                    profile.getLocation(),
                    profile.getGenres(), // Directly using the string
                    profile.getInstruments(), // Directly using the string
                    profile.getVenueName(),
                    profile.getCapacity(),
                    profile.getProfilePictureUrl(),
                    userId
            );
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (Exception e) {
            throw new DaoException("Failed to update user profile", e);
        }
    }
}