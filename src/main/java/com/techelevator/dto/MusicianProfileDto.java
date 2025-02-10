package com.techelevator.dto;

import com.techelevator.model.MusicianProfile;

public class MusicianProfileDto {

    private Long musicianProfileId;
    private String bio;
    private String location;
    private String genres;
    private String instruments;
    private String profilePictureUrl;
    private String createdAt;
    private String updatedAt;

    public MusicianProfileDto() {
        // default constructor
    }

    public MusicianProfileDto(Long musicianProfileId) {
        // constructor with default blank values
        this.musicianProfileId = musicianProfileId;
        this.bio = "";
        this.location = "";
        this.genres = "";
        this.instruments = "";
        this.profilePictureUrl = null;
        this.createdAt = null;
        this.updatedAt = null;
    }

    // Static method to convert from entity to DTO
    public static MusicianProfileDto fromEntity(MusicianProfile profile) {
        MusicianProfileDto dto = new MusicianProfileDto(profile.getMusicianProfileId());
        dto.setBio(profile.getBio());
        dto.setLocation(profile.getLocation());
        dto.setGenres(profile.getGenres());
        dto.setInstruments(profile.getInstruments());
        dto.setProfilePictureUrl(profile.getProfilePictureUrl());
        // Note: createdAt and updatedAt are set to null here since we're assuming they're String in the DTO and might not be set or formatted the same way as in the entity
        return dto;
    }

    // Getters and Setters...

    public Long getMusicianProfileId() {
        return musicianProfileId;
    }

    public void setMusicianProfileId(Long musicianProfileId) {
        this.musicianProfileId = musicianProfileId;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getGenres() {
        return genres;
    }

    public void setGenres(String genres) {
        this.genres = genres;
    }

    public String getInstruments() {
        return instruments;
    }

    public void setInstruments(String instruments) {
        this.instruments = instruments;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}