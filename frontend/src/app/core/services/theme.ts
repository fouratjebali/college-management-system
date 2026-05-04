import { DOCUMENT } from '@angular/common';
import { Injectable, computed, effect, inject, signal } from '@angular/core';

export type ThemeMode = 'dark' | 'light';

@Injectable({
  providedIn: 'root',
})
export class ThemeService {
  private readonly document = inject(DOCUMENT);
  private readonly storageKey = 'issatso-theme';

  readonly mode = signal<ThemeMode>(this.readInitialMode());
  readonly label = computed(() => (this.mode() === 'dark' ? 'Light' : 'Dark'));

  constructor() {
    effect(() => {
      const mode = this.mode();
      const root = this.document.documentElement;
      root.dataset['theme'] = mode;
      root.style.colorScheme = mode;
      this.persist(mode);
    });
  }

  toggle(): void {
    this.mode.update((mode) => (mode === 'dark' ? 'light' : 'dark'));
  }

  private readInitialMode(): ThemeMode {
    try {
      const saved = localStorage.getItem(this.storageKey);
      if (saved === 'dark' || saved === 'light') {
        return saved;
      }
    } catch {
      // Ignore unavailable storage and keep the default theme.
    }

    return 'dark';
  }

  private persist(mode: ThemeMode): void {
    try {
      localStorage.setItem(this.storageKey, mode);
    } catch {
      // Ignore unavailable storage.
    }
  }
}
