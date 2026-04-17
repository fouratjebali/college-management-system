import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, throwError } from 'rxjs';
import { map, tap, catchError } from 'rxjs/operators';
import { AuthResponse, LoginRequest, UserInfo, UserRole, TokenPayload, AuthState } from '../models/auth.model';
import { StorageService } from './storage';

/**
 * Service API pour l'authentification
 * Communication avec le backend
 */
@Injectable({
  providedIn: 'root'
})
export class AuthApiService {
  private apiUrl = '/api/auth'; // Sera préfixé par le proxy

  constructor(private http: HttpClient) {}

  /**
   * Envoie les credentials au backend pour authentification
   */
  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, request);
  }

  adminLogin(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/admin/login`, request);
  }

  /**
   * Envoie une requête de rafraîchissement du token
   */
  refreshToken(refreshToken: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/refresh`, { refreshToken });
  }

  /**
   * Valide le token auprès du backend
   */
  validateToken(token: string): Observable<{ valid: boolean }> {
    return this.http.post<{ valid: boolean }>(`${this.apiUrl}/validate`, { token });
  }

  /**
   * Récupère les infos de l'utilisateur actuel
   */
  getCurrentUser(): Observable<UserInfo> {
    return this.http.get<UserInfo>(`${this.apiUrl}/me`);
  }
}

/**
 * Service d'authentification principal
 * Gère l'état d'authentification et les interactions avec l'API
 * Version 2.0 - Implémentation complète
 */
