package com.company.jobmonitor.controller;

import com.company.jobmonitor.entity.User;
import com.company.jobmonitor.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/setup")
public class SetupController {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public SetupController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @PostMapping("/init-users")
  public ResponseEntity<Map<String, String>> initUsers() {
    Map<String, String> response = new HashMap<>();

    try {
      // Clear existing users
      userRepository.deleteAll();

      // Generate fresh hashes
      String admin123Hash = passwordEncoder.encode("admin123");
      String user123Hash = passwordEncoder.encode("user123");
      String testPasswordHash = passwordEncoder.encode("testpassword");

      // Log the hashes for debugging
      System.out.println("Generated hashes:");
      System.out.println("admin123: " + admin123Hash);
      System.out.println("user123: " + user123Hash);
      System.out.println("testpassword: " + testPasswordHash);

      // Create admin user
      User admin = new User();
      admin.setUsername("admin");
      admin.setPasswordHash(admin123Hash); // Use the generated hash
      admin.setEmail("admin@jobmonitor.com");
      admin.setFirstName("System");
      admin.setLastName("Administrator");
      admin.setRole(User.Role.ADMIN);
      admin.setEnabled(true);
      admin.setAccountNonExpired(true);
      admin.setAccountNonLocked(true);
      admin.setCredentialsNonExpired(true);
      admin.setCreatedAt(LocalDateTime.now());
      admin.setCreatedBy("setup");

      userRepository.save(admin);
      response.put("admin", "Created: admin / admin123");

      // Create test user
      User testUser = new User();
      testUser.setUsername("user");
      testUser.setPasswordHash(passwordEncoder.encode("user123"));
      testUser.setEmail("user@jobmonitor.com");
      testUser.setFirstName("Test");
      testUser.setLastName("User");
      testUser.setRole(User.Role.USER);
      testUser.setEnabled(true);
      testUser.setAccountNonExpired(true);
      testUser.setAccountNonLocked(true);
      testUser.setCredentialsNonExpired(true);
      testUser.setCreatedAt(LocalDateTime.now());
      testUser.setCreatedBy("setup");

      userRepository.save(testUser);
      response.put("user", "Created: user / user123");

      // Create testuser
      User testUser2 = new User();
      testUser2.setUsername("testuser");
      testUser2.setPasswordHash(passwordEncoder.encode("testpassword"));
      testUser2.setEmail("test@jobmonitor.com");
      testUser2.setFirstName("Test");
      testUser2.setLastName("User2");
      testUser2.setRole(User.Role.USER);
      testUser2.setEnabled(true);
      testUser2.setAccountNonExpired(true);
      testUser2.setAccountNonLocked(true);
      testUser2.setCredentialsNonExpired(true);
      testUser2.setCreatedAt(LocalDateTime.now());
      testUser2.setCreatedBy("setup");

      userRepository.save(testUser2);
      response.put("testuser", "Created: testuser / testpassword");

      response.put("status", "success");
      response.put("message", "All test users created successfully!");

    } catch (Exception e) {
      response.put("status", "error");
      response.put("message", "Error creating users: " + e.getMessage());
    }

    return ResponseEntity.ok(response);
  }

  @PostMapping("/generate-hash")
  public ResponseEntity<String> generateHash(
      @org.springframework.web.bind.annotation.RequestParam String password) {
    String hash = passwordEncoder.encode(password);
    System.out.println("Password: " + password + " -> Hash: " + hash);
    return ResponseEntity.ok("Password: " + password + "\nHash: " + hash);
  }

  @PostMapping("/test-password")
  public ResponseEntity<String> testPassword(
      @org.springframework.web.bind.annotation.RequestParam String password,
      @org.springframework.web.bind.annotation.RequestParam String hash) {
    boolean matches = passwordEncoder.matches(password, hash);
    return ResponseEntity.ok("Password '" + password + "' matches hash: " + matches);
  }
}
