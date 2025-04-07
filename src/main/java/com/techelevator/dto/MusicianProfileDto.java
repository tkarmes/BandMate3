package com.techelevator.dto;

import com.techelevator.model.MusicianProfile;

public class MusicianProfileDto {

    private Long musicianProfileId;
    private String name; // Added
    private String bio;
    private String location;
    private String genres;
    private String instruments;
    private String profilePictureUrl;
    private String createdAt;
    private String updatedAt;

    public MusicianProfileDto() {
        // Initialize with default empty strings instead of nulls
        this.name = ""; // Added
        this.bio = "";
        this.location = "";
        this.genres = "";
        this.instruments = "";
        this.profilePictureUrl = "";
        this.createdAt = "";
        this.updatedAt = "";
    }

    public MusicianProfileDto(Long musicianProfileId) {
        this.musicianProfileId = musicianProfileId;
        this.name = ""; // Added
        this.bio = "";
        this.location = "";
        this.genres = "";
        this.instruments = "";
        this.profilePictureUrl = "";
        this.createdAt = "";
        this.updatedAt = "";
    }

    // Static method to convert from entity to DTO
    public static MusicianProfileDto fromEntity(MusicianProfile profile) {
        if (profile == null) {
            return new MusicianProfileDto(); // Return DTO with default empty values
        }

        MusicianProfileDto dto = new MusicianProfileDto(profile.getMusicianProfileId());
        dto.setName(profile.getName() != null ? profile.getName() : ""); // Added
        dto.setBio(profile.getBio() != null ? profile.getBio() : "");
        dto.setLocation(profile.getLocation() != null ? profile.getLocation() : "");
        dto.setGenres(profile.getGenres() != null ? profile.getGenres() : "");
        dto.setInstruments(profile.getInstruments() != null ? profile.getInstruments() : "");
        dto.setProfilePictureUrl(profile.getProfilePictureUrl() != null ? profile.getProfilePictureUrl() : "");
        dto.setCreatedAt(profile.getCreatedAt() != null ? profile.getCreatedAt().toString() : "");
        dto.setUpdatedAt(profile.getUpdatedAt() != null ? profile.getUpdatedAt().toString() : "");
        return dto;
    }

    // Getters and Setters
    public Long getMusicianProfileId() {
        return musicianProfileId;
    }

    public void setMusicianProfileId(Long musicianProfileId) {
        this.musicianProfileId = musicianProfileId;
    }

    public String getName() { // Added
        return name;
    }

    public void setName(String name) { // Added
        this.name = name;
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