@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiService = inject(AuthApiService);
  private storageService = inject(StorageService);

  private authState$ = new BehaviorSubject<AuthState>({
    isAuthenticated: false,
    user: null,
    token: null,
    refreshToken: null,
    loading: false,
    error: null
  });

  private loginAttempts = 0;
  private readonly MAX_LOGIN_ATTEMPTS = 5;
  private readonly LOCKOUT_TIME = 15 * 60 * 1000; // 15 minutes

  constructor() {
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
   * Récupère le token d'accès
   */
  getToken(): string | null {
    return this.authState$.value.token;
  }

  /**
   * Récupère le refresh token
   */
  getRefreshToken(): string | null {
    return this.authState$.value.refreshToken;
  }

  /**
   * Vérifie si l'utilisateur est authentifié
   */
  isAuthenticated(): boolean {
    return this.authState$.value.isAuthenticated && !this.isTokenExpired(this.authState$.value.token);
  }

  /**
   * Vérifie si l'utilisateur a un rôle spécifique
   */
  hasRole(roles: UserRole | UserRole[]): boolean {
    const user = this.authState$.value.user;
    if (!user) return false;

    if (typeof roles === 'string') {
      return user.role === roles;
    }
    return (roles as UserRole[]).includes(user.role);
  }

  /**
   * Authentifie l'utilisateur avec email et mot de passe
   */
  login(email: string, password: string): Observable<AuthResponse> {
    this.updateAuthState({ loading: true, error: null });

    // Vérifier le lockout
    if (this.isLockedOut()) {
      const error = 'Trop de tentatives. Compte verrouillé 15 minutes.';
      this.updateAuthState({ loading: false, error });
      return throwError(() => new Error(error));
    }

    return this.apiService.login({ email, password }).pipe(
      tap((response: AuthResponse) => {
        this.loginAttempts = 0; // Réinitialiser le compteur
        this.handleAuthResponse(response);
      }),
      catchError((error) => {
        this.loginAttempts++;
        this.storageService.set('lastLoginAttempt', Date.now().toString());
        const errorMsg = error.error?.message || 'Erreur de connexion';
        this.updateAuthState({
          loading: false,
          error: errorMsg,
          isAuthenticated: false
        });
        return throwError(() => error);
      })
    );
  }

  adminLogin(email: string, password: string): Observable<AuthResponse> {
    this.updateAuthState({ loading: true, error: null });

    return this.apiService.adminLogin({ email, password }).pipe(
      tap((response: AuthResponse) => {
        this.handleAuthResponse(response);
      }),
      catchError((error) => {
        const errorMsg = error.error?.error || error.error?.message || 'Acces admin refuse';
        this.updateAuthState({
          loading: false,
          error: errorMsg,
          isAuthenticated: false
        });
        return throwError(() => error);
      })
    );
  }

  /**
   * Déconnexion de l'utilisateur
   */
  logout(): void {
    console.log('Déconnexion de l\'utilisateur');
    this.storageService.clearAuth();
    this.updateAuthState({
      isAuthenticated: false,
      user: null,
      token: null,
      refreshToken: null,
      error: null
    });
  }

  /**
   * Rafraîchit le token d'accès
   */
  refreshAccessToken(): Observable<AuthResponse> {
    const refreshToken = this.getRefreshToken();
    if (!refreshToken) {
      return throwError(() => new Error('No refresh token available'));
    }

    return this.apiService.refreshToken(refreshToken).pipe(
      tap((response: AuthResponse) => {
        this.handleAuthResponse(response);
      }),
      catchError((error) => {
        this.logout();
        return throwError(() => error);
      })
    );
  }

  /**
   * Initialise l'état d'authentification depuis le stockage
   */
  private initializeAuthState(): void {
    const stored = this.storageService.getAuth();
    if (stored && stored.token) {
      // Vérifier si le token n'est pas expiré
      if (this.isTokenExpired(stored.token)) {
        this.logout();
      } else {
        // Restaurer l'état depuis le stockage
        this.updateAuthState({
          isAuthenticated: true,
          user: stored.user,
          token: stored.token,
          refreshToken: stored.refreshToken
        });
      }
    }
  }

  /**
   * Traite la réponse d'authentification et met à jour l'état
   */
  private handleAuthResponse(response: AuthResponse): void {
    // Stocker auth et mettre à jour l'état
    this.storageService.setAuth({
      token: response.token,
      refreshToken: response.refreshToken,
      user: response.user
    });

    this.updateAuthState({
      isAuthenticated: true,
      user: response.user,
      token: response.token,
      refreshToken: response.refreshToken,
      loading: false,
      error: null
    });
  }

  /**
   * Décode le JWT et récupère le payload
   */
  private decodeToken(token: string | null): TokenPayload | null {
    if (!token) return null;

    try {
      const parts = token.split('.');
      if (parts.length !== 3) return null;

      const decoded = JSON.parse(atob(parts[1]));
      return decoded as TokenPayload;
    } catch (error) {
      console.error('Erreur décodage token:', error);
      return null;
    }
  }

  /**
   * Vérifie si le token est expiré
   */
  private isTokenExpired(token: string | null): boolean {
    if (!token) return true;

    const payload = this.decodeToken(token);
    if (!payload) return true;

    const now = Math.floor(Date.now() / 1000);
    // Ajouter un buffer de 60 secondes avant expiration
    return payload.exp <= (now + 60);
  }

  /**
   * Vérifie si l'utilisateur est bloqué (après trop de tentatives)
   */
  private isLockedOut(): boolean {
    if (this.loginAttempts < this.MAX_LOGIN_ATTEMPTS) {
      return false;
    }

    // Vérifier si le lockout a expiré
    const lastAttempt = this.storageService.get('lastLoginAttempt');
    if (!lastAttempt) {
      return true;
    }

    const timeSinceLastAttempt = Date.now() - parseInt(lastAttempt, 10);
    if (timeSinceLastAttempt > this.LOCKOUT_TIME) {
      this.loginAttempts = 0;
      this.storageService.remove('lastLoginAttempt');
      return false;
    }

    return true;
  }

  /**
   * Met à jour l'état d'authentification
   */
  private updateAuthState(partialState: Partial<AuthState>): void {
    this.authState$.next({
      ...this.authState$.value,
      ...partialState
    });
  }
}
