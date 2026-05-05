import { TestBed } from '@angular/core/testing';
import { StorageService, StoredAuthData } from './storage';
import { UserRole } from '../models/auth.model';

describe('StorageService', () => {
  let service: StorageService;

  const mockUser = {
    id: 1,
    email: 'test@example.com',
    nomComplet: 'Test User',
    role: UserRole.STUDENT
  };

  const mockAuthData: StoredAuthData = {
    token: 'test-token',
    refreshToken: 'test-refresh-token',
    user: mockUser
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [StorageService]
    });

    service = TestBed.inject(StorageService);

    // Clear localStorage avant chaque test
    localStorage.clear();
  });

  afterEach(() => {
    localStorage.clear();
  });

  describe('Auth storage', () => {
    it('should set and get auth data', () => {
      service.setAuth(mockAuthData);
      const retrieved = service.getAuth();

      expect(retrieved).toEqual(mockAuthData);
    });

    it('should set and get token', () => {
      service.setAuth(mockAuthData);
      const token = service.getToken();

      expect(token).toBe(mockAuthData.token);
    });

    it('should set and get refresh token', () => {
      service.setAuth(mockAuthData);
      const refreshToken = service.getRefreshToken();

      expect(refreshToken).toBe(mockAuthData.refreshToken);
    });

    it('should set and get user', () => {
      service.setAuth(mockAuthData);
      const user = service.getUser();

      expect(user).toEqual(mockUser);
    });

    it('should clear auth data', () => {
      service.setAuth(mockAuthData);
      service.clearAuth();

      expect(service.getAuth()).toBeNull();
      expect(service.getToken()).toBeNull();
    });

    it('should return null when no auth data stored', () => {
      expect(service.getAuth()).toBeNull();
      expect(service.getToken()).toBeNull();
    });

    it('should update token while keeping other data', () => {
      service.setAuth(mockAuthData);
      const newToken = 'new-token';
      service.setToken(newToken);

      const retrieved = service.getAuth();
      expect(retrieved?.token).toBe(newToken);
      expect(retrieved?.user).toEqual(mockUser);
    });
  });

  describe('Generic storage', () => {
    it('should set and get string value', () => {
      service.set('test_key', 'test_value');
      expect(service.get('test_key')).toBe('test_value');
    });

    it('should set and get object value', () => {
      const testObj = { name: 'Test', age: 30 };
      service.set('test_obj', testObj);
      const retrieved = service.getObject('test_obj');

      expect(retrieved).toEqual(testObj);
    });

    it('should remove value', () => {
      service.set('test_key', 'test_value');
      service.remove('test_key');

      expect(service.get('test_key')).toBeNull();
    });

    it('should check if key exists', () => {
      service.set('existing_key', 'value');

      expect(service.has('existing_key')).toBe(true);
      expect(service.has('non_existing_key')).toBe(false);
    });
  });

  describe('Bulk operations', () => {
    it('should clear all stored data', () => {
      service.set('key1', 'value1');
      service.set('key2', 'value2');
      service.setAuth(mockAuthData);

      service.clear();

      expect(service.get('key1')).toBeNull();
      expect(service.get('key2')).toBeNull();
      expect(service.getAuth()).toBeNull();
    });

    it('should get all keys', () => {
      service.set('key1', 'value1');
      service.set('key2', 'value2');
      service.setAuth(mockAuthData);

      const keys = service.getAllKeys();

      expect(keys.length).toBeGreaterThan(0);
      expect(keys).toContain('key1');
      expect(keys).toContain('key2');
    });

    it('should export all data', () => {
      service.set('key1', 'value1');
      service.setAuth(mockAuthData);

      const exported = service.exportData();

      expect(exported).toBeTruthy();
      expect((exported as any).auth).toBeTruthy();
    });

    it('should calculate storage size', () => {
      service.set('key1', 'value1');
      service.set('key2', 'value2');

      const size = service.getStorageSize();

      expect(size).toBeGreaterThan(0);
    });
  });

  describe('Edge cases', () => {
    it('should handle missing auth data when getting token', () => {
      expect(service.getToken()).toBeNull();
    });

    it('should handle invalid JSON gracefully', () => {
      localStorage.setItem('app_test_key', 'invalid-json-not-parseable');
      const result = service.getObject('test_key');

      expect(result).toBeNull();
    });

    it('should use prefix for all keys', () => {
      service.set('my_key', 'value');

      // Check that localStorage has the prefixed key
      const allKeys = Object.keys(localStorage);
      const prefixedKey = allKeys.find(k => k.includes('app_my_key'));

      expect(prefixedKey).toBeTruthy();
    });
  });
});
