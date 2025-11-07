package com.company.jobmonitor.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.company.jobmonitor.dto.UserDto;
import com.company.jobmonitor.entity.User;
import com.company.jobmonitor.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminController.class)
@ContextConfiguration(classes = {AdminController.class})
@Import(TestSecurityConfig.class)
class AdminControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private UserService userService;

  @Autowired private ObjectMapper objectMapper;

  private UserDto testUserDto;

  @BeforeEach
  void setUp() {
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
  @WithMockUser(roles = "ADMIN")
  void getAllUsers_ShouldReturnListOfUsers() throws Exception {
    // Given
    List<UserDto> users = Arrays.asList(testUserDto);
    when(userService.getAllUsers()).thenReturn(users);

    // When & Then
    mockMvc
        .perform(get("/api/admin/users"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].username").value("testuser"))
        .andExpect(jsonPath("$[0].email").value("test@example.com"));
  }

  @Test
  void getAllUsers_WithoutAdminRole_ShouldReturnForbidden() throws Exception {
    mockMvc.perform(get("/api/admin/users")).andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(roles = "USER")
  void getAllUsers_WithUserRole_ShouldReturnForbidden() throws Exception {
    mockMvc.perform(get("/api/admin/users")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void getUserById_WhenUserExists_ShouldReturnUser() throws Exception {
    // Given
    when(userService.getUserById(1)).thenReturn(Optional.of(testUserDto));

    // When & Then
    mockMvc
        .perform(get("/api/admin/users/1"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.username").value("testuser"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void getUserById_WhenUserDoesNotExist_ShouldReturnNotFound() throws Exception {
    // Given
    when(userService.getUserById(1)).thenReturn(Optional.empty());

    // When & Then
    mockMvc.perform(get("/api/admin/users/1")).andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void updateUser_WithValidData_ShouldReturnUpdatedUser() throws Exception {
    // Given
    Map<String, Object> updateData = new HashMap<>();
    updateData.put("email", "updated@example.com");
    updateData.put("firstName", "Updated");
    updateData.put("lastName", "User");
    updateData.put("role", "ADMIN");

    UserDto updatedUser = new UserDto();
    updatedUser.setId(1);
    updatedUser.setEmail("updated@example.com");
    updatedUser.setFirstName("Updated");
    updatedUser.setLastName("User");
    updatedUser.setRole(User.Role.ADMIN);

    when(userService.updateUser(
            eq(1), eq("updated@example.com"), eq("Updated"), eq("User"), eq(User.Role.ADMIN)))
        .thenReturn(updatedUser);

    // When & Then
    mockMvc
        .perform(
            put("/api/admin/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateData)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.email").value("updated@example.com"))
        .andExpect(jsonPath("$.firstName").value("Updated"))
        .andExpect(jsonPath("$.role").value("ADMIN"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void deleteUser_WhenUserExists_ShouldReturnSuccess() throws Exception {
    // Given
    // userService.deleteUser() doesn't throw exception

    // When & Then
    mockMvc
        .perform(delete("/api/admin/users/1"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.message").value("User deleted successfully"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void getAdminStats_ShouldReturnStats() throws Exception {
    // Given
    when(userService.getUserCount()).thenReturn(5L);

    // When & Then
    mockMvc
        .perform(get("/api/admin/stats"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.totalUsers").value(5))
        .andExpect(jsonPath("$.systemStatus").value("Operational"));
  }
}
