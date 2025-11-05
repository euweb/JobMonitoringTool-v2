import {
  LoginRequest,
  JwtAuthenticationResponse,
  RefreshTokenRequest,
} from '@/types/auth'
import apiClient from './apiClient'

export const authService = {
  login: async (credentials: LoginRequest): Promise<JwtAuthenticationResponse> => {
    const response = await apiClient.post<JwtAuthenticationResponse>('/auth/login', credentials)
    return response.data
  },

  logout: async (): Promise<void> => {
    await apiClient.post('/auth/logout')
  },

  refreshToken: async (refreshTokenRequest: RefreshTokenRequest): Promise<JwtAuthenticationResponse> => {
    const response = await apiClient.post<JwtAuthenticationResponse>('/auth/refresh', refreshTokenRequest)
    return response.data
  },
}