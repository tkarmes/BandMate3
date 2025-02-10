package com.techelevator.dao;

import com.techelevator.exception.DaoException;
import com.techelevator.model.MusicianProfile;
import com.techelevator.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.util.Objects;

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
    public void createMusicianProfile(Long userId) throws DaoException {
        String sql = "INSERT INTO musician_profiles (user_id) VALUES (?)";
        int rowsAffected = jdbcTemplate.update(sql, userId);
        if (rowsAffected == 0) {
            throw new DaoException("Failed to create musician profile for user ID: " + userId);
        }
    }

    @Override
    public void updateMusicianProfile(Long userId, MusicianProfile profile) throws DaoException {
        String sql = "UPDATE musician_profiles SET bio = ?, location = ?, genres = ?, instruments = ?, profile_picture_url = ? WHERE user_id = ?";
        int rowsAffected = jdbcTemplate.update(sql,
                profile.getBio(),
                profile.getLocation(),
                profile.getGenres(),
                profile.getInstruments(),
                profile.getProfilePictureUrl(),
                userId
        );
        if (rowsAffected == 0) {
            throw new DaoException("No profile updated for user ID: " + userId);
        }
    }

    private MusicianProfile mapRowToMusicianProfile(SqlRowSet rs) {
        MusicianProfile profile = new MusicianProfile();
        profile.setMusicianProfileId(rs.getLong("musician_profile_id"));
        profile.setUser(new User(rs.getLong("user_id"))); // Assuming User has a constructor with userId
        profile.setBio(rs.getString("bio"));
        profile.setLocation(rs.getString("location"));
        profile.setGenres(rs.getString("genres"));
        profile.setInstruments(rs.getString("instruments"));
        profile.setProfilePictureUrl(rs.getString("profile_picture_url"));
        profile.setCreatedAt(rs.getString("created_at"));
        profile.setUpdatedAt(rs.getString("updated_at"));
        return profile;
    }
}
