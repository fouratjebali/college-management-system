/**
 * Modèle de réponse d'authentification
 */
export interface AuthResponse {
  token: string;
  refreshToken: string;
  user: UserInfo;
}

/**
 * Informations utilisateur dans le token
 */
export interface UserInfo {
  id: number;
  email: string;
  nomComplet: string;
  role: UserRole;
}

/**
 * Rôles utilisateur disponibles
 */
export enum UserRole {
  STUDENT = 'STUDENT',
  PROFESSOR = 'PROFESSOR',
  ADMIN = 'ADMIN'
}

/**
 * Modèle de token décodé
 */
export interface TokenPayload {
  sub: string; // Subject (user ID)
  email: string;
  role: UserRole;
  nomComplet: string;
  iat: number; // Issued at
  exp: number; // Expiration
}

/**
 * Requête de connexion
 */
export interface LoginRequest {
  email: string;
  password: string;
}

/**
 * État d'authentification
 */
export interface AuthState {
  isAuthenticated: boolean;
  user: UserInfo | null;
  token: string | null;
  refreshToken: string | null;
  loading: boolean;
  error: string | null;
}
