import { Injectable } from '@angular/core';
import {
  HttpInterceptor,
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptorFn,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, finalize } from 'rxjs/operators';
import { AuthService } from '../services/auth';
import { Router } from '@angular/router';

/**
 * Interceptor HTTP moderne pour injection du JWT
 * Ajoute le token d'authentification aux en-têtes de toutes les requêtes
 */
export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);

  // Récupérer le token du service d'authentification
  const token = authService.getToken();

  // Si le token existe, l'ajouter au header Authorization
  if (token) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      // Gérer les erreurs 401 (Non autorisé)
      if (error.status === 401) {
        // Token expiré ou invalide
        authService.logout(); // Sera implémentée dans feature/auth-service
        // Optionnel : rediriger vers login
      }

      // Re-lever l'erreur pour que le composant puisse la traiter
      return throwError(() => error);
    })
  );
};

/**
 * Import inject depuis @angular/core
 */
import { inject } from '@angular/core';

/**
 * Classe d'interceptor pour injection dans les anciennes versions d'Angular
 * Utilisable en alternative à la fonction jwtInterceptor
 */
@Injectable()
export class JwtInterceptorClass implements HttpInterceptor {
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // Récupérer le token
    const token = this.authService.getToken();

    // Si token existe, l'ajouter
    if (token) {
      req = this.addTokenToHeaders(req, token);
    }

    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {
        return this.handleError(error);
      })
    );
  }

  /**
   * Ajoute le token au header Authorization
   */
  private addTokenToHeaders(req: HttpRequest<any>, token: string): HttpRequest<any> {
    return req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  /**
   * Gère les erreurs HTTP
   */
  private handleError(error: HttpErrorResponse): Observable<never> {
    if (error.status === 401) {
      // Token expiré
      console.warn('Token expiré, déconnexion...');
      this.authService.logout();
      // Optionel : rediriger
      // this.router.navigate(['/auth/login']);
    }

    if (error.status === 403) {
      // Accès interdit
      console.warn('Accès interdit');
    }

    return throwError(() => error);
  }
}
