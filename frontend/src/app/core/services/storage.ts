import { Injectable } from '@angular/core';
import { UserInfo } from '../models/auth.model';

/**
 * Interface pour les données d'authentification stockées
 */
export interface StoredAuthData {
  token: string;
  refreshToken: string;
  user: UserInfo;
}

/**
 * Service de gestion du localStorage
 * Gère la persistance sécurisée des données de session et du token JWT
 * Fournit une abstraction au-dessus du localStorage navigateur
 */
@Injectable({
  providedIn: 'root'
})
export class StorageService {
  private readonly PREFIX = 'app_';
  private readonly AUTH_KEY = `${this.PREFIX}auth`;
  private readonly TOKEN_KEY = `${this.PREFIX}token`;
  private readonly REFRESH_TOKEN_KEY = `${this.PREFIX}refresh_token`;
  private readonly USER_KEY = `${this.PREFIX}user`;

  constructor() {
    this.initializeStorage();
  }

  /**
   * Initialise le service de stockage
   * Nettoie les données obsolètes si nécessaire
   */
  private initializeStorage(): void {
    // Vérifier si le stockage est disponible
    if (!this.isStorageAvailable()) {
      console.warn('localStorage non disponible, utilisation de la mémoire uniquement');
    }
  }

  /**
   * Définit les données d'authentification (token + user)
   */
  setAuth(data: StoredAuthData): boolean {
    try {
      const authData = {
        token: data.token,
        refreshToken: data.refreshToken,
        user: data.user,
        storedAt: new Date().toISOString()
      };

      localStorage.setItem(this.AUTH_KEY, JSON.stringify(authData));
      return true;
    } catch (error) {
      console.error('Erreur stockage auth:', error);
      return false;
    }
  }

  /**
   * Récupère les données d'authentification stockées
   */
  getAuth(): StoredAuthData | null {
    try {
      const stored = localStorage.getItem(this.AUTH_KEY);
      if (!stored) return null;

      const parsed = JSON.parse(stored);
      return {
        token: parsed.token,
        refreshToken: parsed.refreshToken,
        user: parsed.user
      };
    } catch (error) {
      console.error('Erreur lecture auth:', error);
      return null;
    }
  }

  /**
   * Récupère uniquement le token d'accès
   */
  getToken(): string | null {
    try {
      const auth = this.getAuth();
      return auth?.token || null;
    } catch (error) {
      console.error('Erreur lecture token:', error);
      return null;
    }
  }

  /**
   * Récupère uniquement le refresh token
   */
  getRefreshToken(): string | null {
    try {
      const auth = this.getAuth();
      return auth?.refreshToken || null;
    } catch (error) {
      console.error('Erreur lecture refresh token:', error);
      return null;
    }
  }

  /**
   * Récupère uniquement les infos utilisateur stockées
   */
  getUser(): UserInfo | null {
    try {
      const auth = this.getAuth();
      return auth?.user || null;
    } catch (error) {
      console.error('Erreur lecture user:', error);
      return null;
    }
  }

  /**
   * Met à jour uniquement le token d'accès
   */
  setToken(token: string): boolean {
    try {
      const auth = this.getAuth();
      if (!auth) return false;

      auth.token = token;
      return this.setAuth(auth);
    } catch (error) {
      console.error('Erreur mise à jour token:', error);
      return false;
    }
  }

  /**
   * Nettoie toutes les données d'authentification
   */
  clearAuth(): boolean {
    try {
      localStorage.removeItem(this.AUTH_KEY);
      localStorage.removeItem(this.TOKEN_KEY);
      localStorage.removeItem(this.REFRESH_TOKEN_KEY);
      localStorage.removeItem(this.USER_KEY);
      return true;
    } catch (error) {
      console.error('Erreur nettoyage auth:', error);
      return false;
    }
  }

