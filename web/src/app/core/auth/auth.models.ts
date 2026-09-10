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
