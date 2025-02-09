package com.techelevator.dto;

import com.techelevator.model.User;
import com.techelevator.model.Authority;

import java.util.List;

public class UserDto {

    private Long userId;
    private String username;
    private String email;
    private String userType;
    private String profilePictureUrl;
    private List<String> roles;

    public UserDto() {}

    public UserDto(Long userId, String username, String email, String userType, String profilePictureUrl, List<String> roles) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.userType = userType;
        this.profilePictureUrl = profilePictureUrl;
        this.roles = roles;
    }

    // Static method to convert from entity to DTO
    public static UserDto fromUser(User user) {
        UserDto dto = new UserDto();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setUserType(user.getUserType().toString());
        // Use the getName method from Authority class
        dto.setRoles(user.getAuthorities().stream().map(Authority::getName).toList());
        return dto;
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}