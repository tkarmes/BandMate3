package com.techelevator.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   @Column(name = "user_id")
   private Long userId;

   @Column(name = "username", unique = true, nullable = false)
   private String username;

   @Column(name = "email", unique = true, nullable = false)
   private String email;

   @JsonIgnore
   @Column(name = "password_hash", nullable = false)
   private String passwordHash;

   @Column(name = "user_type", nullable = false)
   @Enumerated(EnumType.STRING)
   private UserType userType;

   @Column(name = "created_at")
   @Temporal(TemporalType.TIMESTAMP)
   private Date createdAt;

   @ElementCollection
   @CollectionTable(name = "user_authorities", joinColumns = @JoinColumn(name = "user_id"))
   @Column(name = "authority_name")
   private Set<Authority> authorities = new HashSet<>();

   // Default constructor
   public User() {}

   // Constructor with all fields except userId (which is auto-generated)
   public User(String username, String email, String passwordHash, UserType userType) {
      this.username = username;
      this.email = email;
      this.passwordHash = passwordHash;
      this.userType = userType;
      //this.createdAt = new Date(); // Set current time when user is created
      this.authorities.add(new Authority("ROLE_USER")); // Default role
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

   public String getPasswordHash() {
      return passwordHash;
   }

   public void setPasswordHash(String passwordHash) {
      this.passwordHash = passwordHash;
   }

   public UserType getUserType() {
      return userType;
   }

   public void setUserType(UserType userType) {
      this.userType = userType;
   }

   public Date getCreatedAt() {
      return createdAt;
   }

   public void setCreatedAt(Date createdAt) {
      this.createdAt = createdAt;
   }

   public Set<Authority> getAuthorities() {
      return authorities;
   }



   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      User user = (User) o;
      return Objects.equals(userId, user.userId) &&
              Objects.equals(username, user.username) &&
              Objects.equals(email, user.email) &&
              Objects.equals(passwordHash, user.passwordHash) &&
              userType == user.userType &&
              Objects.equals(createdAt, user.createdAt) &&
              Objects.equals(this.authorities, user.authorities);
   }

   @Override
   public int hashCode() {
      return Objects.hash(userId, username, email, passwordHash, userType, createdAt, authorities);
   }

   @Override
   public String toString() {
      return "User{" +
              "userId=" + userId +
              ", username='" + username + '\'' +
              ", email='" + email + '\'' +
              ", userType=" + userType +
              ", createdAt=" + createdAt +
              ", authorities=" + authorities +
              '}';
   }

   public enum UserType {
      Musician, VenueOwner
   }
}