export interface AuthTokenResponse {
  accessToken: string;
  tokenType: string;
}

export interface LoginCredentials {
  email: string;
  password: string;
}

export type UserRole = 'USER' | 'ADMIN';

export interface CurrentUser {
  id: string;
  email: string;
  role: UserRole;
}
