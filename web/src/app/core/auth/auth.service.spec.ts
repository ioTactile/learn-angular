import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let http: HttpTestingController;

  const me = { id: 'u1', email: 'a@example.com', role: 'USER' as const };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
  });

  it('register stocke l’access token puis charge /me', () => {
    service.register({ email: 'a@example.com', password: 'Secret123!' }).subscribe();

    const req = http.expectOne('/api/auth/register');
    expect(req.request.method).toBe('POST');
    expect(req.request.withCredentials).toBe(true);
    req.flush({
      accessToken: 'jwt-abc',
      tokenType: 'Bearer',
    });

    http.expectOne('/api/me').flush(me);

    expect(service.token()).toBe('jwt-abc');
    expect(service.isAuthenticated()).toBe(true);
    expect(service.currentUser()).toEqual(me);
  });

  it('login stocke le token et le profil', () => {
    service.login({ email: 'a@example.com', password: 'Secret123!' }).subscribe();

    http.expectOne('/api/auth/login').flush({
      accessToken: 'jwt-login',
      tokenType: 'Bearer',
    });
    http.expectOne('/api/me').flush({ ...me, role: 'ADMIN' });

    expect(service.token()).toBe('jwt-login');
    expect(service.isAdmin()).toBe(true);
  });

  it('refresh renouvelle la session via cookie et recharge /me', () => {
    service.refresh().subscribe();

    const req = http.expectOne('/api/auth/refresh');
    expect(req.request.body).toEqual({});
    expect(req.request.withCredentials).toBe(true);
    req.flush({
      accessToken: 'new-access',
      tokenType: 'Bearer',
    });
    http.expectOne('/api/me').flush(me);

    expect(service.token()).toBe('new-access');
    expect(service.currentUser()).toEqual(me);
  });

  it('restoreSession recharge via refresh puis /me', () => {
    service.restoreSession().subscribe((user) => {
      expect(user).toEqual(me);
    });

    http.expectOne('/api/auth/refresh').flush({
      accessToken: 'restored',
      tokenType: 'Bearer',
    });
    http.expectOne('/api/me').flush(me);

    expect(service.token()).toBe('restored');
    expect(service.currentUser()).toEqual(me);
  });

  it('restoreSession renvoie null si le cookie est absent', () => {
    service.restoreSession().subscribe((user) => {
      expect(user).toBeNull();
    });

    http.expectOne('/api/auth/refresh').flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(service.token()).toBeNull();
  });

  it('logout appelle l’API puis efface la session', () => {
    service.login({ email: 'a@example.com', password: 'Secret123!' }).subscribe();
    http.expectOne('/api/auth/login').flush({ accessToken: 'jwt-old', tokenType: 'Bearer' });
    http.expectOne('/api/me').flush(me);

    expect(service.isAuthenticated()).toBe(true);
    service.logout().subscribe();

    const req = http.expectOne('/api/auth/logout');
    expect(req.request.method).toBe('POST');
    expect(req.request.withCredentials).toBe(true);
    req.flush(null, { status: 204, statusText: 'No Content' });

    expect(service.isAuthenticated()).toBe(false);
    expect(service.currentUser()).toBeNull();
  });

  it('logout efface la session même si l’API échoue', () => {
    service.login({ email: 'a@example.com', password: 'Secret123!' }).subscribe();
    http.expectOne('/api/auth/login').flush({ accessToken: 'jwt-old', tokenType: 'Bearer' });
    http.expectOne('/api/me').flush(me);

    service.logout().subscribe();
    http.expectOne('/api/auth/logout').flush(null, { status: 500, statusText: 'Server Error' });

    expect(service.isAuthenticated()).toBe(false);
  });
});
