package com.company.jobmonitor.controller;

import com.company.jobmonitor.dto.MessageResponse;
import com.company.jobmonitor.dto.SignUpRequest;
import com.company.jobmonitor.dto.UserDto;
import com.company.jobmonitor.entity.User;
import com.company.jobmonitor.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

  private final UserService userService;

  public AdminController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/users")
  public ResponseEntity<List<UserDto>> getAllUsers() {
    List<UserDto> users = userService.getAllUsers();
    return ResponseEntity.ok(users);
  }

  @GetMapping("/users/{id}")
  public ResponseEntity<UserDto> getUserById(@PathVariable Integer id) {
    return userService
        .getUserById(id)
        .map(user -> ResponseEntity.ok(user))
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping("/users")
  public ResponseEntity<?> createUser(@Valid @RequestBody SignUpRequest signUpRequest) {
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

  @PostMapping("/users/admin")
  public ResponseEntity<?> createAdminUser(@Valid @RequestBody SignUpRequest signUpRequest) {
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

  @PutMapping("/users/{id}")
  public ResponseEntity<?> updateUser(
      @PathVariable Integer id, @RequestBody Map<String, Object> updates) {
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

  @PostMapping("/users/{id}/toggle-enabled")
  public ResponseEntity<?> toggleUserEnabled(@PathVariable Integer id) {
    try {
      userService.toggleUserEnabled(id);
      return ResponseEntity.ok(new MessageResponse("User status updated successfully"));
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
    }
  }

  @DeleteMapping("/users/{id}")
  public ResponseEntity<?> deleteUser(@PathVariable Integer id) {
    try {
      userService.deleteUser(id);
      return ResponseEntity.ok(new MessageResponse("User deleted successfully"));
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
    }
  }

  @GetMapping("/stats")
  public ResponseEntity<Map<String, Object>> getAdminStats() {
    Map<String, Object> stats =
        Map.of("totalUsers", userService.getUserCount(), "systemStatus", "Operational");
    return ResponseEntity.ok(stats);
  }
}
