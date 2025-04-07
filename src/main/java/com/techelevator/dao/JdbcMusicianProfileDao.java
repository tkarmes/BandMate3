package com.techelevator.dao;

import com.techelevator.exception.DaoException;
import com.techelevator.model.MusicianProfile;
import com.techelevator.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;

@Component
public class JdbcMusicianProfileDao implements MusicianProfileDao {

    private final JdbcTemplate jdbcTemplate;

    public JdbcMusicianProfileDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public MusicianProfile getMusicianProfileByUserId(Long userId) throws DaoException {
        String sql = "SELECT mp.musician_profile_id, mp.user_id, mp.name, mp.bio, mp.location, mp.genres, mp.instruments, " +
                "mp.profile_picture_url, mp.created_at, mp.updated_at, u.username " +
                "FROM musician_profiles mp " +
                "JOIN users u ON mp.user_id = u.user_id " +
                "WHERE mp.user_id = ?";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId);
        if (results.next()) {
            return mapRowToMusicianProfile(results);
        } else {
            throw new DaoException("Musician profile not found for user ID: " + userId);
        }
    }

    @Override
    public MusicianProfile createMusicianProfile(Long userId) throws DaoException {
        String sql = "INSERT INTO musician_profiles (user_id, name, created_at) " +
                "VALUES (?, 'New Musician', CURRENT_TIMESTAMP) RETURNING musician_profile_id";
        Long profileId = jdbcTemplate.queryForObject(sql, Long.class, userId);
        if (profileId == null) {
            throw new DaoException("Failed to create musician profile for user ID: " + userId);
        }
        return getMusicianProfileByUserId(userId);
    }

    @Override
    public void updateMusicianProfile(Long userId, MusicianProfile profile) throws DaoException {
        String sql = "UPDATE musician_profiles " +
                "SET name = ?, bio = ?, location = ?, genres = ?, instruments = ?, profile_picture_url = ?, " +
                "updated_at = CURRENT_TIMESTAMP " +
                "WHERE user_id = ?";
        int rowsAffected = jdbcTemplate.update(sql,
                profile.getName() != null ? profile.getName() : "",
                profile.getBio() != null ? profile.getBio() : "",
                profile.getLocation() != null ? profile.getLocation() : "",
                profile.getGenres() != null ? profile.getGenres() : "",
                profile.getInstruments() != null ? profile.getInstruments() : "",
                profile.getProfilePictureUrl() != null ? profile.getProfilePictureUrl() : "",
                userId
        );
        if (rowsAffected == 0) {
            throw new DaoException("No profile updated for user ID: " + userId);
        }
    }

    @Override
    public void deleteMusicianProfile(Long userId) throws DaoException {
        String sql = "DELETE FROM musician_profiles WHERE user_id = ?";
        int rowsAffected = jdbcTemplate.update(sql, userId);
        if (rowsAffected == 0) {
            throw new DaoException("No profile found to delete for user ID: " + userId);
        }
    }

    @Override
    public void updateMusicianProfileBio(Long userId, String bio) throws DaoException {
        String sql = "UPDATE musician_profiles SET bio = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";
        int rowsAffected = jdbcTemplate.update(sql, bio != null ? bio : "", userId);
        if (rowsAffected == 0) {
            throw new DaoException("No profile updated for user ID: " + userId);
        }
    }

    @Override
    public void updateMusicianProfileInstruments(Long userId, String instruments) throws DaoException {
        String sql = "UPDATE musician_profiles SET instruments = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";
        int rowsAffected = jdbcTemplate.update(sql, instruments != null ? instruments : "", userId);
        if (rowsAffected == 0) {
            throw new DaoException("No profile updated for user ID: " + userId);
        }
    }

    private MusicianProfile mapRowToMusicianProfile(SqlRowSet rs) {
        MusicianProfile profile = new MusicianProfile();
        profile.setMusicianProfileId(rs.getLong("musician_profile_id"));

        User user = new User();
        user.setUserId(rs.getLong("user_id"));
        user.setUsername(rs.getString("username"));
        profile.setUser(user);

        profile.setName(rs.getString("name") != null ? rs.getString("name") : ""); // Added
        profile.setBio(rs.getString("bio") != null ? rs.getString("bio") : "");
        profile.setLocation(rs.getString("location") != null ? rs.getString("location") : "");
        profile.setGenres(rs.getString("genres") != null ? rs.getString("genres") : "");
        profile.setInstruments(rs.getString("instruments") != null ? rs.getString("instruments") : "");
        profile.setProfilePictureUrl(rs.getString("profile_picture_url") != null ? rs.getString("profile_picture_url") : "");

        Timestamp createdAt = rs.getTimestamp("created_at");
        profile.setCreatedAt(createdAt != null ? createdAt.toString() : "");
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        profile.setUpdatedAt(updatedAt != null ? updatedAt.toString() : "");

        return profile;
    }
}