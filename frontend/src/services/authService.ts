import {
  LoginRequest,
  JwtAuthenticationResponse,
  RefreshTokenRequest,
} from "@/types/auth";
import apiClient from "./apiClient";

/**
 * User data interface for authenticated user information.
 *
 * Represents the user profile data returned from the backend
 * after successful authentication or user queries.
 */
export interface User {
  /** Unique user identifier */
  id: number;
  /** Unique username for authentication */
  username: string;
  /** User's email address */
  email: string;
  /** User's first name */
  firstName: string;
  /** User's last name */
  lastName: string;
  /** User's system role - either ADMIN or USER */
  role: "ADMIN" | "USER";
  /** Whether the user account is enabled */
  enabled: boolean;
  /** ISO timestamp when the user was created */
  createdAt: string;
  /** ISO timestamp when the user was last updated */
  updatedAt: string;
}

/**
 * Standard message response interface.
 *
 * Used for API responses that return simple status messages.
 */
export interface MessageResponse {
  /** Response message text */
  message: string;
}

/**
 * Authentication service for handling user login, logout, and token management.
 *
 * This service provides a clean interface for all authentication-related
 * operations, including JWT token handling and user session management.
 *
 * @example
 * ```typescript
 * // Login user
 * const response = await authService.login({
 *   username: 'user',
 *   password: 'password'
 * });
 *
 * // Get current user info
 * const user = await authService.getCurrentUser();
 *
 * // Logout
 * await authService.logout();
 * ```
 */
export const authService = {
  /**
   * Authenticates a user with username and password.
   *
   * @param credentials - User login credentials
   * @returns Promise resolving to JWT authentication response with tokens and user info
   * @throws {AxiosError} When authentication fails or network errors occur
   */
  login: async (
    credentials: LoginRequest,
  ): Promise<JwtAuthenticationResponse> => {
    const response = await apiClient.post<JwtAuthenticationResponse>(
      "/auth/login",
      credentials,
    );
    return response.data;
  },

  /**
   * Logs out the current user.
   *
   * Sends a logout request to invalidate server-side sessions.
   * Client-side token cleanup should be handled separately.
   *
   * @returns Promise that resolves when logout is complete
   */
  logout: async (): Promise<void> => {
    await apiClient.post("/auth/logout");
  },

  /**
   * Refreshes the JWT access token using a refresh token.
   *
   * @param refreshTokenRequest - Request containing the refresh token
   * @returns Promise resolving to new JWT authentication response with fresh tokens
   * @throws {AxiosError} When refresh token is invalid or expired
   */
  refreshToken: async (
    refreshTokenRequest: RefreshTokenRequest,
  ): Promise<JwtAuthenticationResponse> => {
    const response = await apiClient.post<JwtAuthenticationResponse>(
      "/auth/refresh",
      refreshTokenRequest,
    );
    return response.data;
  },

  /**
   * Retrieves information about the currently authenticated user.
   *
   * @returns Promise resolving to the current user's profile data
   * @throws {AxiosError} When user is not authenticated or request fails
   */
  getCurrentUser: async (): Promise<User> => {
    const response = await apiClient.get<User>("/auth/me");
    return response.data;
  },
};
