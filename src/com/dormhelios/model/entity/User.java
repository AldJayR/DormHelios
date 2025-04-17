package com.dormhelios.model.entity;

import java.time.LocalDateTime;
import java.util.Objects;

public class User {

    public enum Role {
        ADMIN, LANDLORD, TENANT
    }

    private int userId;
    private String username;
    private String passwordHash;
    private String fullName;
    private Role role;
    private String email;
    private String phoneNumber;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public User() {
        this.isActive = true;
    }

    public User(String username, String passwordHash, String fullName, Role role, String email, String phoneNumber) {
        this();
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId != 0 ? userId : username);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        User user = (User) o;
        if (userId != 0) {
            return userId == user.userId;
        }
        return Objects.equals(username, user.username);
    }

    @Override
    public String toString() {
        return "User{" + "userId=" + userId + ", username=" + username + ", passwordHash=" + passwordHash + ", fullName=" + fullName + ", role=" + role + ", email=" + email + ", phoneNumber=" + phoneNumber + ", isActive=" + isActive + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + '}';
    }
    
    

}
