package com.company.jobmonitor.dto;

import com.company.jobmonitor.entity.User;
import java.time.LocalDateTime;

public class UserDto {

  private Integer id;
  private String username;
  private String email;
  private String firstName;
  private String lastName;
  private String fullName;
  private User.Role role;
  private boolean enabled;
  private boolean accountNonExpired;
  private boolean accountNonLocked;
  private boolean credentialsNonExpired;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  // Constructors
  public UserDto() {}

  public UserDto(
      Integer id,
      String username,
      String email,
      String firstName,
      String lastName,
      String fullName,
      User.Role role,
      boolean enabled,
      boolean accountNonExpired,
      boolean accountNonLocked,
      boolean credentialsNonExpired,
      LocalDateTime createdAt,
      LocalDateTime updatedAt) {
    this.id = id;
    this.username = username;
    this.email = email;
    this.firstName = firstName;
    this.lastName = lastName;
    this.fullName = fullName;
    this.role = role;
    this.enabled = enabled;
    this.accountNonExpired = accountNonExpired;
    this.accountNonLocked = accountNonLocked;
    this.credentialsNonExpired = credentialsNonExpired;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  // Static factory method
  public static UserDto from(User user) {
    return new UserDto(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        user.getFirstName(),
        user.getLastName(),
        user.getFullName(),
        user.getRole(),
        user.isEnabled(),
        user.isAccountNonExpired(),
        user.isAccountNonLocked(),
        user.isCredentialsNonExpired(),
        user.getCreatedAt(),
        user.getUpdatedAt());
  }

  // Getters and Setters
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
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

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public User.Role getRole() {
    return role;
  }

  public void setRole(User.Role role) {
    this.role = role;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public boolean isAccountNonExpired() {
    return accountNonExpired;
  }

  public void setAccountNonExpired(boolean accountNonExpired) {
    this.accountNonExpired = accountNonExpired;
  }

  public boolean isAccountNonLocked() {
    return accountNonLocked;
  }

  public void setAccountNonLocked(boolean accountNonLocked) {
    this.accountNonLocked = accountNonLocked;
  }

  public boolean isCredentialsNonExpired() {
    return credentialsNonExpired;
  }

  public void setCredentialsNonExpired(boolean credentialsNonExpired) {
    this.credentialsNonExpired = credentialsNonExpired;
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
}
