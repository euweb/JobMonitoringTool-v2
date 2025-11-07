import axios, { AxiosError, AxiosResponse } from "axios";
import { useAuthStore } from "@/store/authStore";
import { ApiError } from "@/types/auth";

/**
 * Centralized Axios client for API communication with the JobMonitor backend.
 *
 * This configured HTTP client provides:
 * - Automatic JWT token attachment to requests
 * - Token refresh on 401 errors with retry logic
 * - Consistent error handling and timeout configuration
 * - Base URL and headers standardization
 *
 * @example
 * ```typescript
 * import apiClient from '@/services/apiClient';
 *
 * // GET request
 * const response = await apiClient.get('/users');
 *
 * // POST request with data
 * const user = await apiClient.post('/users', userData);
 * ```
 */

/**
 * Pre-configured Axios instance for JobMonitor API.
 *
 * Features:
 * - Base URL: `/api`
 * - Timeout: 10 seconds
 * - Content-Type: `application/json`
 * - Automatic Bearer token injection
 * - Token refresh on 401 responses
 */
export const apiClient = axios.create({
  baseURL: "/api",
  timeout: 10000,
  headers: {
    "Content-Type": "application/json",
  },
});

/**
 * Request interceptor that automatically adds JWT bearer tokens to outgoing requests.
 *
 * Retrieves the current access token from the auth store and injects it into
 * the Authorization header for authenticated API calls.
 */
apiClient.interceptors.request.use(
  (config) => {
    const token = useAuthStore.getState().accessToken;
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  },
);

/**
 * Response interceptor that handles token refresh and authentication errors.
 *
 * On 401 responses:
 * 1. Attempts to refresh the access token using the stored refresh token
 * 2. Retries the original request with the new token
 * 3. Redirects to login if refresh fails or no refresh token exists
 *
 * Prevents infinite retry loops using the `_retry` flag.
 */
apiClient.interceptors.response.use(
  (response: AxiosResponse) => response,
  async (error: AxiosError<ApiError>) => {
    const originalRequest = error.config;

    if (
      error.response?.status === 401 &&
      originalRequest &&
      !originalRequest._retry
    ) {
      originalRequest._retry = true;

      const refreshToken = useAuthStore.getState().refreshToken;

      if (refreshToken) {
        try {
          const response = await axios.post("/api/auth/refresh", {
            refreshToken,
          });

          const { accessToken, refreshToken: newRefreshToken } = response.data;

          // Update tokens in store
          useAuthStore.getState().updateTokens(accessToken, newRefreshToken);

          // Retry original request with new token
          originalRequest.headers.Authorization = `Bearer ${accessToken}`;
          return apiClient(originalRequest);
        } catch (refreshError) {
          // Refresh failed, logout user
          useAuthStore.getState().logout();
          window.location.href = "/login";
        }
      } else {
        // No refresh token, logout user
        useAuthStore.getState().logout();
        window.location.href = "/login";
      }
    }

    return Promise.reject(error);
  },
);

/**
 * Extended Axios types to support retry functionality.
 *
 * Adds the `_retry` flag to AxiosRequestConfig to prevent infinite
 * retry loops when token refresh fails.
 */
declare module "axios" {
  interface AxiosRequestConfig {
    /** Flag to prevent infinite retry loops during token refresh */
    _retry?: boolean;
  }
}

export default apiClient;
