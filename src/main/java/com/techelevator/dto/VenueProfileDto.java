package com.techelevator.dto;

import java.util.List;
import com.techelevator.model.VenueProfile;

public class VenueProfileDto {

    private Long venueProfileId;
    private String venueName;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private Integer capacity;
    private String venueType;
    private List<String> genrePreferences;
    private String phone;
    private String email;
    private String websiteUrl;
    private String operatingHours;
    private List<String> amenities;
    private String profilePictureUrl;
    private String createdAt;
    private String updatedAt;

    public VenueProfileDto() {
        // default constructor
    }

    public VenueProfileDto(Long venueProfileId) {
        // constructor with default blank values
        this.venueProfileId = venueProfileId;
        this.venueName = "";
        this.address = "";
        this.city = "";
        this.state = "";
        this.zipCode = "";
        this.capacity = 0;
        this.venueType = "";
        this.genrePreferences = List.of();
        this.phone = "";
        this.email = "";
        this.websiteUrl = "";
        this.operatingHours = "";
        this.amenities = List.of();
        this.profilePictureUrl = null;
        this.createdAt = null;
        this.updatedAt = null;
    }

    // Static method to convert from entity to DTO
    public static VenueProfileDto fromEntity(VenueProfile profile) {
        VenueProfileDto dto = new VenueProfileDto(profile.getVenueProfileId());
        dto.setVenueName(profile.getName());
        dto.setAddress(profile.getAddress());
        dto.setCity(profile.getCity());
        dto.setState(profile.getState());
        dto.setZipCode(profile.getZipCode());
        dto.setCapacity(profile.getCapacity());
        dto.setVenueType(profile.getVenueType());
        dto.setGenrePreferences(profile.getGenrePreferences());
        dto.setPhone(profile.getPhone());
        dto.setEmail(profile.getEmail());
        dto.setWebsiteUrl(profile.getWebsiteUrl());
        dto.setOperatingHours(profile.getOperatingHours());
        dto.setAmenities(profile.getAmenities());
        dto.setProfilePictureUrl(profile.getProfilePictureUrl());
        dto.setCreatedAt(profile.getCreatedAt());
        dto.setUpdatedAt(profile.getUpdatedAt());
        return dto;
    }

    // Getters and Setters...

    public Long getVenueProfileId() {
        return venueProfileId;
    }

    public void setVenueProfileId(Long venueProfileId) {
        this.venueProfileId = venueProfileId;
    }

    public String getVenueName() {
        return venueName;
    }

    public void setVenueName(String venueName) {
        this.venueName = venueName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public String getVenueType() {
        return venueType;
    }

    public void setVenueType(String venueType) {
        this.venueType = venueType;
    }

    public List<String> getGenrePreferences() {
        return genrePreferences;
    }

    public void setGenrePreferences(List<String> genrePreferences) {
        this.genrePreferences = genrePreferences;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public String getOperatingHours() {
        return operatingHours;
    }

    public void setOperatingHours(String operatingHours) {
        this.operatingHours = operatingHours;
    }

    public List<String> getAmenities() {
        return amenities;
    }

    public void setAmenities(List<String> amenities) {
        this.amenities = amenities;
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
