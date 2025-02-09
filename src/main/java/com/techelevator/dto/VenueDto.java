package com.techelevator.dto;

import java.util.List;

public class VenueDto {

    private Long venueProfileId;
    private String name;
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

    public VenueDto() {}

    public VenueDto(Long venueProfileId, String name, String address, String city, String state, String zipCode, Integer capacity, String venueType, List<String> genrePreferences, String phone, String email, String websiteUrl, String operatingHours, List<String> amenities) {
        this.venueProfileId = venueProfileId;
        this.name = name;
        this.address = address;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.capacity = capacity;
        this.venueType = venueType;
        this.genrePreferences = genrePreferences;
        this.phone = phone;
        this.email = email;
        this.websiteUrl = websiteUrl;
        this.operatingHours = operatingHours;
        this.amenities = amenities;
    }

    // Static method to convert from entity to DTO
    public static VenueDto fromEntity(com.techelevator.model.VenueProfile profile) {
        VenueDto dto = new VenueDto();
        dto.setVenueProfileId(profile.getVenueProfileId());
        dto.setName(profile.getName());
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
        return dto;
    }

    // Getters and Setters
    public Long getVenueProfileId() {
        return venueProfileId;
    }

    public void setVenueProfileId(Long venueProfileId) {
        this.venueProfileId = venueProfileId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}