package com.company.jobmonitor.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "User registration request data")
public class SignUpRequest {

  @NotBlank(message = "Username is required")
  @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
  @Schema(
      description = "Unique username for the new account",
      example = "john_doe",
      minLength = 3,
      maxLength = 20)
  private String username;

  @NotBlank(message = "Email is required")
  @Email(message = "Email should be valid")
  @Size(max = 50, message = "Email must not exceed 50 characters")
  @Schema(description = "User's email address", example = "john.doe@example.com", maxLength = 50)
  private String email;

  @NotBlank(message = "Password is required")
  @Size(min = 6, max = 40, message = "Password must be between 6 and 40 characters")
  @Schema(
      description = "Password for the new account",
      example = "securePassword123",
      minLength = 6,
      maxLength = 40)
  private String password;

  @NotBlank(message = "First name is required")
  @Size(max = 20, message = "First name must not exceed 20 characters")
  @Schema(description = "User's first name", example = "John", maxLength = 20)
  private String firstName;

  @NotBlank(message = "Last name is required")
  @Size(max = 20, message = "Last name must not exceed 20 characters")
  @Schema(description = "User's last name", example = "Doe", maxLength = 20)
  private String lastName;

  // Constructors
  public SignUpRequest() {}

  public SignUpRequest(
      String username, String email, String password, String firstName, String lastName) {
    this.username = username;
    this.email = email;
    this.password = password;
    this.firstName = firstName;
    this.lastName = lastName;
  }

  // Getters and Setters
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

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
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
}
