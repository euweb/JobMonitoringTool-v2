package com.company.jobmonitor.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Data Transfer Object for JWT authentication responses.
 *
 * <p>Contains the JWT tokens and user information returned after successful authentication. Used as
 * the response for POST /api/auth/login endpoint.
 *
 * @author JobMonitor Team
 * @version 1.0
 * @since 1.0
 */
@Schema(description = "JWT authentication response containing tokens and user information")
public class JwtAuthenticationResponse {

  @Schema(
      description = "JWT access token for API authentication",
      example = "eyJhbGciOiJIUzUxMiJ9...")
  private String accessToken;

  @Schema(
      description = "JWT refresh token for obtaining new access tokens",
      example = "eyJhbGciOiJIUzUxMiJ9...")
  private String refreshToken;

  @Schema(description = "Token type identifier", example = "Bearer")
  private String tokenType = "Bearer";

  @Schema(description = "Token expiration time in milliseconds", example = "3600000")
  private long expiresIn;

  @Schema(description = "Authenticated user information")
  private UserDto user;

  // Constructors
  public JwtAuthenticationResponse() {}

  public JwtAuthenticationResponse(String accessToken, String refreshToken, UserDto user) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.tokenType = "Bearer";
    this.user = user;
  }

  public JwtAuthenticationResponse(
      String accessToken, String refreshToken, String tokenType, long expiresIn, UserDto user) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.tokenType = tokenType != null ? tokenType : "Bearer";
    this.expiresIn = expiresIn;
    this.user = user;
  }

  // Getters and Setters
  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public String getTokenType() {
    return tokenType;
  }

  public void setTokenType(String tokenType) {
    this.tokenType = tokenType;
  }

  public long getExpiresIn() {
    return expiresIn;
  }

  public void setExpiresIn(long expiresIn) {
    this.expiresIn = expiresIn;
  }

  public UserDto getUser() {
    return user;
  }

  public void setUser(UserDto user) {
    this.user = user;
  }
}
