package com.techelevator.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Email;

public class RegisterUserDto {

    @NotEmpty(message = "Username cannot be empty.")
    private String username;

    @NotEmpty(message = "Password cannot be empty.")
    private String password;

    @NotEmpty(message = "Confirm Password cannot be empty.")
    private String confirmPassword;

    @NotEmpty(message = "Please select a user type for this user.")
    @Pattern(regexp = "Musician|VenueOwner|Admin", message = "User type must be either Musician, VenueOwner or Admin.")
    private String userType;

    @NotEmpty(message = "Please select a role for this user.")
    @Pattern(regexp = "ROLE_(ADMIN|USER)", message = "Role must be either ROLE_ADMIN or ROLE_USER.")
    private String role;

    @Email(message = "Email should be valid")
    @NotEmpty(message = "Email cannot be empty.")
    private String email;

    // Venue name for VenueOwner (required only for them)
    private String venueName;

    // Musician name for Musician (optional, added now)
    private String musicianName;

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getVenueName() { return venueName; }
    public void setVenueName(String venueName) { this.venueName = venueName; }
    public String getMusicianName() { return musicianName; } // Added
    public void setMusicianName(String musicianName) { this.musicianName = musicianName; } // Added
}