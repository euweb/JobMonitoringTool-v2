package com.company.jobmonitor.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.company.jobmonitor.dto.UserDto;
import com.company.jobmonitor.entity.User;
import com.company.jobmonitor.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock private UserRepository userRepository;

  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private UserService userService;

  private User testUser;
  private UserDto testUserDto;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId(1);
    testUser.setUsername("testuser");
    testUser.setEmail("test@example.com");
    testUser.setFirstName("Test");
    testUser.setLastName("User");
    testUser.setRole(User.Role.USER);
    testUser.setEnabled(true);
    testUser.setCreatedAt(LocalDateTime.now());
    testUser.setUpdatedAt(LocalDateTime.now());
    testUser.setCreatedBy("admin");
    testUser.setUpdatedBy("admin");
    testUser.setVersion(1);
    testUser.setAccountNonExpired(true);
    testUser.setAccountNonLocked(true);
    testUser.setCredentialsNonExpired(true);

    testUserDto = new UserDto();
    testUserDto.setId(1);
    testUserDto.setUsername("testuser");
    testUserDto.setEmail("test@example.com");
    testUserDto.setFirstName("Test");
    testUserDto.setLastName("User");
    testUserDto.setRole(User.Role.USER);
    testUserDto.setEnabled(true);
  }

  @Test
  void getAllUsers_ShouldReturnListOfUserDtos() {
    // Given
    List<User> users = Arrays.asList(testUser);
    when(userRepository.findAll()).thenReturn(users);

    // When
    List<UserDto> result = userService.getAllUsers();

    // Then
    assertEquals(1, result.size());
    assertEquals(testUser.getId(), result.get(0).getId());
    assertEquals(testUser.getUsername(), result.get(0).getUsername());
    assertEquals(testUser.getEmail(), result.get(0).getEmail());
    verify(userRepository).findAll();
  }

  @Test
  void getUserById_WhenUserExists_ShouldReturnUserDto() {
    // Given
    when(userRepository.findById(1)).thenReturn(Optional.of(testUser));

    // When
    Optional<UserDto> result = userService.getUserById(1);

    // Then
    assertTrue(result.isPresent());
    assertEquals(testUser.getId(), result.get().getId());
    assertEquals(testUser.getUsername(), result.get().getUsername());
    verify(userRepository).findById(1);
  }

  @Test
  void getUserById_WhenUserDoesNotExist_ShouldReturnEmpty() {
    // Given
    when(userRepository.findById(1)).thenReturn(Optional.empty());

    // When
    Optional<UserDto> result = userService.getUserById(1);

    // Then
    assertTrue(result.isEmpty());
    verify(userRepository).findById(1);
  }

  @Test
  void getUserCount_ShouldReturnCorrectCount() {
    // Given
    when(userRepository.count()).thenReturn(5L);

    // When
    long result = userService.getUserCount();

    // Then
    assertEquals(5L, result);
    verify(userRepository).count();
  }

  @Test
  void updateUser_ShouldUpdateAndReturnUserDto() {
    // Given
    when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    // When
    UserDto result = userService.updateUser(1, "new@example.com", "New", "Name", User.Role.ADMIN);

    // Then
    assertEquals("new@example.com", result.getEmail());
    assertEquals("New", result.getFirstName());
    assertEquals("Name", result.getLastName());
    assertEquals(User.Role.ADMIN, result.getRole());
    verify(userRepository).findById(1);
    verify(userRepository).save(any(User.class));
  }

  @Test
  void updateUser_WhenUserDoesNotExist_ShouldThrowException() {
    // Given
    when(userRepository.findById(1)).thenReturn(Optional.empty());

    // When & Then
    assertThrows(
        RuntimeException.class,
        () -> {
          userService.updateUser(1, "new@example.com", "New", "Name", User.Role.ADMIN);
        });
    verify(userRepository).findById(1);
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void toggleUserEnabled_ShouldToggleEnabledStatus() {
    // Given
    testUser.setEnabled(true);
    when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    // When
    userService.toggleUserEnabled(1);

    // Then
    verify(userRepository).findById(1);
    verify(userRepository).save(argThat(user -> !user.isEnabled()));
  }

  @Test
  void deleteUser_WhenUserExists_ShouldDeleteUser() {
    // Given
    when(userRepository.findById(1)).thenReturn(Optional.of(testUser));

    // When
    userService.deleteUser(1);

    // Then
    verify(userRepository).findById(1);
    verify(userRepository).delete(testUser);
  }

  @Test
  void deleteUser_WhenUserDoesNotExist_ShouldThrowException() {
    // Given
    when(userRepository.findById(1)).thenReturn(Optional.empty());

    // When & Then
    assertThrows(
        RuntimeException.class,
        () -> {
          userService.deleteUser(1);
        });
    verify(userRepository).findById(1);
    verify(userRepository, never()).delete(any(User.class));
  }
}
