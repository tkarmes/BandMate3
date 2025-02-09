package com.techelevator.model;

import javax.persistence.*;
import java.util.Arrays;
import java.util.Objects;

@Entity
@Table(name = "profiles")
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Long profileId;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;

    @Column(name = "bio")
    private String bio;

    @Column(name = "location")
    private String location;

    @Column(name = "genres")
    private String genres;

    @Column(name = "instruments")
    private String instruments;

    // Other fields for VenueOwners like venue_name, capacity
    @Column(name = "venue_name")
    private String venueName;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    // Default constructor
    public Profile() {}

    // Getters and Setters
    public Long getProfileId() {
        return profileId;
    }

    public void setProfileId(Long profileId) {
        this.profileId = profileId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Profile profile = (Profile) o;
        return Objects.equals(profileId, profile.profileId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(profileId);
    }

    @Override
    public String toString() {
        return "Profile{" +
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