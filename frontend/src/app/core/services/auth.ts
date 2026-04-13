import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { AuthState, UserInfo, UserRole } from '../models/auth.model';

/**
 * Service d'authentification Angular
 * Stub version - sera complété dans feature/auth-service
 */
@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private authState$ = new BehaviorSubject<AuthState>({
    isAuthenticated: false,
    user: null,
    token: null,
    refreshToken: null,
    loading: false,
    error: null
  });

  constructor() {
    // Initialisation du state depuis le token stocké
    this.initializeAuthState();
  }

  /**
   * Observable de l'état d'authentification
   */
  getAuthState(): Observable<AuthState> {
    return this.authState$.asObservable();
  }

  /**
   * Récupère l'état actuel
   */
  getCurrentState(): AuthState {
    return this.authState$.value;
  }

  /**
   * Récupère l'utilisateur actuel
   */
  getCurrentUser(): UserInfo | null {
    return this.authState$.value.user;
  }

  /**
   * Récupère le token actuel
   */
  getToken(): string | null {
    return this.authState$.value.token;
  }

  /**
   * Vérifie si l'utilisateur est authentifié
   */
  isAuthenticated(): boolean {
    return this.authState$.value.isAuthenticated;
  }

  /**
   * Vérifie si l'utilisateur a un rôle spécifique
   */
  hasRole(role: UserRole | UserRole[]): boolean {
    const user = this.authState$.value.user;
    if (!user) return false;

    if (Array.isArray(role)) {
      return role.includes(user.role);
    }
    return user.role === role;
  }

  /**
   * Initialise l'état d'authentification depuis le stockage
   */
  private initializeAuthState(): void {
    // Sera implémentée dans feature/token-storage
    // Pour maintenant, juste initialiser le state par défaut
  }

  /**
   * Met à jour l'état d'authentification
   */
  protected updateAuthState(partialState: Partial<AuthState>): void {
    this.authState$.next({
      ...this.authState$.value,
      ...partialState
    });
  }
}
