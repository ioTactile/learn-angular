import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { AuthTokenResponse, LoginCredentials, RefreshRequest } from './auth.models';

const ACCESS_TOKEN_KEY = 'habits.accessToken';
const REFRESH_TOKEN_KEY = 'habits.refreshToken';

/**
 * AuthService ≈ un composable/pinia store + fetch.
 * Access JWT court + refresh opaque longue durée.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);

  private readonly tokenSignal = signal<string | null>(this.readAccessToken());
  private readonly refreshTokenSignal = signal<string | null>(this.readRefreshToken());

  readonly token = this.tokenSignal.asReadonly();
  readonly refreshToken = this.refreshTokenSignal.asReadonly();
  readonly isAuthenticated = computed(() => !!this.tokenSignal());

  register(credentials: LoginCredentials): Observable<AuthTokenResponse> {
    return this.http
      .post<AuthTokenResponse>('/api/auth/register', credentials)
      .pipe(tap((response) => this.persistSession(response)));
  }

  login(credentials: LoginCredentials): Observable<AuthTokenResponse> {
    return this.http
      .post<AuthTokenResponse>('/api/auth/login', credentials)
      .pipe(tap((response) => this.persistSession(response)));
  }

  refresh(): Observable<AuthTokenResponse> {
    const refreshToken = this.refreshTokenSignal();
    if (!refreshToken) {
      throw new Error('No refresh token');
    }

    const body: RefreshRequest = { refreshToken };
    return this.http
      .post<AuthTokenResponse>('/api/auth/refresh', body)
      .pipe(tap((response) => this.persistSession(response)));
  }

  logout(): void {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    this.tokenSignal.set(null);
    this.refreshTokenSignal.set(null);
  }

  private persistSession(response: AuthTokenResponse): void {
    localStorage.setItem(ACCESS_TOKEN_KEY, response.accessToken);
    localStorage.setItem(REFRESH_TOKEN_KEY, response.refreshToken);
    this.tokenSignal.set(response.accessToken);
    this.refreshTokenSignal.set(response.refreshToken);
  }

  private readAccessToken(): string | null {
    return localStorage.getItem(ACCESS_TOKEN_KEY);
  }

  private readRefreshToken(): string | null {
    return localStorage.getItem(REFRESH_TOKEN_KEY);
  }
}
