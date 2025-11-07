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

@Service
@Transactional
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public List<UserDto> getAllUsers() {
    return userRepository.findAll().stream().map(UserDto::from).collect(Collectors.toList());
  }

  public Optional<UserDto> getUserById(Integer id) {
    return userRepository.findById(id).map(UserDto::from);
  }

  public Optional<UserDto> getUserByUsername(String username) {
    return userRepository.findByUsername(username).map(UserDto::from);
  }

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

  public void deleteUser(Integer id) {
    User user =
        userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

    userRepository.delete(user);
  }

  public void toggleUserEnabled(Integer id) {
    User user =
        userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

    user.setEnabled(!user.isEnabled());
    userRepository.save(user);
  }

  public long getUserCount() {
    return userRepository.count();
  }

  public boolean hasUsers() {
    return userRepository.count() > 0;
  }
}
