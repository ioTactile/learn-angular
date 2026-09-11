import { TestBed } from '@angular/core/testing';
import {
  HttpClient,
  HttpErrorResponse,
  provideHttpClient,
  withInterceptors,
} from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { authErrorInterceptor } from './auth-error.interceptor';
import { AuthService } from './auth.service';

describe('authErrorInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;
  let auth: {
    token: ReturnType<typeof vi.fn>;
    refresh: ReturnType<typeof vi.fn>;
    logout: ReturnType<typeof vi.fn>;
  };
  let router: { navigateByUrl: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    auth = {
      token: vi.fn().mockReturnValue('access-1'),
      refresh: vi.fn(),
      logout: vi.fn().mockReturnValue(of(undefined)),
    };
    router = { navigateByUrl: vi.fn().mockResolvedValue(true) };

    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        provideHttpClient(withInterceptors([authErrorInterceptor])),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: auth },
        { provide: Router, useValue: router },
      ],
    });

    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('sur 401 : refresh puis rejoue la requête', () => {
    auth.refresh.mockReturnValue(
      of({
        accessToken: 'access-2',
        tokenType: 'Bearer',
      }),
    );
    auth.token.mockReturnValue('access-2');

    let status = 0;
    http.get('/api/habits').subscribe({
      next: () => (status = 200),
      error: () => (status = 500),
    });

    const first = httpMock.expectOne('/api/habits');
    first.flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(auth.refresh).toHaveBeenCalled();

    const retry = httpMock.expectOne('/api/habits');
    expect(retry.request.headers.get('Authorization')).toBe('Bearer access-2');
    retry.flush({ content: [] });

    expect(status).toBe(200);
    expect(auth.logout).not.toHaveBeenCalled();
  });

  it('si refresh échoue : logout + redirect login', () => {
    auth.refresh.mockReturnValue(throwError(() => new HttpErrorResponse({ status: 401 })));

    http.get('/api/habits').subscribe({
      error: () => undefined,
    });

    httpMock.expectOne('/api/habits').flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(auth.logout).toHaveBeenCalled();
    expect(router.navigateByUrl).toHaveBeenCalledWith('/login');
  });

  it('un 401 sur /api/auth/logout ne déclenche pas de refresh', () => {
    http.post('/api/auth/logout', {}).subscribe({
      error: () => undefined,
    });

    httpMock.expectOne('/api/auth/logout').flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(auth.refresh).not.toHaveBeenCalled();
    expect(auth.logout).not.toHaveBeenCalled();
  });
});
