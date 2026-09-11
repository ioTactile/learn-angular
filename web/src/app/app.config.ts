import { provideHttpClient, withInterceptors } from '@angular/common/http';
import {
  ApplicationConfig,
  inject,
  provideAppInitializer,
  provideBrowserGlobalErrorListeners,
} from '@angular/core';
import { MatPaginatorIntl } from '@angular/material/paginator';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideRouter } from '@angular/router';
import { catchError, firstValueFrom, of } from 'rxjs';
import { routes } from './app.routes';
import { authErrorInterceptor } from './core/auth/auth-error.interceptor';
import { authInterceptor } from './core/auth/auth.interceptor';
import { AuthService } from './core/auth/auth.service';
import { FrPaginatorIntl } from './core/i18n/fr-paginator-intl';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor, authErrorInterceptor])),
    provideAnimationsAsync(),
    { provide: MatPaginatorIntl, useClass: FrPaginatorIntl },
    provideAppInitializer(() => {
      const auth = inject(AuthService);
      return firstValueFrom(auth.restoreSession().pipe(catchError(() => of(null))));
    }),
  ],
};
