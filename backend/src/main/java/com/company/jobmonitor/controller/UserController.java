package com.company.jobmonitor.controller;

import com.company.jobmonitor.dto.MessageResponse;
import com.company.jobmonitor.dto.UserDto;
import com.company.jobmonitor.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for user self-service operations.
 *
 * <p>This controller provides endpoints for authenticated users to manage their own profiles and
 * account settings. All endpoints require USER or ADMIN role authorization and operate on the
 * current user's context.
 *
 * <p>Base URL: {@code /api/user}
 *
 * @author JobMonitor Team
 * @version 1.0
 * @since 1.0
 */
@Tag(
    name = "User Self-Service",
    description = "Endpoints for authenticated users to manage their own profiles")
@RestController
@RequestMapping("/api/user")
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  /**
   * Retrieves the current user's profile information.
   *
   * <p>Returns the profile data for the currently authenticated user, determined by the JWT token's
   * principal.
   *
   * @param principal the current user's authentication principal
   * @return ResponseEntity containing the user's profile or 404 if not found
   * @apiNote GET /api/user/profile
   */
  @Operation(
      summary = "Get current user profile",
      description = "Retrieves the profile information of the currently authenticated user.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Profile retrieved successfully",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserDto.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token"),
    @ApiResponse(responseCode = "404", description = "User profile not found")
  })
  @GetMapping("/profile")
  public ResponseEntity<UserDto> getUserProfile(Principal principal) {
    return userService
        .getUserByUsername(principal.getName())
        .map(user -> ResponseEntity.ok(user))
        .orElse(ResponseEntity.notFound().build());
  }

  /**
   * Updates the current user's profile information.
   *
   * <p>Allows users to update their email, first name, and last name. Role information cannot be
   * changed through this endpoint - only administrators can modify user roles.
   *
   * @param principal the current user's authentication principal
   * @param updates a map containing the profile fields to update
   * @return ResponseEntity containing the updated profile or error message
   * @apiNote PUT /api/user/profile
   */
  @Operation(
      summary = "Update current user profile",
      description =
          "Updates the current user's profile information. Users can modify email, first name, and"
              + " last name. Role changes require ADMIN privileges.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Profile updated successfully",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserDto.class))),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid input data",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = MessageResponse.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token"),
    @ApiResponse(responseCode = "404", description = "User not found")
  })
  @PutMapping("/profile")
  public ResponseEntity<?> updateUserProfile(
      Principal principal, @RequestBody Map<String, Object> updates) {
    try {
      String email = (String) updates.get("email");
      String firstName = (String) updates.get("firstName");
      String lastName = (String) updates.get("lastName");

      UserDto userDto =
          userService.updateUserProfile(principal.getName(), email, firstName, lastName);
      return ResponseEntity.ok(userDto);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
    }
  }

  /**
   * Changes the current user's password.
   *
   * <p>Requires the current password for verification before allowing the change. Enforces minimum
   * password length of 6 characters. The new password is automatically encoded before storage.
   *
   * @param principal the current user's authentication principal
   * @param passwordData a map containing currentPassword and newPassword
   * @return ResponseEntity with success message or error details
   * @apiNote POST /api/user/change-password
   */
  @Operation(
      summary = "Change current user password",
      description =
          "Changes the current user's password. Requires current password for verification. New"
              + " password must be at least 6 characters long.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Password changed successfully",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = MessageResponse.class))),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid password data or current password incorrect",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = MessageResponse.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token")
  })
  @PostMapping("/change-password")
  public ResponseEntity<?> changePassword(
      Principal principal, @RequestBody Map<String, String> passwordData) {
    try {
      String currentPassword = passwordData.get("currentPassword");
      String newPassword = passwordData.get("newPassword");

      if (currentPassword == null || newPassword == null) {
        return ResponseEntity.badRequest()
            .body(new MessageResponse("Error: Current and new password are required"));
      }

      if (newPassword.length() < 6) {
        return ResponseEntity.badRequest()
            .body(new MessageResponse("Error: New password must be at least 6 characters long"));
      }

      userService.changePassword(principal.getName(), currentPassword, newPassword);
      return ResponseEntity.ok(new MessageResponse("Password changed successfully"));
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
    }
  }
}
