import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { AuthTokenResponse, LoginCredentials } from './auth.models';

const TOKEN_KEY = 'habits.accessToken';

/**
 * AuthService ≈ un composable/pinia store + fetch.
 * - signal `token` = état réactif (comme ref/useState)
 * - HttpClient = axios/fetch encapsulé Angular
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);

  private readonly tokenSignal = signal<string | null>(this.readStoredToken());

  readonly token = this.tokenSignal.asReadonly();
  readonly isAuthenticated = computed(() => !!this.tokenSignal());

  register(credentials: LoginCredentials): Observable<AuthTokenResponse> {
    return this.http
      .post<AuthTokenResponse>('/api/auth/register', credentials)
      .pipe(tap((response) => this.persistToken(response.accessToken)));
  }

  login(credentials: LoginCredentials): Observable<AuthTokenResponse> {
    return this.http
      .post<AuthTokenResponse>('/api/auth/login', credentials)
      .pipe(tap((response) => this.persistToken(response.accessToken)));
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    this.tokenSignal.set(null);
  }

  private persistToken(token: string): void {
    localStorage.setItem(TOKEN_KEY, token);
    this.tokenSignal.set(token);
  }

  private readStoredToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }
}
