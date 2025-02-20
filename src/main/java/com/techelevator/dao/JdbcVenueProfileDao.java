package com.techelevator.dao;

import com.techelevator.exception.DaoException;
import com.techelevator.model.User;
import com.techelevator.model.VenueProfile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

@Component
public class JdbcVenueProfileDao implements VenueProfileDao {

    private final JdbcTemplate jdbcTemplate;

    public JdbcVenueProfileDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public VenueProfile getVenueProfileByUserId(Long userId) throws DaoException {
        String sql = "SELECT * FROM venue_profiles WHERE user_id = ?";
        List<VenueProfile> results = jdbcTemplate.query(sql, new VenueProfileRowMapper(), userId);
        if (!results.isEmpty()) {
            return results.get(0);
        } else {
            throw new DaoException("Venue profile not found for user ID: " + userId);
        }
    }

    @Override
    public VenueProfile createVenueProfile(Long userId, String venueName) throws DaoException {
        String sql = "INSERT INTO venue_profiles (user_id, name) VALUES (?, ?) RETURNING venue_profile_id";
        Long profileId = jdbcTemplate.queryForObject(sql, Long.class, userId, venueName);
        if (profileId == null) {
            throw new DaoException("Failed to create venue profile for user ID: " + userId);
        }
        return getVenueProfileByUserId(userId); // Fetch the newly created profile
    }

    @Override
    public void updateVenueProfile(Long userId, VenueProfile profile) throws DaoException {
        String sql = "UPDATE venue_profiles SET " +
                "name = ?, " +
                "address = ?, " +
                "city = ?, " +
                "state = ?, " +
                "zip_code = ?, " +
                "capacity = ?, " +
                "venue_type = ?, " +
                "phone = ?, " +
                "email = ?, " +
                "website_url = ?, " +
                "operating_hours = ?, " +
                "profile_picture_url = ?, " +
                "genre_preferences = ?, " +
                "amenities = ?, " +
                "updated_at = CURRENT_TIMESTAMP " +
                "WHERE user_id = ?";
        int rowsAffected = jdbcTemplate.update(sql, new PreparedStatementSetter() {
            @Override
            public void setValues(java.sql.PreparedStatement ps) throws SQLException {
                ps.setString(1, profile.getName());
                ps.setString(2, profile.getAddress());
                ps.setString(3, profile.getCity());
                ps.setString(4, profile.getState());
                ps.setString(5, profile.getZipCode());
                ps.setInt(6, profile.getCapacity());
                ps.setString(7, profile.getVenueType());
                ps.setString(8, profile.getPhone());
                ps.setString(9, profile.getEmail());
                ps.setString(10, profile.getWebsiteUrl());
                ps.setString(11, profile.getOperatingHours());
                ps.setString(12, profile.getProfilePictureUrl());
                ps.setArray(13, profile.getGenrePreferences() != null ? ps.getConnection().createArrayOf("text", profile.getGenrePreferences().toArray()) : null);
                ps.setArray(14, profile.getAmenities() != null ? ps.getConnection().createArrayOf("text", profile.getAmenities().toArray()) : null);
                ps.setLong(15, userId);
            }
        });
        if (rowsAffected == 0) {
            throw new DaoException("No profile updated for user ID: " + userId);
        }
    }

    @Override
    public void deleteVenueProfile(Long userId) throws DaoException {
        String sql = "DELETE FROM venue_profiles WHERE user_id = ?";
        int rowsAffected = jdbcTemplate.update(sql, userId);
        if (rowsAffected == 0) {
            throw new DaoException("No profile found to delete for user ID: " + userId);
        }
    }

    @Override
    public void updateVenueName(Long userId, String venueName) throws DaoException {
        String sql = "UPDATE venue_profiles SET name = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";
        int rowsAffected = jdbcTemplate.update(sql, venueName, userId);
        if (rowsAffected == 0) {
            throw new DaoException("No profile updated for user ID: " + userId);
        }
    }

    @Override
    public void updateAddress(Long userId, String address) throws DaoException {
        String sql = "UPDATE venue_profiles SET address = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";
        int rowsAffected = jdbcTemplate.update(sql, address, userId);
        if (rowsAffected == 0) {
            throw new DaoException("No profile updated for user ID: " + userId);
        }
    }

    @Override
    public void updateGenrePreferences(Long userId, List<String> genrePreferences) throws DaoException {
        String sql = "UPDATE venue_profiles SET genre_preferences = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";
        int rowsAffected = jdbcTemplate.update(sql, new PreparedStatementSetter() {
            @Override
            public void setValues(java.sql.PreparedStatement ps) throws SQLException {
                ps.setArray(1, genrePreferences != null ? ps.getConnection().createArrayOf("text", genrePreferences.toArray()) : null);
                ps.setLong(2, userId);
            }
        });
        if (rowsAffected == 0) {
            throw new DaoException("No profile updated for user ID: " + userId);
        }
    }

    @Override
    public void updateAmenities(Long userId, List<String> amenities) throws DaoException {
        String sql = "UPDATE venue_profiles SET amenities = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";
        int rowsAffected = jdbcTemplate.update(sql, new PreparedStatementSetter() {
            @Override
            public void setValues(java.sql.PreparedStatement ps) throws SQLException {
                ps.setArray(1, amenities != null ? ps.getConnection().createArrayOf("text", amenities.toArray()) : null);
                ps.setLong(2, userId);
            }
        });
        if (rowsAffected == 0) {
            throw new DaoException("No profile updated for user ID: " + userId);
        }
    }

    private class VenueProfileRowMapper implements RowMapper<VenueProfile> {
        @Override
        public VenueProfile mapRow(ResultSet rs, int rowNum) throws SQLException {
            VenueProfile profile = new VenueProfile();
            profile.setVenueProfileId(rs.getLong("venue_profile_id"));
            profile.setUser(new User(rs.getLong("user_id")));
            profile.setName(rs.getString("name"));
            profile.setAddress(rs.getString("address"));
            profile.setCity(rs.getString("city"));
            profile.setState(rs.getString("state"));
            profile.setZipCode(rs.getString("zip_code"));
            profile.setCapacity(rs.getInt("capacity"));
            profile.setVenueType(rs.getString("venue_type"));
            profile.setPhone(rs.getString("phone"));
            profile.setEmail(rs.getString("email"));
            profile.setWebsiteUrl(rs.getString("website_url"));
            profile.setOperatingHours(rs.getString("operating_hours"));
            profile.setProfilePictureUrl(rs.getString("profile_picture_url"));
            profile.setCreatedAt(rs.getString("created_at"));
            profile.setUpdatedAt(rs.getString("updated_at"));

            // Fetch genre preferences directly from the array column
            Array genreArray = rs.getArray("genre_preferences");
            List<String> genrePreferences = (genreArray != null) ? Arrays.asList((String[]) genreArray.getArray()) : new ArrayList<>();
            profile.setGenrePreferences(genrePreferences);

            // Fetch amenities directly from the array column
            Array amenitiesArray = rs.getArray("amenities");
            List<String> amenities = (amenitiesArray != null) ? Arrays.asList((String[]) amenitiesArray.getArray()) : new ArrayList<>();
            profile.setAmenities(amenities);

            return profile;
        }
    }
}