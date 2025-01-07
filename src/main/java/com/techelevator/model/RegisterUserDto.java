package com.techelevator.model;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/*
    The acronym DTO is being used for "data transfer object". It means that this type of class is specifically
    created to transfer data between the client and the server. For example, CredentialsDto represents the data a client must
    pass to the server for a login endpoint, and TokenDto represents the object that's returned from the server
    to the client from a login endpoint.
 */
public class RegisterUserDto {

    @NotEmpty(message = "Username cannot be empty.")
    private String username;

    @NotEmpty(message = "Password cannot be empty.")
    private String password;

    @NotEmpty(message = "Confirm Password cannot be empty.")
    private String confirmPassword;

    @NotEmpty(message = "Please select a user type for this user.")
    @Pattern(regexp = "Musician|VenueOwner", message = "User type must be either Musician or VenueOwner.")
    private String userType;

    @NotEmpty(message = "Please select a role for this user.")
    @Pattern(regexp = "ROLE_(ADMIN|USER)", message = "Role must be either ROLE_ADMIN or ROLE_USER.")
    private String role;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}