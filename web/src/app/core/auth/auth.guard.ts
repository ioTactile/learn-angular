import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { catchError, map, of, switchMap } from 'rxjs';
import { AuthService } from './auth.service';

/** Guard ≈ middleware Next.js / navigation guard Nuxt. */
export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (!auth.isAuthenticated()) {
    return router.createUrlTree(['/login']);
  }

  if (auth.currentUser()) {
    return true;
  }

  return auth.loadMe().pipe(
    map(() => true),
    catchError(() =>
      auth.logout().pipe(switchMap(() => of(router.createUrlTree(['/login'])))),
    ),
  );
};

export const guestGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (!auth.isAuthenticated()) {
    return true;
  }

  return router.createUrlTree(['/workspaces']);
};

/** UI only — l’API refuse déjà /api/admin/** aux non-ADMIN. */
export const adminGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (!auth.isAuthenticated()) {
    return router.createUrlTree(['/login']);
  }

  const allow = () => (auth.isAdmin() ? true : router.createUrlTree(['/workspaces']));

  if (auth.currentUser()) {
    return allow();
  }

  return auth.loadMe().pipe(
    map(() => allow()),
    catchError(() =>
      auth.logout().pipe(switchMap(() => of(router.createUrlTree(['/login'])))),
    ),
  );
};
