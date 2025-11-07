package com.company.jobmonitor.controller;

import com.company.jobmonitor.dto.JwtAuthenticationResponse;
import com.company.jobmonitor.dto.LoginRequest;
import com.company.jobmonitor.dto.MessageResponse;
import com.company.jobmonitor.dto.SignUpRequest;
import com.company.jobmonitor.dto.UserDto;
import com.company.jobmonitor.entity.User;
import com.company.jobmonitor.repository.UserRepository;
import com.company.jobmonitor.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

/**
 * REST Controller for authentication and user registration operations.
 *
 * <p>This controller handles user authentication, registration, and current user information
 * retrieval. It provides JWT-based authentication endpoints and supports CORS for cross-origin
 * requests.
 *
 * <p>Base URL: {@code /api/auth}
 *
 * @author JobMonitor Team
 * @version 1.0
 * @since 1.0
 */
@Tag(name = "Authentication", description = "Authentication and user registration endpoints")
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

  /**
   * Authenticates a user and provides JWT token.
   *
   * <p>Validates user credentials and returns a JWT token for subsequent authenticated requests.
   * The token includes user information and expires based on configured timeout settings.
   *
   * @param loginRequest containing username and password
   * @return ResponseEntity with JWT token and user info or error message
   * @apiNote POST /api/auth/login
   */
  @Operation(
      summary = "User authentication",
      description =
          """
          Authenticates a user with username and password credentials.

          Returns a JWT token that must be included in subsequent requests.
          Token format: Authorization: Bearer <token>

          Default users for testing:
          - admin / admin123 (ADMIN role)
          - user / user123 (USER role)
          """)
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Authentication successful",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = JwtAuthenticationResponse.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid credentials",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MessageResponse.class)))
      })
  @PostMapping("/login")
  public ResponseEntity<?> authenticateUser(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "User login credentials",
              required = true,
              content = @Content(schema = @Schema(implementation = LoginRequest.class)))
          @Valid
          @RequestBody
          LoginRequest loginRequest) {
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

  /**
   * Registers a new user account.
   *
   * <p>Creates a new user account with USER role. Validates that username and email are unique in
   * the system. Passwords are automatically encoded before storage.
   *
   * @param signUpRequest containing user registration information
   * @return ResponseEntity with success message or error details
   * @apiNote POST /api/auth/register
   */
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

  /**
   * Retrieves information about the currently authenticated user.
   *
   * <p>Returns user profile information for the currently authenticated user based on the JWT
   * token. Useful for client-side user context and profile display.
   *
   * @param authentication the current authentication context
   * @return ResponseEntity containing current user's UserDto or error message
   * @apiNote GET /api/auth/me
   */
  @Operation(
      summary = "Get current user information",
      description =
          "Retrieves profile information for the currently authenticated user based on JWT token.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved user information",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Not authenticated - missing or invalid JWT token",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MessageResponse.class)))
      })
  @SecurityRequirement(name = "bearerAuth")
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
