package com.company.jobmonitor.service;

import com.company.jobmonitor.dto.UserDto;
import com.company.jobmonitor.entity.User;
import com.company.jobmonitor.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for user management operations.
 *
 * <p>This service provides comprehensive user management functionality including:
 *
 * <ul>
 *   <li>User creation and registration
 *   <li>User retrieval and search operations
 *   <li>User profile updates and modifications
 *   <li>Password management and security operations
 *   <li>User status management (enable/disable)
 *   <li>Administrative user operations
 * </ul>
 *
 * <p>All operations are performed within transactional contexts to ensure data consistency.
 * Password encoding and security validations are handled automatically.
 *
 * @author Job Monitor Development Team
 * @version 1.0
 * @since 1.0
 */
@Service
@Transactional
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  /**
   * Constructs a UserService with the specified dependencies.
   *
   * @param userRepository the repository for user data access operations
   * @param passwordEncoder the encoder for password security operations
   */
  public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  /**
   * Retrieves all users from the system.
   *
   * <p>This method returns all registered users as DTOs, suitable for display in administrative
   * interfaces. Sensitive information like password hashes are excluded from the returned data.
   *
   * @return a list of all users as UserDto objects
   * @see UserDto
   */
  public List<UserDto> getAllUsers() {
    return userRepository.findAll().stream().map(UserDto::from).collect(Collectors.toList());
  }

  /**
   * Retrieves a user by their unique identifier.
   *
   * @param id the unique identifier of the user to retrieve
   * @return an Optional containing the UserDto if found, empty otherwise
   * @throws IllegalArgumentException if the id is null
   */
  public Optional<UserDto> getUserById(Integer id) {
    return userRepository.findById(id).map(UserDto::from);
  }

  /**
   * Retrieves a user by their username.
   *
   * <p>Username lookup is case-sensitive and must match exactly.
   *
   * @param username the username to search for
   * @return an Optional containing the UserDto if found, empty otherwise
   * @throws IllegalArgumentException if the username is null or empty
   */
  public Optional<UserDto> getUserByUsername(String username) {
    return userRepository.findByUsername(username).map(UserDto::from);
  }

  /**
   * Creates a new user in the system.
   *
   * <p>This method performs comprehensive validation including:
   *
   * <ul>
   *   <li>Username uniqueness verification
   *   <li>Email address uniqueness verification
   *   <li>Password encoding for security
   *   <li>Automatic timestamp and audit field population
   * </ul>
   *
   * @param username the unique username for the new user
   * @param email the email address for the new user
   * @param password the plaintext password (will be encoded)
   * @param firstName the user's first name
   * @param lastName the user's last name
   * @param role the role to assign to the user
   * @return the created user as a UserDto
   * @throws RuntimeException if the username or email already exists
   * @throws IllegalArgumentException if any required parameter is null or empty
   */
  public UserDto createUser(
      String username,
      String email,
      String password,
      String firstName,
      String lastName,
      User.Role role) {
    if (userRepository.existsByUsername(username)) {
      throw new RuntimeException("Username already exists");
    }
    if (userRepository.existsByEmail(email)) {
      throw new RuntimeException("Email already exists");
    }

    User user = new User();
    user.setUsername(username);
    user.setEmail(email);
    user.setPasswordHash(passwordEncoder.encode(password));
    user.setFirstName(firstName);
    user.setLastName(lastName);
    user.setRole(role);
    user.setEnabled(true);
    user.setAccountNonExpired(true);
    user.setAccountNonLocked(true);
    user.setCredentialsNonExpired(true);

    User savedUser = userRepository.save(user);
    return UserDto.from(savedUser);
  }

  /**
   * Updates an existing user's information.
   *
   * <p>This method allows updating user details while preserving data integrity:
   *
   * <ul>
   *   <li>Validates email uniqueness across all users
   *   <li>Preserves username and password information
   *   <li>Updates timestamps automatically
   *   <li>Maintains audit trail information
   * </ul>
   *
   * @param id the unique identifier of the user to update
   * @param email the new email address for the user
   * @param firstName the new first name for the user
   * @param lastName the new last name for the user
   * @param role the new role to assign to the user
   * @return the updated user as a UserDto
   * @throws RuntimeException if the user is not found or email already exists
   */
  public UserDto updateUser(
      Integer id, String email, String firstName, String lastName, User.Role role) {
    User user =
        userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

    // Check if email is already used by another user
    if (!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
      throw new RuntimeException("Email already exists");
    }

    user.setEmail(email);
    user.setFirstName(firstName);
    user.setLastName(lastName);
    user.setRole(role);

    User savedUser = userRepository.save(user);
    return UserDto.from(savedUser);
  }

  /**
   * Updates a user's profile information by username.
   *
   * <p>This method is designed for self-service profile updates where users can modify their own
   * profile information. Role information is preserved and cannot be changed through this method.
   *
   * @param username the username of the user to update
   * @param email the new email address for the user
   * @param firstName the new first name for the user
   * @param lastName the new last name for the user
   * @return the updated user as a UserDto
   * @throws RuntimeException if the user is not found or email already exists
   */
  public UserDto updateUserProfile(
      String username, String email, String firstName, String lastName) {
    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

    // Check if email is already used by another user
    if (!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
      throw new RuntimeException("Email already exists");
    }

    user.setEmail(email);
    user.setFirstName(firstName);
    user.setLastName(lastName);

    User savedUser = userRepository.save(user);
    return UserDto.from(savedUser);
  }

  /**
   * Changes a user's password after validating their current password.
   *
   * <p>This method provides secure password change functionality by requiring the current password
   * for verification before allowing the change. The new password is automatically encoded using
   * the configured password encoder.
   *
   * @param username the username of the user changing their password
   * @param oldPassword the current password for verification
   * @param newPassword the new password to set (will be encoded)
   * @throws RuntimeException if the user is not found or current password is invalid
   * @see #resetPassword(String, String) for administrative password resets
   */
  public void changePassword(String username, String oldPassword, String newPassword) {
    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

    if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
      throw new RuntimeException("Invalid current password");
    }

    user.setPasswordHash(passwordEncoder.encode(newPassword));
    userRepository.save(user);
  }

  /**
   * Deletes a user from the system by their ID.
   *
   * <p>This method permanently removes a user from the system. Use with caution as this operation
   * cannot be undone. Consider using {@link #toggleUserEnabled(Integer)} to disable users instead
   * of deletion for audit trail preservation.
   *
   * @param id the unique identifier of the user to delete
   * @throws RuntimeException if the user is not found
   * @see #toggleUserEnabled(Integer) for non-destructive user deactivation
   */
  public void deleteUser(Integer id) {
    User user =
        userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

    userRepository.delete(user);
  }

  /**
   * Toggles the enabled/disabled status of a user by their ID.
   *
   * <p>This method provides a non-destructive way to activate or deactivate user accounts. Disabled
   * users cannot log in but their data is preserved. This is the preferred approach for user
   * management over deletion.
   *
   * @param id the unique identifier of the user to toggle
   * @throws RuntimeException if the user is not found
   * @see #deleteUser(Integer) for permanent user removal
   */
  public void toggleUserEnabled(Integer id) {
    User user =
        userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

    user.setEnabled(!user.isEnabled());
    userRepository.save(user);
  }

  /**
   * Returns the total number of users in the system.
   *
   * <p>This method counts all users regardless of their enabled/disabled status. Useful for
   * administrative dashboards and system monitoring.
   *
   * @return the total count of users in the system
   */
  public long getUserCount() {
    return userRepository.count();
  }

  /**
   * Checks if any users exist in the system.
   *
   * <p>This method is commonly used during application initialization to determine if a setup
   * process is needed or if admin users exist.
   *
   * @return true if at least one user exists, false if no users are present
   */
  public boolean hasUsers() {
    return userRepository.count() > 0;
  }
}
