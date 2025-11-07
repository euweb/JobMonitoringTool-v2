package com.company.jobmonitor.controller;

import com.company.jobmonitor.dto.MessageResponse;
import com.company.jobmonitor.dto.UserDto;
import com.company.jobmonitor.service.UserService;
import java.security.Principal;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/profile")
  public ResponseEntity<UserDto> getUserProfile(Principal principal) {
    return userService
        .getUserByUsername(principal.getName())
        .map(user -> ResponseEntity.ok(user))
        .orElse(ResponseEntity.notFound().build());
  }

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
