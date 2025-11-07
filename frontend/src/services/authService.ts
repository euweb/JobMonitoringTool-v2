import {
  LoginRequest,
  JwtAuthenticationResponse,
  RefreshTokenRequest,
} from "@/types/auth";
import apiClient from "./apiClient";

export interface User {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  role: "ADMIN" | "USER";
  enabled: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface MessageResponse {
  message: string;
}

export const authService = {
  login: async (
    credentials: LoginRequest,
  ): Promise<JwtAuthenticationResponse> => {
    const response = await apiClient.post<JwtAuthenticationResponse>(
      "/auth/login",
      credentials,
    );
    return response.data;
  },

  logout: async (): Promise<void> => {
    await apiClient.post("/auth/logout");
  },

  refreshToken: async (
    refreshTokenRequest: RefreshTokenRequest,
  ): Promise<JwtAuthenticationResponse> => {
    const response = await apiClient.post<JwtAuthenticationResponse>(
      "/auth/refresh",
      refreshTokenRequest,
    );
    return response.data;
  },

  getCurrentUser: async (): Promise<User> => {
    const response = await apiClient.get<User>("/auth/me");
    return response.data;
  },
};
