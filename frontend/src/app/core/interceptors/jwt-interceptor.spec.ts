import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HTTP_INTERCEPTORS, HttpClient } from '@angular/common/http';
import { JwtInterceptorClass } from './jwt-interceptor';
import { AuthService } from '../services/auth';
import { Router } from '@angular/router';

describe('JwtInterceptor', () => {
  let httpMock: HttpTestingController;
  let httpClient: HttpClient;
  let authService: jasmine.SpyObj<AuthService>;
  let router: jasmine.SpyObj<Router>;

  const mockToken = 'mock-jwt-token-xyz';

  beforeEach(() => {
    const authServiceSpy = jasmine.createSpyObj('AuthService', ['getToken', 'logout']);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        { provide: Router, useValue: routerSpy },
        { provide: HTTP_INTERCEPTORS, useClass: JwtInterceptorClass, multi: true }
      ]
    });

    httpMock = TestBed.inject(HttpTestingController);
    httpClient = TestBed.inject(HttpClient);
    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should add Authorization header with Bearer token to requests', () => {
    authService.getToken.and.returnValue(mockToken);

    httpClient.get('/api/data').subscribe();

    const req = httpMock.expectOne('/api/data');
    expect(req.request.headers.get('Authorization')).toBe(`Bearer ${mockToken}`);
    req.flush({});
  });

  it('should not add Authorization header if no token', () => {
    authService.getToken.and.returnValue(null);

    httpClient.get('/api/data').subscribe();

    const req = httpMock.expectOne('/api/data');
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush({});
  });

  it('should handle 401 error and logout user', () => {
    authService.getToken.and.returnValue(mockToken);

    httpClient.get('/api/protected').subscribe(
      () => fail('should have failed with 401 error'),
      () => {
        expect(authService.logout).toHaveBeenCalled();
      }
    );

    const req = httpMock.expectOne('/api/protected');
    req.flush({ message: 'Unauthorized' }, { status: 401, statusText: 'Unauthorized' });
  });

  it('should retry once on error', () => {
    authService.getToken.and.returnValue(null);

    let requestCount = 0;
    httpClient.get('/api/data').subscribe(
      () => fail('should have failed'),
      () => {} // Error handler
    );

    expect(httpMock.match('/api/data').length).toBe(2); // 1 original + 1 retry
  });
});
