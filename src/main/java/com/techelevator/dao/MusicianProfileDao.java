package com.techelevator.dao;

import com.techelevator.exception.DaoException;
import com.techelevator.model.MusicianProfile;

public interface MusicianProfileDao {

    MusicianProfile getMusicianProfileByUserId(Long userId) throws DaoException;

    void createMusicianProfile(Long userId) throws DaoException;

    void updateMusicianProfile(Long userId, MusicianProfile profile) throws DaoException;
}