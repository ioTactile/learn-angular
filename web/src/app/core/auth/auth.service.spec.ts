import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let http: HttpTestingController;

  const me = { id: 'u1', email: 'a@example.com', role: 'USER' as const };

  beforeEach(() => {
    localStorage.clear();

    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
    localStorage.clear();
  });

  it('register stocke access + refresh tokens puis charge /me', () => {
    service.register({ email: 'a@example.com', password: 'Secret123!' }).subscribe();

    const req = http.expectOne('/api/auth/register');
    expect(req.request.method).toBe('POST');
    req.flush({
      accessToken: 'jwt-abc',
      refreshToken: 'refresh-abc',
      tokenType: 'Bearer',
    });

    http.expectOne('/api/me').flush(me);

    expect(service.token()).toBe('jwt-abc');
    expect(service.refreshToken()).toBe('refresh-abc');
    expect(service.isAuthenticated()).toBe(true);
    expect(service.currentUser()).toEqual(me);
    expect(localStorage.getItem('habits.accessToken')).toBe('jwt-abc');
    expect(localStorage.getItem('habits.refreshToken')).toBe('refresh-abc');
  });

  it('login stocke les tokens et le profil', () => {
    service.login({ email: 'a@example.com', password: 'Secret123!' }).subscribe();

    http.expectOne('/api/auth/login').flush({
      accessToken: 'jwt-login',
      refreshToken: 'refresh-login',
      tokenType: 'Bearer',
    });
    http.expectOne('/api/me').flush({ ...me, role: 'ADMIN' });

    expect(service.token()).toBe('jwt-login');
    expect(service.isAdmin()).toBe(true);
  });

  it('refresh renouvelle la session et recharge /me', () => {
    localStorage.setItem('habits.accessToken', 'old-access');
    localStorage.setItem('habits.refreshToken', 'old-refresh');
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);

    service.refresh().subscribe();

    const req = http.expectOne('/api/auth/refresh');
    expect(req.request.body).toEqual({ refreshToken: 'old-refresh' });
    req.flush({
      accessToken: 'new-access',
      refreshToken: 'new-refresh',
      tokenType: 'Bearer',
    });
    http.expectOne('/api/me').flush(me);

    expect(service.token()).toBe('new-access');
    expect(service.refreshToken()).toBe('new-refresh');
    expect(service.currentUser()).toEqual(me);
  });

  it('logout efface les tokens et le profil', () => {
    localStorage.setItem('habits.accessToken', 'jwt-old');
    localStorage.setItem('habits.refreshToken', 'refresh-old');
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);

    expect(service.isAuthenticated()).toBe(true);
    service.logout();
    expect(service.isAuthenticated()).toBe(false);
    expect(service.currentUser()).toBeNull();
    expect(localStorage.getItem('habits.accessToken')).toBeNull();
    expect(localStorage.getItem('habits.refreshToken')).toBeNull();
  });
});