  /**
   * Définit une valeur générique dans le stockage
   */
  set(key: string, value: string | object): boolean {
    try {
      const prefixedKey = `${this.PREFIX}${key}`;
      const stringValue = typeof value === 'string' ? value : JSON.stringify(value);
      localStorage.setItem(prefixedKey, stringValue);
      return true;
    } catch (error) {
      console.error(`Erreur stockage ${key}:`, error);
      return false;
    }
  }

  /**
   * Récupère une valeur générique du stockage
   */
  get(key: string): string | null {
    try {
      const prefixedKey = `${this.PREFIX}${key}`;
      return localStorage.getItem(prefixedKey);
    } catch (error) {
      console.error(`Erreur lecture ${key}:`, error);
      return null;
    }
  }

  /**
   * Récupère et parse une valeur JSON du stockage
   */
  getObject<T>(key: string): T | null {
    try {
      const value = this.get(key);
      return value ? JSON.parse(value) : null;
    } catch (error) {
      console.error(`Erreur parse ${key}:`, error);
      return null;
    }
  }

  /**
   * Supprime une clé du stockage
   */
  remove(key: string): boolean {
    try {
      const prefixedKey = `${this.PREFIX}${key}`;
      localStorage.removeItem(prefixedKey);
      return true;
    } catch (error) {
      console.error(`Erreur suppression ${key}:`, error);
      return false;
    }
  }

  /**
   * Vérifie si une clé existe dans le stockage
   */
  has(key: string): boolean {
    try {
      const prefixedKey = `${this.PREFIX}${key}`;
      return localStorage.getItem(prefixedKey) !== null;
    } catch (error) {
      return false;
    }
  }

  /**
   * Vide entièrement le stockage (toutes les clés préfixées)
   */
  clear(): boolean {
    try {
      const keysToRemove: string[] = [];
      for (let i = 0; i < localStorage.length; i++) {
        const key = localStorage.key(i);
        if (key && key.startsWith(this.PREFIX)) {
          keysToRemove.push(key);
        }
      }

      keysToRemove.forEach(key => localStorage.removeItem(key));
      return true;
    } catch (error) {
      console.error('Erreur nettoyage complet:', error);
      return false;
    }
  }

  /**
   * Retourne tous les items du stockage (avec le prefix)
   */
  getAllKeys(): string[] {
    try {
      const keys: string[] = [];
      for (let i = 0; i < localStorage.length; i++) {
        const key = localStorage.key(i);
        if (key && key.startsWith(this.PREFIX)) {
          // Retourner sans le prefix
          keys.push(key.replace(this.PREFIX, ''));
        }
      }
      return keys;
    } catch (error) {
      console.error('Erreur lecture clés:', error);
      return [];
    }
  }

  /**
   * Vérifie la disponibilité de localStorage
   */
  private isStorageAvailable(): boolean {
    try {
      const test = '__localStorage_test__';
      localStorage.setItem(test, test);
      localStorage.removeItem(test);
      return true;
    } catch (error) {
      return false;
    }
  }

  /**
   * Obtient la taille (approximativement) du stockage utilisé
   */
  getStorageSize(): number {
    let size = 0;
    try {
      for (let i = 0; i < localStorage.length; i++) {
        const key = localStorage.key(i);
        if (key && key.startsWith(this.PREFIX)) {
          const value = localStorage.getItem(key);
          if (value) {
            size += key.length + value.length;
          }
        }
      }
    } catch (error) {
      console.error('Erreur calcul taille:', error);
    }
    return size;
  }

  /**
   * Export des données pour debug
   */
  exportData(): object {
    try {
      const data: any = {};
      for (let i = 0; i < localStorage.length; i++) {
        const key = localStorage.key(i);
        if (key && key.startsWith(this.PREFIX)) {
          const cleanKey = key.replace(this.PREFIX, '');
          const value = localStorage.getItem(key);
          try {
            data[cleanKey] = value ? JSON.parse(value) : value;
          } catch {
            data[cleanKey] = value;
          }
        }
      }
      return data;
    } catch (error) {
      console.error('Erreur export:', error);
      return {};
    }
  }
}
