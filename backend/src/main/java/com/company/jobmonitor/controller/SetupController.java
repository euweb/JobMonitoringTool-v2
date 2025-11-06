package com.company.jobmonitor.controller;

import com.company.jobmonitor.entity.User;
import com.company.jobmonitor.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

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
            // Create admin user
            User admin = new User();
            admin.setUsername("admin");
            admin.setPasswordHash(passwordEncoder.encode("admin123"));
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
}