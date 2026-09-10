export interface AuthTokenResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
}

export interface LoginCredentials {
  email: string;
  password: string;
}

export interface RefreshRequest {
  refreshToken: string;
}

export type UserRole = 'USER' | 'ADMIN';

export interface CurrentUser {
  id: string;
  email: string;
  role: UserRole;
}
