import { Injectable } from '@angular/core';
import { CanActivateFn, Router, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { AuthService } from '../services/auth';
import { UserRole } from '../models/auth.model';

/**
 * Guard de rôle - Vérifie que l'utilisateur a les rôles requis
 * Utilisation : { path: 'admin', component: AdminComponent, canActivate: [roleGuard], data: { roles: ['ADMIN'] } }
 */
export const roleGuard: CanActivateFn = (route: ActivatedRouteSnapshot, state: RouterStateSnapshot) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // Récupérer les rôles requis depuis data.roles
  const requiredRoles: UserRole[] = route.data['roles'] || [];

  if (requiredRoles.length === 0) {
    // Si aucun rôle spécifié, accepter si authentifié
    return authService.isAuthenticated() ? true : redirectToLogin(router, state.url);
  }

  // Vérifier si l'utilisateur a l'un des rôles requis
  if (authService.hasRole(requiredRoles)) {
    return true;
  }

  // Accès refusé - rediriger vers 403
  router.navigate(['/forbidden']);
  return false;
};

/**
 * Service injectable version du guard pour plus de contrôle
 */
@Injectable({
  providedIn: 'root'
})
export class RoleGuardService {
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    // Récupérer les rôles requis
    const requiredRoles: UserRole[] = route.data['roles'] || [];

    if (requiredRoles.length === 0) {
      // Si aucun rôle spécifié, accepter si authentifié
      if (!this.authService.isAuthenticated()) {
        this.router.navigate(['/auth/login'], {
          queryParams: { returnUrl: state.url }
        });
        return false;
      }
      return true;
    }

    // Vérifier l'authentification
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/auth/login'], {
        queryParams: { returnUrl: state.url }
      });
      return false;
    }

    // Vérifier les rôles
    if (this.authService.hasRole(requiredRoles)) {
      return true;
    }

    // Accès refusé
    this.router.navigate(['/forbidden']);
    return false;
  }
}

/**
 * Factory helper pour rediriger vers login
 */
function redirectToLogin(router: Router, returnUrl: string): boolean {
  router.navigate(['/auth/login'], {
    queryParams: { returnUrl }
  });
  return false;
}

// Import inject depuis @angular/core
import { inject } from '@angular/core';
