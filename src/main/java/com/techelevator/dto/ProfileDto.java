package com.techelevator.dto;

import javax.validation.constraints.Size;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.lang.Nullable;

public class ProfileDto {

    private Long profileId;

    @Size(max = 500, message = "Bio must not exceed 500 characters")
    private String bio;

    @NotBlank(message = "Location is required")
    private String location;

    @JsonProperty("musicGenres")
    private String genres;

    @JsonProperty("playedInstruments")
    private String instruments;

    private String venueName;

    @Min(value = 0, message = "Capacity must be a non-negative number")
    private Integer capacity;

    @Nullable
    private String profilePictureUrl;

    // Constructors
    public ProfileDto() {
        // Default constructor for JSON deserialization
    }

    public ProfileDto(Long profileId, String bio, String location, String genres, String instruments,
                      String venueName, Integer capacity, String profilePictureUrl) {
        this.profileId = profileId;
        this.bio = bio;
        this.location = location;
        this.genres = genres;
        this.instruments = instruments;
        this.venueName = venueName;
        this.capacity = capacity;
        this.profilePictureUrl = profilePictureUrl;
    }

    // Getters and Setters
    public Long getProfileId() {
        return profileId;
    }

    public void setProfileId(Long profileId) {
        this.profileId = profileId;
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

    public String getVenueName() {
        return venueName;
    }

    public void setVenueName(String venueName) {
        this.venueName = venueName;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    // Method to convert entity to DTO
    public static ProfileDto fromEntity(com.techelevator.model.Profile profile) {
        ProfileDto dto = new ProfileDto();
        dto.setProfileId(profile.getProfileId());
        dto.setBio(profile.getBio());
        dto.setLocation(profile.getLocation());
        dto.setGenres(profile.getGenres());
        dto.setInstruments(profile.getInstruments());
        dto.setVenueName(profile.getVenueName());
        dto.setCapacity(profile.getCapacity());
        dto.setProfilePictureUrl(profile.getProfilePictureUrl());
        return dto;
    }

    // Method to convert DTO to entity
    public com.techelevator.model.Profile toEntity() {
        com.techelevator.model.Profile profile = new com.techelevator.model.Profile();
        profile.setProfileId(this.profileId);
        profile.setBio(this.bio);
        profile.setLocation(this.location);
        profile.setGenres(this.genres);
        profile.setInstruments(this.instruments);
        profile.setVenueName(this.venueName);
        profile.setCapacity(this.capacity);
        profile.setProfilePictureUrl(this.profilePictureUrl);
        return profile;
    }

    @Override
    public String toString() {
        return "ProfileDto{" +
                "profileId=" + profileId +
                ", bio='" + bio + '\'' +
                ", location='" + location + '\'' +
                ", genres='" + genres + '\'' +
                ", instruments='" + instruments + '\'' +
                ", venueName='" + venueName + '\'' +
                ", capacity=" + capacity +
                ", profilePictureUrl='" + profilePictureUrl + '\'' +
                '}';
    }
}