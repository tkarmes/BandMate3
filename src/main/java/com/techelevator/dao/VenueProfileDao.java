package com.techelevator.dao;

import com.techelevator.exception.DaoException;
import com.techelevator.model.VenueProfile;

import java.util.List;

public interface VenueProfileDao {

    VenueProfile getVenueProfileByUserId(Long userId) throws DaoException;

    void createVenueProfile(Long userId) throws DaoException;

    void updateVenueProfile(Long userId, VenueProfile profile) throws DaoException;
}
