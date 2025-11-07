import apiClient from "./apiClient";
import { User, MessageResponse } from "./authService";

// Re-export User for convenience
export type { User };

export interface CreateUserRequest {
  username: string;
  email: string;
  password: string;
  firstName: string;
  lastName: string;
}

export interface UpdateUserRequest {
  email: string;
  firstName: string;
  lastName: string;
  role: "ADMIN" | "USER";
}

export interface AdminStats {
  totalUsers: number;
  systemStatus: string;
}

export const adminService = {
  // User Management
  getAllUsers: async (): Promise<User[]> => {
    const response = await apiClient.get<User[]>("/admin/users");
    return response.data;
  },

  getUserById: async (id: number): Promise<User> => {
    const response = await apiClient.get<User>(`/admin/users/${id}`);
    return response.data;
  },

  createUser: async (userData: CreateUserRequest): Promise<User> => {
    const response = await apiClient.post<User>("/admin/users", userData);
    return response.data;
  },

  createAdminUser: async (userData: CreateUserRequest): Promise<User> => {
    const response = await apiClient.post<User>("/admin/users/admin", userData);
    return response.data;
  },

  updateUser: async (
    id: number,
    userData: UpdateUserRequest,
  ): Promise<User> => {
    const response = await apiClient.put<User>(`/admin/users/${id}`, userData);
    return response.data;
  },

  toggleUserEnabled: async (id: number): Promise<MessageResponse> => {
    const response = await apiClient.post<MessageResponse>(
      `/admin/users/${id}/toggle-enabled`,
    );
    return response.data;
  },

  deleteUser: async (id: number): Promise<MessageResponse> => {
    const response = await apiClient.delete<MessageResponse>(
      `/admin/users/${id}`,
    );
    return response.data;
  },

  // Admin Stats
  getAdminStats: async (): Promise<AdminStats> => {
    const response = await apiClient.get<AdminStats>("/admin/stats");
    return response.data;
  },
};
