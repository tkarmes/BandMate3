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
        String sql = "SELECT * FROM musician_profiles WHERE user_id = ?";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId);
        if (results.next()) {
            return mapRowToMusicianProfile(results);
        } else {
            throw new DaoException("Musician profile not found for user ID: " + userId);
        }
    }

    @Override
    public MusicianProfile createMusicianProfile(Long userId) throws DaoException {
        String sql = "INSERT INTO musician_profiles (user_id, created_at) " +
                "VALUES (?, CURRENT_TIMESTAMP) RETURNING musician_profile_id";
        Long profileId = jdbcTemplate.queryForObject(sql, Long.class, userId);
        if (profileId == null) {
            throw new DaoException("Failed to create musician profile for user ID: " + userId);
        }
        return getMusicianProfileByUserId(userId);
    }

    @Override
    public void updateMusicianProfile(Long userId, MusicianProfile profile) throws DaoException {
        String sql = "UPDATE musician_profiles SET bio = ?, location = ?, genres = ?, instruments = ?, " +
                "profile_picture_url = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";
        int rowsAffected = jdbcTemplate.update(sql,
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
        profile.setUser(new User(rs.getLong("user_id")));
        profile.setBio(rs.getString("bio") != null ? rs.getString("bio") : "");
        profile.setLocation(rs.getString("location") != null ? rs.getString("location") : "");
        profile.setGenres(rs.getString("genres") != null ? rs.getString("genres") : "");
        profile.setInstruments(rs.getString("instruments") != null ? rs.getString("instruments") : "");
        profile.setProfilePictureUrl(rs.getString("profile_picture_url") != null ? rs.getString("profile_picture_url") : "");

        // Handle timestamps as Strings since that's what MusicianProfile expects
        Timestamp createdAt = rs.getTimestamp("created_at");
        profile.setCreatedAt(createdAt != null ? createdAt.toString() : "");
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        profile.setUpdatedAt(updatedAt != null ? updatedAt.toString() : "");

        return profile;
    }
}