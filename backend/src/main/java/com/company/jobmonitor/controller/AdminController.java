package com.company.jobmonitor.controller;

import com.company.jobmonitor.dto.MessageResponse;
import com.company.jobmonitor.dto.SignUpRequest;
import com.company.jobmonitor.dto.UserDto;
import com.company.jobmonitor.entity.User;
import com.company.jobmonitor.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for administrative user management operations.
 *
 * <p>This controller provides endpoints for admin users to manage other users in the system. All
 * endpoints require ADMIN role authorization and provide full CRUD operations for user management.
 *
 * <p>Base URL: {@code /api/admin}
 *
 * @author JobMonitor Team
 * @version 1.0
 * @since 1.0
 */
@Tag(name = "Admin Management", description = "Administrative operations for user management")
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

  private final UserService userService;

  public AdminController(UserService userService) {
    this.userService = userService;
  }

  /**
   * Retrieves all users in the system.
   *
   * @return ResponseEntity containing a list of all UserDto objects
   * @apiNote GET /api/admin/users
   */
  @Operation(
      summary = "Get all users",
      description = "Retrieves a list of all users in the system. Requires ADMIN role.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved users list",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - invalid or missing JWT token",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - requires ADMIN role",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MessageResponse.class)))
      })
  @GetMapping("/users")
  public ResponseEntity<List<UserDto>> getAllUsers() {
    List<UserDto> users = userService.getAllUsers();
    return ResponseEntity.ok(users);
  }

  /**
   * Retrieves a specific user by their ID.
   *
   * @param id the unique identifier of the user
   * @return ResponseEntity containing the UserDto if found, 404 Not Found otherwise
   * @apiNote GET /api/admin/users/{id}
   */
  @Operation(
      summary = "Get user by ID",
      description = "Retrieves a specific user by their unique identifier. Requires ADMIN role.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved user",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role")
      })
  @GetMapping("/users/{id}")
  public ResponseEntity<UserDto> getUserById(
      @Parameter(description = "User ID", required = true, example = "1") @PathVariable("id")
          Integer id) {
    return userService
        .getUserById(id)
        .map(user -> ResponseEntity.ok(user))
        .orElse(ResponseEntity.notFound().build());
  }

  /**
   * Creates a new regular user account.
   *
   * <p>Creates a new user with USER role. For creating admin accounts, use the {@link
   * #createAdminUser(SignUpRequest)} endpoint instead.
   *
   * @param signUpRequest the user registration data
   * @return ResponseEntity containing the created UserDto or error message
   * @apiNote POST /api/admin/users
   */
  @Operation(
      summary = "Create new user",
      description = "Creates a new user account with USER role. Requires ADMIN role.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "User created successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input or user already exists",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role")
      })
  @PostMapping("/users")
  public ResponseEntity<?> createUser(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "User registration data",
              required = true,
              content = @Content(schema = @Schema(implementation = SignUpRequest.class)))
          @Valid
          @RequestBody
          SignUpRequest signUpRequest) {
    try {
      UserDto userDto =
          userService.createUser(
              signUpRequest.getUsername(),
              signUpRequest.getEmail(),
              signUpRequest.getPassword(),
              signUpRequest.getFirstName(),
              signUpRequest.getLastName(),
              User.Role.USER // New users created by admin are regular users by default
              );
      return ResponseEntity.ok(userDto);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
    }
  }

  /**
   * Creates a new admin user account.
   *
   * <p>Creates a new user with ADMIN role, granting full system access. Use this endpoint carefully
   * as it creates privileged accounts.
   *
   * @param signUpRequest the admin user registration data
   * @return ResponseEntity containing the created UserDto or error message
   * @apiNote POST /api/admin/users/admin
   */
  @Operation(
      summary = "Create new admin user",
      description =
          "Creates a new user account with ADMIN role. Use with caution as this grants full system"
              + " access. Requires ADMIN role.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Admin user created successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input or user already exists",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role")
      })
  @PostMapping("/users/admin")
  public ResponseEntity<?> createAdminUser(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Admin user registration data",
              required = true,
              content = @Content(schema = @Schema(implementation = SignUpRequest.class)))
          @Valid
          @RequestBody
          SignUpRequest signUpRequest) {
    try {
      UserDto userDto =
          userService.createUser(
              signUpRequest.getUsername(),
              signUpRequest.getEmail(),
              signUpRequest.getPassword(),
              signUpRequest.getFirstName(),
              signUpRequest.getLastName(),
              User.Role.ADMIN);
      return ResponseEntity.ok(userDto);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
    }
  }

  /**
   * Updates an existing user's information.
   *
   * <p>Allows updating email, first name, last name, and role of an existing user. All fields are
   * optional in the request body.
   *
   * @param id the unique identifier of the user to update
   * @param updates a map containing the fields to update
   * @return ResponseEntity containing the updated UserDto or error message
   * @apiNote PUT /api/admin/users/{id}
   */
  @Operation(
      summary = "Update user information",
      description =
          "Updates an existing user's profile information. All fields in the request body are"
              + " optional. Requires ADMIN role.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "User updated successfully",
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
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role"),
        @ApiResponse(responseCode = "404", description = "User not found")
      })
  @PutMapping("/users/{id}")
  public ResponseEntity<?> updateUser(
      @Parameter(description = "User ID", required = true, example = "1") @PathVariable("id")
          Integer id,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Map containing fields to update (email, firstName, lastName, role)",
              required = true)
          @RequestBody
          Map<String, Object> updates) {
    try {
      String email = (String) updates.get("email");
      String firstName = (String) updates.get("firstName");
      String lastName = (String) updates.get("lastName");
      String roleStr = (String) updates.get("role");
      User.Role role = User.Role.valueOf(roleStr);

      UserDto userDto = userService.updateUser(id, email, firstName, lastName, role);
      return ResponseEntity.ok(userDto);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
    }
  }

  /**
   * Toggles a user's enabled/disabled status.
   *
   * <p>This endpoint provides a safe way to activate or deactivate user accounts without permanent
   * deletion. Disabled users cannot log in but retain their data.
   *
   * @param id the unique identifier of the user to toggle
   * @return ResponseEntity containing success message or error message
   * @apiNote POST /api/admin/users/{id}/toggle-enabled
   */
  @Operation(
      summary = "Toggle user enabled status",
      description =
          "Enables or disables a user account. Disabled users cannot log in but retain their data."
              + " Requires ADMIN role.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "User status updated successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Error updating user status",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role"),
        @ApiResponse(responseCode = "404", description = "User not found")
      })
  @PostMapping("/users/{id}/toggle-enabled")
  public ResponseEntity<?> toggleUserEnabled(
      @Parameter(description = "User ID", required = true, example = "1") @PathVariable("id")
          Integer id) {
    try {
      userService.toggleUserEnabled(id);
      return ResponseEntity.ok(new MessageResponse("User status updated successfully"));
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
    }
  }

  /**
   * Permanently deletes a user from the system.
   *
   * <p><strong>Warning:</strong> This operation permanently removes the user and cannot be undone.
   * Consider using {@link #toggleUserEnabled(Integer)} instead to preserve audit trails while
   * disabling access.
   *
   * @param id the unique identifier of the user to delete
   * @return ResponseEntity containing success message or error message
   * @apiNote DELETE /api/admin/users/{id}
   */
  @Operation(
      summary = "Delete user permanently",
      description =
          "WARNING: Permanently deletes a user from the system. This action cannot be undone."
              + " Consider disabling the user instead. Requires ADMIN role.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "User deleted successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Error deleting user",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role"),
        @ApiResponse(responseCode = "404", description = "User not found")
      })
  @DeleteMapping("/users/{id}")
  public ResponseEntity<?> deleteUser(
      @Parameter(description = "User ID", required = true, example = "1") @PathVariable("id")
          Integer id) {
    try {
      userService.deleteUser(id);
      return ResponseEntity.ok(new MessageResponse("User deleted successfully"));
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
    }
  }

  /**
   * Retrieves system statistics for administrative dashboard.
   *
   * <p>Provides key metrics including total user count and overall system status for monitoring and
   * administrative purposes.
   *
   * @return ResponseEntity containing a map of system statistics
   * @apiNote GET /api/admin/stats
   */
  @Operation(
      summary = "Get admin statistics",
      description =
          "Retrieves system statistics including user counts and system status. Requires ADMIN"
              + " role.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
    @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token"),
    @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role")
  })
  @GetMapping("/stats")
  public ResponseEntity<Map<String, Object>> getAdminStats() {
    Map<String, Object> stats =
        Map.of("totalUsers", userService.getUserCount(), "systemStatus", "Operational");
    return ResponseEntity.ok(stats);
  }
}
