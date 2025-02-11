package com.techelevator.dao;

import com.techelevator.exception.DaoException;
import com.techelevator.model.MusicianProfile;

public interface MusicianProfileDao {

    MusicianProfile getMusicianProfileByUserId(Long userId) throws DaoException;

    /**
     * Creates a new musician profile for the given user ID and returns the profile.
     * @param userId The ID of the user for whom the profile is being created.
     * @return The newly created MusicianProfile object.
     * @throws DaoException if there's an error creating the profile in the database.
     */
    MusicianProfile createMusicianProfile(Long userId) throws DaoException;

    /**
     * Updates the musician profile for the given user ID.
     * @param userId The ID of the user whose profile is to be updated.
     * @param profile The updated MusicianProfile object.
     * @throws DaoException if there's an error updating the profile in the database.
     */
    void updateMusicianProfile(Long userId, MusicianProfile profile) throws DaoException;

    /**
     * Deletes the musician profile for the given user ID.
     * @param userId The ID of the user whose profile is to be deleted.
     * @throws DaoException if there's an error deleting the profile from the database.
     */
    void deleteMusicianProfile(Long userId) throws DaoException;

    /**
     * Updates only the bio of the musician profile for the given user ID.
     * @param userId The ID of the user whose profile bio is to be updated.
     * @param bio The new bio content.
     * @throws DaoException if there's an error updating the bio in the database.
     */
    void updateMusicianProfileBio(Long userId, String bio) throws DaoException;

    /**
     * Updates only the instruments of the musician profile for the given user ID.
     * @param userId The ID of the user whose profile instruments are to be updated.
     * @param instruments The new instruments content.
     * @throws DaoException if there's an error updating the instruments in the database.
     */
    void updateMusicianProfileInstruments(Long userId, String instruments) throws DaoException;
}