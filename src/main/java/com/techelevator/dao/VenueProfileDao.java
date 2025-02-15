package com.techelevator.dao;

import com.techelevator.exception.DaoException;
import com.techelevator.model.VenueProfile;

import java.util.List;

public interface VenueProfileDao {

    /**
     * Retrieves the venue profile for the given user ID.
     * @param userId The ID of the user whose venue profile is to be retrieved.
     * @return The VenueProfile object for the user.
     * @throws DaoException if there's an error fetching the profile from the database.
     */
    VenueProfile getVenueProfileByUserId(Long userId) throws DaoException;

    /**
     * Creates a new venue profile for the given user ID with the specified venue name and returns the profile.
     * @param userId The ID of the user for whom the profile is being created.
     * @param venueName The name of the venue to be set for the new profile.
     * @return The newly created VenueProfile object.
     * @throws DaoException if there's an error creating the profile in the database.
     */
    VenueProfile createVenueProfile(Long userId, String venueName) throws DaoException;

    /**
     * Updates the venue profile for the given user ID.
     * @param userId The ID of the user whose profile is to be updated.
     * @param profile The updated VenueProfile object.
     * @throws DaoException if there's an error updating the profile in the database.
     */
    void updateVenueProfile(Long userId, VenueProfile profile) throws DaoException;

    /**
     * Deletes the venue profile for the given user ID.
     * @param userId The ID of the user whose profile is to be deleted.
     * @throws DaoException if there's an error deleting the profile from the database.
     */
    void deleteVenueProfile(Long userId) throws DaoException;

    /**
     * Updates only the venue name in the venue profile for the given user ID.
     * @param userId The ID of the user whose profile venue name is to be updated.
     * @param venueName The new venue name.
     * @throws DaoException if there's an error updating the venue name in the database.
     */
    void updateVenueName(Long userId, String venueName) throws DaoException;

    /**
     * Updates only the address in the venue profile for the given user ID.
     * @param userId The ID of the user whose profile address is to be updated.
     * @param address The new address.
     * @throws DaoException if there's an error updating the address in the database.
     */
    void updateAddress(Long userId, String address) throws DaoException;

    void updateGenrePreferences(Long userId, List<String> genrePreferences) throws DaoException;

    void updateAmenities(Long userId, List<String> amenities) throws DaoException;
}