import { CommonModule } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  Input,
  Output,
  computed,
  signal,
} from '@angular/core';

export type NotificationPriority = 'critical' | 'high' | 'normal' | 'low';

export interface NotificationCenterItem {
  id: string;
  title: string;
  description: string;
  category: string;
  meta: string;
  priority: NotificationPriority;
  target: string;
}

@Component({
  selector: 'app-notification-center',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './notification-center.html',
  styleUrl: './notification-center.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NotificationCenterComponent {
  @Output() readonly notificationSelected = new EventEmitter<NotificationCenterItem>();

  protected readonly open = signal(false);
  protected readonly activeFilter = signal<'all' | 'unread'>('all');
  private readonly itemsSignal = signal<readonly NotificationCenterItem[]>([]);
  private readonly readIds = signal<readonly string[]>([]);
  private storageNamespace = 'notifications';

  @Input()
  set storageKey(value: string) {
    this.storageNamespace = value || 'notifications';
    this.readIds.set(this.loadReadIds());
  }

  @Input()
  set items(value: readonly NotificationCenterItem[] | null) {
    this.itemsSignal.set(value ?? []);
  }

  protected readonly sortedItems = computed(() =>
    [...this.itemsSignal()].sort((first, second) => this.priorityRank(second) - this.priorityRank(first))
  );

  protected readonly unreadItems = computed(() =>
    this.sortedItems().filter((item) => !this.readIds().includes(item.id))
  );

  protected readonly filteredItems = computed(() =>
    this.activeFilter() === 'unread' ? this.unreadItems() : this.sortedItems()
  );

  protected readonly unreadCount = computed(() => this.unreadItems().length);

  protected readonly criticalCount = computed(() =>
    this.unreadItems().filter((item) => item.priority === 'critical' || item.priority === 'high').length
  );

  protected toggleOpen(): void {
    this.open.update((current) => !current);
  }

  protected setFilter(filter: 'all' | 'unread'): void {
    this.activeFilter.set(filter);
  }

  protected selectItem(item: NotificationCenterItem): void {
    this.markAsRead(item.id);
    this.open.set(false);
    this.notificationSelected.emit(item);
  }

  protected markAllAsRead(): void {
    const ids = this.sortedItems().map((item) => item.id);
    this.readIds.set(ids);
    this.persistReadIds(ids);
  }

  protected isUnread(item: NotificationCenterItem): boolean {
    return !this.readIds().includes(item.id);
  }

  private markAsRead(id: string): void {
    if (this.readIds().includes(id)) {
      return;
    }

    const ids = [...this.readIds(), id];
    this.readIds.set(ids);
    this.persistReadIds(ids);
  }

  private priorityRank(item: NotificationCenterItem): number {
    const ranks: Record<NotificationPriority, number> = {
      low: 1,
      normal: 2,
      high: 3,
      critical: 4,
    };

    return ranks[item.priority];
  }

  private loadReadIds(): readonly string[] {
    try {
      return JSON.parse(localStorage.getItem(this.storageNamespace) ?? '[]') as string[];
    } catch {
      return [];
    }
  }

  private persistReadIds(ids: readonly string[]): void {
    localStorage.setItem(this.storageNamespace, JSON.stringify(ids));
  }
}
