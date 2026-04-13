import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuthService, AuthApiService } from './auth';
import { StorageService } from './storage';
import { UserRole, AuthResponse } from '../models/auth.model';

describe('AuthService', () => {
  let service: AuthService;
  let apiService: AuthApiService;
  let httpMock: HttpTestingController;
  let storageService: jasmine.SpyObj<StorageService>;

  const mockUserInfo = {
    id: 1,
    email: 'test@example.com',
    nomComplet: 'Test User',
    role: UserRole.STUDENT
  };

  const mockAuthResponse: AuthResponse = {
    token: 'mock-token-jwt',
    refreshToken: 'mock-refresh-token',
    user: mockUserInfo
  };

  beforeEach(() => {
    const storageSpy = jasmine.createSpyObj('StorageService', [
      'getAuth',
      'setAuth',
      'clearAuth',
      'get',
      'set',
      'remove'
    ]);

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        AuthService,
        AuthApiService,
        { provide: StorageService, useValue: storageSpy }
      ]
    });

    service = TestBed.inject(AuthService);
    apiService = TestBed.inject(AuthApiService);
    httpMock = TestBed.inject(HttpTestingController);
    storageService = TestBed.inject(StorageService) as jasmine.SpyObj<StorageService>;
  });

  afterEach(() => {
    httpMock.verify();
  });

  describe('AuthService', () => {
    it('should be created', () => {
      expect(service).toBeTruthy();
    });

    it('should return false for isAuthenticated when no token', () => {
      const result = service.isAuthenticated();
      expect(result).toBe(false);
    });

    it('should have user null when not authenticated', () => {
      const user = service.getCurrentUser();
      expect(user).toBeNull();
    });

    it('should have token null when not authenticated', () => {
      const token = service.getToken();
      expect(token).toBeNull();
    });

    it('should return false for hasRole when not authenticated', () => {
      const result = service.hasRole(UserRole.ADMIN);
      expect(result).toBe(false);
    });

    it('should handle login success', (done) => {
      service.login('test@example.com', 'password123').subscribe(
        (response) => {
          expect(response).toEqual(mockAuthResponse);
          expect(storageService.setAuth).toHaveBeenCalled();
          done();
        }
      );

      const req = httpMock.expectOne('/api/auth/login');
      expect(req.request.method).toBe('POST');
      req.flush(mockAuthResponse);
    });

    it('should handle login failure', (done) => {
      service.login('test@example.com', 'wrong-password').subscribe(
        () => fail('should have failed'),
        () => {
          done();
        }
      );

      const req = httpMock.expectOne('/api/auth/login');
      req.flush({ message: 'Invalid credentials' }, { status: 401, statusText: 'Unauthorized' });
    });

    it('should logout and clear auth', () => {
      service.logout();
      expect(storageService.clearAuth).toHaveBeenCalled();
      expect(service.isAuthenticated()).toBe(false);
    });

    it('should update auth state after login', (done) => {
      service.login(mockUserInfo.email, 'password').subscribe(() => {
        service.getAuthState().subscribe((state) => {
          expect(state.isAuthenticated).toBe(true);
          expect(state.user).toEqual(mockUserInfo);
          expect(state.token).toBe(mockAuthResponse.token);
          done();
        });
      });

      const req = httpMock.expectOne('/api/auth/login');
      req.flush(mockAuthResponse);
    });
  });

  describe('AuthApiService', () => {
    it('should make login POST request', () => {
      apiService.login({ email: 'test@example.com', password: 'password' }).subscribe();

      const req = httpMock.expectOne('/api/auth/login');
      expect(req.request.method).toBe('POST');
      req.flush(mockAuthResponse);
    });

    it('should make refresh token POST request', () => {
      apiService.refreshToken('refresh-token').subscribe();

      const req = httpMock.expectOne('/api/auth/refresh');
      expect(req.request.method).toBe('POST');
      req.flush(mockAuthResponse);
    });

    it('should validate token', () => {
      apiService.validateToken('test-token').subscribe();

      const req = httpMock.expectOne('/api/auth/validate');
      expect(req.request.method).toBe('POST');
      req.flush({ valid: true });
    });

    it('should get current user', () => {
      apiService.getCurrentUser().subscribe();

      const req = httpMock.expectOne('/api/auth/me');
      expect(req.request.method).toBe('GET');
      req.flush(mockUserInfo);
    });
  });
});
