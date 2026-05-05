import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HTTP_INTERCEPTORS, HttpClient } from '@angular/common/http';
import { ErrorInterceptorClass, ErrorInterceptor } from './error-interceptor';
import { Router } from '@angular/router';

describe('ErrorInterceptor', () => {
  let httpMock: HttpTestingController;
  let httpClient: HttpClient;
  let router: jasmine.SpyObj<Router>;

  beforeEach(() => {
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        { provide: Router, useValue: routerSpy },
        { provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptorClass, multi: true }
      ]
    });

    httpMock = TestBed.inject(HttpTestingController);
    httpClient = TestBed.inject(HttpClient);
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should get error message for 400', () => {
    const message = ErrorInterceptor.getErrorMessage(400, {});
    expect(message).toBe('Requête invalide');
  });

  it('should get error message for 401', () => {
    const message = ErrorInterceptor.getErrorMessage(401, {});
    expect(message).toBe('Non authentifié - Pour accéder, connectez-vous');
  });

  it('should get error message for 403', () => {
    const message = ErrorInterceptor.getErrorMessage(403, {});
    expect(message).toBe('Accès refusé - Vous n\'avez pas les permissions');
  });

  it('should get error message for 404', () => {
    const message = ErrorInterceptor.getErrorMessage(404, {});
    expect(message).toBe('Ressource non trouvée');
  });

  it('should redirect to login on 401 error', () => {
    ErrorInterceptor.handleErrorByStatus(401, router);
    expect(router.navigate).toHaveBeenCalledWith(['/']);
  });

  it('should redirect to forbidden on 403 error', () => {
    ErrorInterceptor.handleErrorByStatus(403, router);
    expect(router.navigate).toHaveBeenCalledWith(['/forbidden']);
  });

  it('should handle 404 error gracefully', () => {
    expect(() => ErrorInterceptor.handleErrorByStatus(404, router)).not.toThrow();
  });

  it('should retry once on error before throwing', (done) => {
    httpClient.get('/api/data').subscribe(
      () => fail('should have failed'),
      (error) => {
        expect(error).toBeTruthy();
        done();
      }
    );

    // First attempt
    const req1 = httpMock.expectOne('/api/data');
    req1.error(new ProgressEvent('error'), { status: 500 });

    // Retry attempt
    const req2 = httpMock.expectOne('/api/data');
    req2.error(new ProgressEvent('error'), { status: 500 });
  });
});
