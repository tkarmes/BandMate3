package com.techelevator.model;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "musician_profiles")
public class MusicianProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "musician_profile_id")
    private Long musicianProfileId;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;

    @Column(name = "name") // New field
    private String name;

    @Column(name = "bio")
    private String bio;

    @Column(name = "location")
    private String location;

    @Column(name = "genres")
    private String genres;

    @Column(name = "instruments")
    private String instruments;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    @Column(name = "created_at")
    private String createdAt;

    @Column(name = "updated_at")
    private String updatedAt;

    // Default constructor
    public MusicianProfile() {}

    // Getters and Setters
    public Long getMusicianProfileId() {
        return musicianProfileId;
    }

    public void setMusicianProfileId(Long musicianProfileId) {
        this.musicianProfileId = musicianProfileId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MusicianProfile that = (MusicianProfile) o;
        return Objects.equals(musicianProfileId, that.musicianProfileId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(musicianProfileId);
    }

    @Override
    public String toString() {
        return "MusicianProfile{" +
                "musicianProfileId=" + musicianProfileId +
                ", name='" + name + '\'' +
                ", bio='" + bio + '\'' +
                ", location='" + location + '\'' +
                ", genres='" + genres + '\'' +
                ", instruments='" + instruments + '\'' +
                ", profilePictureUrl='" + profilePictureUrl + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                '}';
    }
}