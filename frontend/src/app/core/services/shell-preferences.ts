import { Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class ShellPreferencesService {
  private readonly storageKey = 'issatso-sidebar-collapsed';

  readonly sidebarCollapsed = signal(this.readInitialState());

  toggleSidebar(): void {
    this.sidebarCollapsed.update((collapsed) => {
      const next = !collapsed;
      this.persist(next);
      return next;
    });
  }

  private readInitialState(): boolean {
    try {
      return localStorage.getItem(this.storageKey) === 'true';
    } catch {
      return false;
    }
  }

  private persist(collapsed: boolean): void {
    try {
      localStorage.setItem(this.storageKey, String(collapsed));
    } catch {
      // Ignore unavailable storage.
    }
  }
}
