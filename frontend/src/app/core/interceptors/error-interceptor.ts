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
import { catchError, retry } from 'rxjs/operators';
import { inject } from '@angular/core';
import { Router } from '@angular/router';

/**
 * Interceptor HTTP moderne pour gestion des erreurs
 * Centralise la gestion des codes d'erreur HTTP
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);

  return next(req).pipe(
    retry(1), // Réessayer 1 fois en cas d'erreur temporaire
    catchError((error: HttpErrorResponse) => {
      let errorMessage = 'Une erreur est survenue';

      if (error.error instanceof ErrorEvent) {
        // Erreur côté client
        errorMessage = `Erreur: ${error.error.message}`;
        console.error('Erreur client:', error.error);
      } else {
        // Erreur côté serveur
        errorMessage = ErrorInterceptor.getErrorMessage(error.status, error.error);
        console.error(`Erreur HTTP ${error.status}:`, error.error);
      }

      // Logger l'erreur (en production, envoyer à un service de logging)
      console.error('Erreur complète:', {
        status: error.status,
        message: errorMessage,
        url: error.url,
        timestamp: new Date().toISOString()
      });

      // Actions spécifiques selon le code d'erreur
      ErrorInterceptor.handleErrorByStatus(error.status, router);

      return throwError(() => ({
        status: error.status,
        message: errorMessage,
        details: error.error
      }));
    })
  );
};

/**
 * Classe d'interceptor pour injection dans les anciennes versions d'Angular
 */
@Injectable()
export class ErrorInterceptorClass implements HttpInterceptor {
  constructor(private router: Router) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req).pipe(
      retry(1),
      catchError((error: HttpErrorResponse) => {
        const errorMessage = ErrorInterceptor.getErrorMessage(error.status, error.error);

        console.error(`Erreur HTTP ${error.status}:`, errorMessage);

        // Action spécifiques
        ErrorInterceptor.handleErrorByStatus(error.status, this.router);

        return throwError(() => ({
          status: error.status,
          message: errorMessage,
          details: error.error
        }));
      })
    );
  }
}

/**
 * Classe utilitaire pour la gestion des erreurs
 */
export class ErrorInterceptor {
  /**
   * Retourne un message d'erreur approprié selon le code HTTP
   */
  static getErrorMessage(status: number, error: any): string {
    const errorMessages: { [key: number]: string } = {
      400: 'Requête invalide',
      401: 'Non authentifié - Pour accéder, connectez-vous',
      403: 'Accès refusé - Vous n\'avez pas les permissions',
      404: 'Ressource non trouvée',
      409: 'Conflit - Les données existent déjà',
      422: 'Données invalides - Vérifiez votre saisie',
      429: 'Trop de requêtes - Veuillez patienter',
      500: 'Erreur serveur interne',
      502: 'Mauvaise passerelle - Serveur indisponible',
      503: 'Service indisponible - Veuillez réessayer plus tard',
      504: 'Timeout serveur - Veuillez réessayer'
    };

    return errorMessages[status]
      || (error?.message
        || error?.error
        || error?.detail
        || `Erreur ${status} non gérée`);
  }

  /**
   * Effectue les actions spécifiques selon le code d'erreur
   */
  static handleErrorByStatus(status: number, router: Router): void {
    switch (status) {
      case 401:
        // Non authentifié - rediriger vers login
        console.warn('Session expirée, redirection vers login');
        router.navigate(['/auth/login']);
        break;

      case 403:
        // Accès refusé - rediriger vers forbidden
        console.warn('Accès refusé, redirection vers forbidden');
        router.navigate(['/forbidden']);
        break;

      case 404:
        // Ressource non trouvée
        console.warn('Ressource non trouvée');
        break;

      case 500:
      case 502:
      case 503:
      case 504:
        // Erreurs serveur graves
        console.error('Erreur serveur grave, contact admin recommandé');
        // Optionnel : rediriger vers page d'erreur
        // router.navigate(['/error']);
        break;

      default:
        // Autres erreurs
        break;
    }
  }
}
