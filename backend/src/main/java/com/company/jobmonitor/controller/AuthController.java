package com.company.jobmonitor.controller;

import com.company.jobmonitor.dto.JwtAuthenticationResponse;
import com.company.jobmonitor.dto.LoginRequest;
import com.company.jobmonitor.dto.UserDto;
import com.company.jobmonitor.entity.RefreshToken;
import com.company.jobmonitor.entity.User;
import com.company.jobmonitor.repository.UserRepository;
import com.company.jobmonitor.security.JwtTokenProvider;
import com.company.jobmonitor.service.CustomUserDetailsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;

    public AuthController(AuthenticationManager authenticationManager,
                         UserRepository userRepository,
                         PasswordEncoder passwordEncoder,
                         JwtTokenProvider tokenProvider,
                         CustomUserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String jwt = tokenProvider.generateToken(userDetails);
            
            // Get user details for response
            Optional<User> userOpt = userRepository.findByUsername(loginRequest.getUsername());
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: User not found!"));
            }

            User user = userOpt.get();
            UserDto userDto = UserDto.from(user);

            return ResponseEntity.ok(new JwtAuthenticationResponse(jwt, "Bearer", userDto));
        } catch (AuthenticationException e) {
            return ResponseEntity.badRequest()
                .body(new MessageResponse("Error: Invalid username or password!"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest()
                .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest()
                .body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create new user account
        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setPasswordHash(passwordEncoder.encode(signUpRequest.getPassword()));
        user.setFirstName(signUpRequest.getFirstName());
        user.setLastName(signUpRequest.getLastName());
        user.setRole(User.Role.USER);
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);

        User result = userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.badRequest()
                .body(new MessageResponse("Error: Not authenticated!"));
        }

        String username = authentication.getName();
        Optional<User> userOpt = userRepository.findByUsername(username);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                .body(new MessageResponse("Error: User not found!"));
        }

        User user = userOpt.get();
        UserDto userDto = UserDto.from(user);
        
        return ResponseEntity.ok(userDto);
    }

    // Helper classes for requests and responses
    public static class SignUpRequest {
        private String username;
        private String email;
        private String password;
        private String firstName;
        private String lastName;

        // Getters and setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
    }

    public static class MessageResponse {
        private String message;

        public MessageResponse(String message) {
            this.message = message;
        }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}