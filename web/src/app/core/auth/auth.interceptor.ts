import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from './auth.service';

/**
 * Envoie les cookies (refresh HttpOnly) et le Bearer access si présent.
 * Ne met pas le Bearer sur refresh/logout (le cookie est le credential).
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const withCreds = req.clone({ withCredentials: true });

  if (withCreds.url.includes('/api/auth/refresh') || withCreds.url.includes('/api/auth/logout')) {
    return next(withCreds);
  }

  const auth = inject(AuthService);
  const token = auth.token();

  if (!token) {
    return next(withCreds);
  }

  return next(
    withCreds.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`,
      },
    }),
  );
};
