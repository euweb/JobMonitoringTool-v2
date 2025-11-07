package com.company.jobmonitor.dto;

import com.company.jobmonitor.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for User entity.
 *
 * <p>This DTO provides a secure representation of user data for API responses, excluding sensitive
 * information such as password hashes. It includes all necessary user profile information and
 * account status details.
 *
 * <p>The DTO automatically computes derived fields like {@code fullName} and provides factory
 * methods for easy conversion from User entities.
 *
 * @author JobMonitor Team
 * @version 1.0
 * @since 1.0
 */
@Schema(description = "User data transfer object for secure API responses")
public class UserDto {

  @Schema(description = "Unique user identifier", example = "1")
  private Integer id;

  @Schema(description = "Unique username for login", example = "john_doe")
  private String username;

  @Schema(description = "User's email address", example = "john.doe@example.com")
  private String email;

  @Schema(description = "User's first name", example = "John")
  private String firstName;

  @Schema(description = "User's last name", example = "Doe")
  private String lastName;

  @Schema(description = "Computed full name (first + last)", example = "John Doe")
  private String fullName;

  @Schema(description = "User's system role", example = "USER")
  private User.Role role;

  @Schema(description = "Whether the user account is enabled", example = "true")
  private boolean enabled;

  @Schema(description = "Whether the user account is not expired", example = "true")
  private boolean accountNonExpired;

  @Schema(description = "Whether the user account is not locked", example = "true")
  private boolean accountNonLocked;

  @Schema(description = "Whether the user credentials are not expired", example = "true")
  private boolean credentialsNonExpired;

  @Schema(description = "User account creation timestamp", example = "2024-01-15T10:30:00")
  private LocalDateTime createdAt;

  @Schema(description = "User account last update timestamp", example = "2024-01-20T14:45:00")
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
