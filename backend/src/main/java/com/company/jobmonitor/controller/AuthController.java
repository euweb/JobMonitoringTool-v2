package com.company.jobmonitor.controller;

import com.company.jobmonitor.dto.JwtAuthenticationResponse;
import com.company.jobmonitor.dto.LoginRequest;
import com.company.jobmonitor.dto.MessageResponse;
import com.company.jobmonitor.dto.SignUpRequest;
import com.company.jobmonitor.dto.UserDto;
import com.company.jobmonitor.entity.User;
import com.company.jobmonitor.repository.UserRepository;
import com.company.jobmonitor.security.JwtTokenProvider;
import jakarta.validation.Valid;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

  private final AuthenticationManager authenticationManager;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider tokenProvider;

  public AuthController(
      AuthenticationManager authenticationManager,
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      JwtTokenProvider tokenProvider) {
    this.authenticationManager = authenticationManager;
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.tokenProvider = tokenProvider;
  }

  @PostMapping("/login")
  public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
    try {
      Authentication authentication =
          authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(
                  loginRequest.getUsername(), loginRequest.getPassword()));

      UserDetails userDetails = (UserDetails) authentication.getPrincipal();
      String jwt = tokenProvider.generateToken(userDetails);

      // Get user details for response
      Optional<User> userOpt = userRepository.findByUsername(loginRequest.getUsername());
      if (userOpt.isEmpty()) {
        return ResponseEntity.badRequest().body(new MessageResponse("Error: User not found!"));
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

    userRepository.save(user);

    return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
  }

  @GetMapping("/me")
  public ResponseEntity<?> getCurrentUser(Authentication authentication) {
    if (authentication == null) {
      return ResponseEntity.badRequest().body(new MessageResponse("Error: Not authenticated!"));
    }

    String username = authentication.getName();
    Optional<User> userOpt = userRepository.findByUsername(username);

    if (userOpt.isEmpty()) {
      return ResponseEntity.badRequest().body(new MessageResponse("Error: User not found!"));
    }

    User user = userOpt.get();
    UserDto userDto = UserDto.from(user);

    return ResponseEntity.ok(userDto);
  }
}
