package com.techelevator.dto;

public class MusicianDto {

    private Long musicianProfileId;
    private String bio;
    private String location;
    private String genres;
    private String instruments;

    public MusicianDto() {}

    public MusicianDto(Long musicianProfileId, String bio, String location, String genres, String instruments) {
        this.musicianProfileId = musicianProfileId;
        this.bio = bio;
        this.location = location;
        this.genres = genres;
        this.instruments = instruments;
    }

    // Static method to convert from entity to DTO
    public static MusicianDto fromEntity(com.techelevator.model.MusicianProfile profile) {
        MusicianDto dto = new MusicianDto();
        dto.setMusicianProfileId(profile.getMusicianProfileId());
        dto.setBio(profile.getBio());
        dto.setLocation(profile.getLocation());
        dto.setGenres(profile.getGenres());
        dto.setInstruments(profile.getInstruments());
        return dto;
    }

    // Getters and Setters
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
}