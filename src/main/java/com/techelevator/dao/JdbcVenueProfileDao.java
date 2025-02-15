package com.techelevator.dao;

import com.techelevator.exception.DaoException;
import com.techelevator.model.User;
import com.techelevator.model.VenueProfile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JdbcVenueProfileDao implements VenueProfileDao {

    private final JdbcTemplate jdbcTemplate;

    public JdbcVenueProfileDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public VenueProfile getVenueProfileByUserId(Long userId) throws DaoException {
        String sql = "SELECT * FROM venue_profiles WHERE user_id = ?";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId);
        if (results.next()) {
            return mapRowToVenueProfile(results);
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
                "updated_at = CURRENT_TIMESTAMP " +
                "WHERE user_id = ?";
        int rowsAffected = jdbcTemplate.update(sql,
                profile.getName(),
                profile.getAddress(),
                profile.getCity(),
                profile.getState(),
                profile.getZipCode(),
                profile.getCapacity(),
                profile.getVenueType(),
                profile.getPhone(),
                profile.getEmail(),
                profile.getWebsiteUrl(),
                profile.getOperatingHours(),
                profile.getProfilePictureUrl(),
                userId
        );
        if (rowsAffected == 0) {
            throw new DaoException("No profile updated for user ID: " + userId);
        }

        // Handle genre preferences and amenities
        updateGenrePreferencesForProfile(profile.getVenueProfileId(), profile.getGenrePreferences());
        updateAmenitiesForProfile(profile.getVenueProfileId(), profile.getAmenities());
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
        VenueProfile profile = getVenueProfileByUserId(userId);
        updateGenrePreferencesForProfile(profile.getVenueProfileId(), genrePreferences);
    }

    @Override
    public void updateAmenities(Long userId, List<String> amenities) throws DaoException {
        VenueProfile profile = getVenueProfileByUserId(userId);
        updateAmenitiesForProfile(profile.getVenueProfileId(), amenities);
    }

    private void updateGenrePreferencesForProfile(Long venueProfileId, List<String> genrePreferences) {
        String deleteSql = "DELETE FROM venue_genre_preferences WHERE venue_profile_id = ?";
        jdbcTemplate.update(deleteSql, venueProfileId);

        String insertSql = "INSERT INTO venue_genre_preferences (venue_profile_id, genre_preference) VALUES (?, ?)";
        for (String genre : genrePreferences) {
            jdbcTemplate.update(insertSql, venueProfileId, genre);
        }
    }

    private void updateAmenitiesForProfile(Long venueProfileId, List<String> amenities) {
        String deleteSql = "DELETE FROM venue_amenities WHERE venue_profile_id = ?";
        jdbcTemplate.update(deleteSql, venueProfileId);

        String insertSql = "INSERT INTO venue_amenities (venue_profile_id, amenity) VALUES (?, ?)";
        for (String amenity : amenities) {
            jdbcTemplate.update(insertSql, venueProfileId, amenity);
        }
    }

    private VenueProfile mapRowToVenueProfile(SqlRowSet rs) {
        VenueProfile profile = new VenueProfile();
        profile.setVenueProfileId(rs.getLong("venue_profile_id"));
        profile.setUser(new User(rs.getLong("user_id"))); // Assuming User has a constructor with userId
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

        // Fetch genre preferences
        profile.setGenrePreferences(fetchGenrePreferences(profile.getVenueProfileId()));

        // Fetch amenities
        profile.setAmenities(fetchAmenities(profile.getVenueProfileId()));

        return profile;
    }

    private List<String> fetchGenrePreferences(Long venueProfileId) {
        String sql = "SELECT genre_preference FROM venue_genre_preferences WHERE venue_profile_id = ?";
        return jdbcTemplate.queryForList(sql, String.class, venueProfileId);
    }

    private List<String> fetchAmenities(Long venueProfileId) {
        String sql = "SELECT amenity FROM venue_amenities WHERE venue_profile_id = ?";
        return jdbcTemplate.queryForList(sql, String.class, venueProfileId);
    }
}