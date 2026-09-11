import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, catchError, map, of, switchMap, tap } from 'rxjs';
import { AuthTokenResponse, CurrentUser, LoginCredentials } from './auth.models';

/**
 * Access JWT en mémoire. Refresh opaque : cookie HttpOnly (Path=/api/auth).
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);

  private readonly tokenSignal = signal<string | null>(null);
  private readonly currentUserSignal = signal<CurrentUser | null>(null);

  readonly token = this.tokenSignal.asReadonly();
  readonly currentUser = this.currentUserSignal.asReadonly();
  readonly isAuthenticated = computed(() => !!this.tokenSignal());
  readonly isAdmin = computed(() => this.currentUserSignal()?.role === 'ADMIN');

  register(credentials: LoginCredentials): Observable<CurrentUser> {
    return this.http
      .post<AuthTokenResponse>('/api/auth/register', credentials, { withCredentials: true })
      .pipe(
        tap((response) => this.tokenSignal.set(response.accessToken)),
        switchMap(() => this.loadMe()),
      );
  }

  login(credentials: LoginCredentials): Observable<CurrentUser> {
    return this.http
      .post<AuthTokenResponse>('/api/auth/login', credentials, { withCredentials: true })
      .pipe(
        tap((response) => this.tokenSignal.set(response.accessToken)),
        switchMap(() => this.loadMe()),
      );
  }

  refresh(): Observable<AuthTokenResponse> {
    return this.http.post<AuthTokenResponse>('/api/auth/refresh', {}, { withCredentials: true }).pipe(
      tap((response) => this.tokenSignal.set(response.accessToken)),
      switchMap((response) => this.loadMe().pipe(map(() => response))),
    );
  }

  /** Reprend la session via le cookie refresh (F5 / nouvel onglet). */
  restoreSession(): Observable<CurrentUser | null> {
    return this.refresh().pipe(
      map(() => this.currentUserSignal()),
      catchError(() => of(null)),
    );
  }

  loadMe(): Observable<CurrentUser> {
    return this.http
      .get<CurrentUser>('/api/me', { withCredentials: true })
      .pipe(tap((user) => this.currentUserSignal.set(user)));
  }

  logout(): Observable<void> {
    return this.http
      .post('/api/auth/logout', {}, { withCredentials: true, responseType: 'text' })
      .pipe(
        catchError(() => of('')),
        tap(() => this.clearSession()),
        map(() => undefined),
      );
  }

  private clearSession(): void {
    this.tokenSignal.set(null);
    this.currentUserSignal.set(null);
  }
}
