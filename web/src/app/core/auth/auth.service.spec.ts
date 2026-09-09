import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let http: HttpTestingController;

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

  it('register stocke le JWT et marque authentifié', () => {
    let receivedToken = '';

    service.register({ email: 'a@example.com', password: 'Secret123!' }).subscribe((res) => {
      receivedToken = res.accessToken;
    });

    const req = http.expectOne('/api/auth/register');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ email: 'a@example.com', password: 'Secret123!' });
    req.flush({ accessToken: 'jwt-abc', tokenType: 'Bearer' });

    expect(receivedToken).toBe('jwt-abc');
    expect(service.token()).toBe('jwt-abc');
    expect(service.isAuthenticated()).toBe(true);
    expect(localStorage.getItem('habits.accessToken')).toBe('jwt-abc');
  });

  it('login stocke le JWT', () => {
    service.login({ email: 'a@example.com', password: 'Secret123!' }).subscribe();

    const req = http.expectOne('/api/auth/login');
    expect(req.request.method).toBe('POST');
    req.flush({ accessToken: 'jwt-login', tokenType: 'Bearer' });

    expect(service.token()).toBe('jwt-login');
  });

  it('logout efface le token', () => {
    localStorage.setItem('habits.accessToken', 'jwt-old');
    // recreate service to pick up stored token
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);

    expect(service.isAuthenticated()).toBe(true);
    service.logout();
    expect(service.isAuthenticated()).toBe(false);
    expect(localStorage.getItem('habits.accessToken')).toBeNull();
  });
});
