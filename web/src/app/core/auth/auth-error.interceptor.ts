import {
  HttpErrorResponse,
  HttpInterceptorFn,
  HttpRequest,
} from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from './auth.service';

let refreshInFlight: ReturnType<AuthService['refresh']> | null = null;

/**
 * Sur 401 :
 * 1. tente un refresh (une seule fois, partagé si requêtes parallèles)
 * 2. rejoue la requête avec le nouvel access token
 * 3. sinon logout + redirect /login
 *
 * Pattern entreprise classique (axios interceptor / Nuxt plugin).
 */
export const authErrorInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  return next(req).pipe(
    catchError((error: unknown) => {
      if (!(error instanceof HttpErrorResponse) || error.status !== 401) {
        return throwError(() => error);
      }

      if (isAuthEndpoint(req)) {
        return throwError(() => error);
      }

      if (!auth.refreshToken()) {
        auth.logout();
        void router.navigateByUrl('/login');
        return throwError(() => error);
      }

      if (!refreshInFlight) {
        refreshInFlight = auth.refresh().pipe(
          catchError((refreshError) => {
            refreshInFlight = null;
            auth.logout();
            void router.navigateByUrl('/login');
            return throwError(() => refreshError);
          }),
        );
      }

      return refreshInFlight.pipe(
        switchMap(() => {
          refreshInFlight = null;
          const token = auth.token();
          const retry = token
            ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
            : req;
          return next(retry);
        }),
      );
    }),
  );
};

function isAuthEndpoint(req: HttpRequest<unknown>): boolean {
  return (
    req.url.includes('/api/auth/login') ||
    req.url.includes('/api/auth/register') ||
    req.url.includes('/api/auth/refresh') ||
    req.url.includes('/api/auth/logout')
  );
}
