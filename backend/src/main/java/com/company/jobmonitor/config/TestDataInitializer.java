package com.company.jobmonitor.config;

import com.company.jobmonitor.entity.User;
import com.company.jobmonitor.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Profile("!prod") // Nur in Development und Test, nicht in Production
public class TestDataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public TestDataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void initTestData() {
        // Check if users already exist
        if (userRepository.count() > 0) {
            System.out.println("Users already exist, skipping test data initialization");
            return;
        }

        System.out.println("Creating test users...");

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
        admin.setCreatedBy("system");
        
        userRepository.save(admin);
        System.out.println("Created admin user: admin / admin123");

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
        testUser.setCreatedBy("system");
        
        userRepository.save(testUser);
        System.out.println("Created test user: user / user123");

        // Create another test user 
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
        testUser2.setCreatedBy("system");
        
        userRepository.save(testUser2);
        System.out.println("Created test user: testuser / testpassword");

        System.out.println("Test data initialization completed!");
    }
}