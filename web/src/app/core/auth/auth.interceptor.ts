import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from './auth.service';

/**
 * Ajoute Authorization: Bearer <accessToken> si présent.
 * Ne touche pas refresh/logout (le refresh opaque est le credential).
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  if (req.url.includes('/api/auth/refresh') || req.url.includes('/api/auth/logout')) {
    return next(req);
  }

  const auth = inject(AuthService);
  const token = auth.token();

  if (!token) {
    return next(req);
  }

  return next(
    req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`,
      },
    }),
  );
};
