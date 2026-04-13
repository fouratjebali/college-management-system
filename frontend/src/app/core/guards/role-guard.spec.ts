import { TestBed } from '@angular/core/testing';
import { Router, ActivatedRouteSnapshot } from '@angular/router';
import { AuthService } from '../services/auth';
import { RoleGuardService } from './role-guard';
import { UserRole } from '../models/auth.model';

describe('RoleGuardService', () => {
  let service: RoleGuardService;
  let authService: jasmine.SpyObj<AuthService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(() => {
    const authServiceSpy = jasmine.createSpyObj('AuthService', ['isAuthenticated', 'hasRole']);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [
        RoleGuardService,
        { provide: AuthService, useValue: authServiceSpy },
        { provide: Router, useValue: routerSpy }
      ]
    });

    service = TestBed.inject(RoleGuardService);
    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should allow access when user is authenticated with required role', () => {
    authService.isAuthenticated.and.returnValue(true);
    authService.hasRole.and.returnValue(true);

    const route = { data: { roles: [UserRole.ADMIN] } } as any;
    const state = { url: '/admin' } as any;

    const result = service.canActivate(route, state);
    expect(result).toBe(true);
  });

  it('should deny access when user has no required role', () => {
    authService.isAuthenticated.and.returnValue(true);
    authService.hasRole.and.returnValue(false);

    const route = { data: { roles: [UserRole.ADMIN] } } as any;
    const state = { url: '/admin' } as any;

    const result = service.canActivate(route, state);
    expect(result).toBe(false);
    expect(router.navigate).toHaveBeenCalledWith(['/forbidden']);
  });

  it('should redirect to login when user is not authenticated', () => {
    authService.isAuthenticated.and.returnValue(false);

    const route = { data: { roles: [UserRole.ADMIN] } } as any;
    const state = { url: '/admin' } as any;

    const result = service.canActivate(route, state);
    expect(result).toBe(false);
    expect(router.navigate).toHaveBeenCalledWith(
      ['/auth/login'],
      { queryParams: { returnUrl: '/admin' } }
    );
  });
});
