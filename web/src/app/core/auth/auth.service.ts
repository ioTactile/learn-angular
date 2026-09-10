import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, map, switchMap, tap } from 'rxjs';
import {
  AuthTokenResponse,
  CurrentUser,
  LoginCredentials,
  RefreshRequest,
} from './auth.models';

const ACCESS_TOKEN_KEY = 'habits.accessToken';
const REFRESH_TOKEN_KEY = 'habits.refreshToken';

/**
 * AuthService ≈ un composable/pinia store + fetch.
 * Access JWT court + refresh opaque longue durée.
 * Profil (`/api/me`) pour le rôle UI — la sécurité réelle reste côté API.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);

  private readonly tokenSignal = signal<string | null>(this.readAccessToken());
  private readonly refreshTokenSignal = signal<string | null>(this.readRefreshToken());
  private readonly currentUserSignal = signal<CurrentUser | null>(null);

  readonly token = this.tokenSignal.asReadonly();
  readonly refreshToken = this.refreshTokenSignal.asReadonly();
  readonly currentUser = this.currentUserSignal.asReadonly();
  readonly isAuthenticated = computed(() => !!this.tokenSignal());
  readonly isAdmin = computed(() => this.currentUserSignal()?.role === 'ADMIN');

  register(credentials: LoginCredentials): Observable<CurrentUser> {
    return this.http.post<AuthTokenResponse>('/api/auth/register', credentials).pipe(
      tap((response) => this.persistTokens(response)),
      switchMap(() => this.loadMe()),
    );
  }

  login(credentials: LoginCredentials): Observable<CurrentUser> {
    return this.http.post<AuthTokenResponse>('/api/auth/login', credentials).pipe(
      tap((response) => this.persistTokens(response)),
      switchMap(() => this.loadMe()),
    );
  }

  refresh(): Observable<AuthTokenResponse> {
    const refreshToken = this.refreshTokenSignal();
    if (!refreshToken) {
      throw new Error('No refresh token');
    }

    const body: RefreshRequest = { refreshToken };
    return this.http.post<AuthTokenResponse>('/api/auth/refresh', body).pipe(
      tap((response) => this.persistTokens(response)),
      switchMap((response) => this.loadMe().pipe(map(() => response))),
    );
  }

  loadMe(): Observable<CurrentUser> {
    return this.http
      .get<CurrentUser>('/api/me')
      .pipe(tap((user) => this.currentUserSignal.set(user)));
  }

  logout(): void {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    this.tokenSignal.set(null);
    this.refreshTokenSignal.set(null);
    this.currentUserSignal.set(null);
  }

  private persistTokens(response: AuthTokenResponse): void {
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
