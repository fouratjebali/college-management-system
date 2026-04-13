import { Injectable } from '@angular/core';
import { CanActivateFn, Router, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { AuthService } from '../services/auth';

/**
 * Guard d'authentification - Vérifie que l'utilisateur est authentifié
 * Redirige vers la page de login si non authentifié
 */
export const authGuard: CanActivateFn = (route: ActivatedRouteSnapshot, state: RouterStateSnapshot) => {
  return inject(AuthService).isAuthenticated() 
    ? true 
    : inject(Router).createUrlTree(['/auth/login'], { queryParams: { returnUrl: state.url } });
};

/**
 * Service injectable version du guard pour plus de contrôle
 */
@Injectable({
  providedIn: 'root'
})
export class AuthGuardService {
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    if (this.authService.isAuthenticated()) {
      return true;
    }

    // Rediriger vers le login avec l'URL de retour
    this.router.navigate(['/auth/login'], {
      queryParams: { returnUrl: state.url }
    });
    return false;
  }
}

// Import inject depuis @angular/core
import { inject } from '@angular/core';
