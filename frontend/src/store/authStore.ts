import { create } from "zustand";
import { persist } from "zustand/middleware";
import { User } from "@/types/auth";

/**
 * Authentication state interface defining the shape of the auth store.
 *
 * This interface describes both the state properties and action methods
 * available for managing user authentication and session state.
 */
interface AuthState {
  /** Currently authenticated user object, null if not logged in */
  user: User | null;
  /** JWT access token for API requests */
  accessToken: string | null;
  /** JWT refresh token for obtaining new access tokens */
  refreshToken: string | null;
  /** Boolean indicating if user is currently authenticated */
  isAuthenticated: boolean;
  /** Loading state for async authentication operations */
  isLoading: boolean;

  /**
   * Logs in a user by setting user data and tokens.
   *
   * @param user - User object with profile information
   * @param accessToken - JWT access token
   * @param refreshToken - JWT refresh token
   */
  login: (user: User, accessToken: string, refreshToken: string) => void;

  /**
   * Logs out the current user by clearing all auth state.
   */
  logout: () => void;

  /**
   * Updates the current user's profile information.
   *
   * @param user - Updated user object
   */
  updateUser: (user: User) => void;

  /**
   * Updates authentication tokens without changing user data.
   *
   * @param accessToken - New JWT access token
   * @param refreshToken - New JWT refresh token (optional, keeps existing if not provided)
   */
  updateTokens: (accessToken: string, refreshToken?: string) => void;

  /**
   * Sets the loading state for async operations.
   *
   * @param loading - Loading state boolean
   */
  setLoading: (loading: boolean) => void;
}

/**
 * Zustand store for managing authentication state across the application.
 *
 * Features:
 * - Persistent storage of auth tokens and user data
 * - Automatic state persistence across browser sessions
 * - Type-safe state management with TypeScript
 * - Centralized authentication logic
 *
 * The store automatically persists authentication data to localStorage,
 * allowing users to remain logged in across browser sessions.
 *
 * @example
 * ```typescript
 * import { useAuthStore } from '@/store/authStore';
 *
 * // In a component
 * const { user, isAuthenticated, login, logout } = useAuthStore();
 *
 * // Check auth status
 * if (isAuthenticated) {
 *   console.log('User logged in:', user?.username);
 * }
 *
 * // Login user
 * login(userData, accessToken, refreshToken);
 * ```
 */
export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      accessToken: null,
      refreshToken: null,
      isAuthenticated: false,
      isLoading: false,

      login: (user, accessToken, refreshToken) => {
        set({
          user,
          accessToken,
          refreshToken,
          isAuthenticated: true,
          isLoading: false,
        });
      },

      logout: () => {
        set({
          user: null,
          accessToken: null,
          refreshToken: null,
          isAuthenticated: false,
          isLoading: false,
        });
      },

      updateUser: (user) => {
        set({ user });
      },

      updateTokens: (accessToken, refreshToken) => {
        const currentRefreshToken = refreshToken || get().refreshToken;
        set({
          accessToken,
          refreshToken: currentRefreshToken,
          isAuthenticated: true,
        });
      },

      setLoading: (loading) => {
        set({ isLoading: loading });
      },
    }),
    {
      name: "auth-storage",
      partialize: (state) => ({
        user: state.user,
        accessToken: state.accessToken,
        refreshToken: state.refreshToken,
        isAuthenticated: state.isAuthenticated,
      }),
    },
  ),
);
