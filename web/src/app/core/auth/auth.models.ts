export interface AuthTokenResponse {
  accessToken: string;
  tokenType: string;
}

export interface LoginCredentials {
  email: string;
  password: string;
}
