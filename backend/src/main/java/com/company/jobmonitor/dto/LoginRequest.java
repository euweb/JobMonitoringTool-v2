package com.company.jobmonitor.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for user login requests.
 *
 * <p>Contains the username and password credentials required for authentication. Used with the POST
 * /api/auth/login endpoint.
 *
 * @author JobMonitor Team
 * @version 1.0
 * @since 1.0
 */
@Schema(description = "User login credentials")
public class LoginRequest {

  @Schema(description = "Username for authentication", example = "admin", required = true)
  @NotBlank(message = "Username is required")
  @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
  private String username;

  @Schema(description = "User password", example = "admin123", required = true)
  @NotBlank(message = "Password is required")
  @Size(min = 6, message = "Password must be at least 6 characters")
  private String password;

  // Constructors
  public LoginRequest() {}

  public LoginRequest(String username, String password) {
    this.username = username;
    this.password = password;
  }

  // Getters and Setters
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
}
