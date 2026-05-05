import { inject, Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivateFn, Router, RouterStateSnapshot } from '@angular/router';
import { UserRole } from '../models/auth.model';
import { AuthService } from '../services/auth';

export const roleGuard: CanActivateFn = (
  route: ActivatedRouteSnapshot,
  state: RouterStateSnapshot
) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const requiredRoles: UserRole[] = route.data['roles'] || [];

  if (!authService.isAuthenticated()) {
    return router.createUrlTree(['/'], {
      queryParams: { returnUrl: state.url },
    });
  }

  if (requiredRoles.length === 0 || authService.hasRole(requiredRoles)) {
    return true;
  }

  router.navigate(['/forbidden']);
  return false;
};

@Injectable({
  providedIn: 'root',
})
export class RoleGuardService {
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    const requiredRoles: UserRole[] = route.data['roles'] || [];

    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/'], {
        queryParams: { returnUrl: state.url },
      });
      return false;
    }

    if (requiredRoles.length === 0 || this.authService.hasRole(requiredRoles)) {
      return true;
    }

    this.router.navigate(['/forbidden']);
    return false;
  }
}
