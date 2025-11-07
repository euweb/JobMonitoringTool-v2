import apiClient from "./apiClient";
import { User, MessageResponse } from "./authService";

export interface UpdateProfileRequest {
  email: string;
  firstName: string;
  lastName: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

export const userService = {
  // User Profile
  getUserProfile: async (): Promise<User> => {
    const response = await apiClient.get<User>("/user/profile");
    return response.data;
  },

  updateUserProfile: async (userData: UpdateProfileRequest): Promise<User> => {
    const response = await apiClient.put<User>("/user/profile", userData);
    return response.data;
  },

  changePassword: async (
    passwordData: ChangePasswordRequest,
  ): Promise<MessageResponse> => {
    const response = await apiClient.post<MessageResponse>(
      "/user/change-password",
      passwordData,
    );
    return response.data;
  },
};
