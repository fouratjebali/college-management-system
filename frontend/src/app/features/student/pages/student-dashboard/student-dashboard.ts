import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../../core/services/auth';
import {
  StudentAnnouncementRow,
  StudentAbsenceSummary,
  StudentDashboardApi,
  StudentGradeSummary,
  StudentGradeRow,
  StudentMakeupRow,
  StudentMaterialRow,
  StudentProfile,
  StudentScheduleRow,
  StudentSubjectGrade,
} from '../../services/student-dashboard-api';

type StudentSection =
  | 'announcements'
  | 'grades'
  | 'schedule'
  | 'exams'
  | 'makeups'
  | 'absences'
  | 'materials';

interface NavItem {
  id: StudentSection;
  label: string;
  description: string;
}

interface ScheduleDayGroup {
  day: string;
  sessions: readonly StudentScheduleRow[];
}

interface NotificationItem {
  label: string;
  count: number;
  target: StudentSection;
}

@Component({
  selector: 'app-student-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './student-dashboard.html',
  styleUrl: './student-dashboard.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StudentDashboardComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly dashboardApi = inject(StudentDashboardApi);

  protected readonly activeSection = signal<StudentSection>('announcements');
  protected readonly searchTerm = signal('');
  protected readonly toastMessage = signal('Espace etudiant charge avec succes.');
  protected readonly isDashboardLoading = signal(true);

  protected readonly profile = signal<StudentProfile | null>(null);
  protected readonly gradeSummary = signal<StudentGradeSummary | null>(null);
  protected readonly subjectGrades = signal<readonly StudentSubjectGrade[]>([]);
  protected readonly grades = signal<readonly StudentGradeRow[]>([]);
  protected readonly schedule = signal<readonly StudentScheduleRow[]>([]);
  protected readonly materials = signal<readonly StudentMaterialRow[]>([]);
  protected readonly announcements = signal<readonly StudentAnnouncementRow[]>([]);
  protected readonly makeups = signal<readonly StudentMakeupRow[]>([]);
  protected readonly absenceSummaries = signal<readonly StudentAbsenceSummary[]>([]);

  protected readonly navItems: readonly NavItem[] = [
    {
      id: 'announcements',
      label: 'Annonces',
      description: 'Messages publies',
    },
    {
      id: 'grades',
      label: 'Notes',
      description: 'Notes publiees et moyenne',
    },
    {
      id: 'schedule',
      label: 'Emploi du temps',
      description: 'Seances par jour',
    },
    {
      id: 'exams',
      label: 'Examens',
      description: 'Calendrier planifie',
    },
    {
      id: 'makeups',
      label: 'Rattrapages',
      description: 'Seances supplementaires',
    },
    {
      id: 'absences',
      label: 'Absences',
      description: 'Suivi par matiere',
    },
    {
      id: 'materials',
      label: 'Supports',
      description: 'Documents de cours',
    },
  ];

  protected readonly filteredAbsenceSummaries = computed(() => {
    const query = this.searchTerm().trim().toLowerCase();

    return this.absenceSummaries().filter((summary) =>
      this.matchesQuery(query, [
        summary.subject,
        summary.professor,
        summary.typeSeance,
        summary.status,
      ])
    );
  });

  protected readonly filteredGrades = computed(() => {
    const query = this.searchTerm().trim().toLowerCase();
    return this.grades().filter((grade) =>
      this.matchesQuery(query, [
        grade.subject,
        grade.evaluation,
        grade.type,
        grade.status,
        grade.value,
      ])
    );
  });

  protected readonly filteredSubjectGrades = computed(() => {
    const query = this.searchTerm().trim().toLowerCase();
    return this.subjectGrades().filter((subjectGrade) =>
      this.matchesQuery(query, [
        subjectGrade.subject,
        subjectGrade.professor,
        subjectGrade.average,
        ...subjectGrade.evaluations.flatMap((evaluation) => [
          evaluation.type,
          evaluation.label,
          evaluation.value,
          evaluation.status,
        ]),
      ])
    );
  });

  protected readonly filteredSchedule = computed(() => {
    const query = this.searchTerm().trim().toLowerCase();
    return this.schedule().filter((session) =>
      !this.isExamSession(session) &&
      this.matchesQuery(query, [
        session.subject,
        session.professor,
        session.day,
        session.start,
        session.end,
        session.room,
        session.type,
      ])
    );
  });

  protected readonly filteredExams = computed(() => {
    const query = this.searchTerm().trim().toLowerCase();
    return this.schedule().filter((session) =>
      this.isExamSession(session) &&
      this.matchesQuery(query, [
        session.subject,
        session.professor,
        session.day,
        session.start,
        session.end,
        session.room,
        session.type,
      ])
    );
  });

  protected readonly filteredMaterials = computed(() => {
    const query = this.searchTerm().trim().toLowerCase();
    return this.materials().filter((material) =>
      this.matchesQuery(query, [
        material.title,
        material.subject,
        material.fileName,
        material.fileType,
      ])
    );
  });

  protected readonly filteredAnnouncements = computed(() => {
    const query = this.searchTerm().trim().toLowerCase();
    return this.announcements().filter((announcement) =>
      this.matchesQuery(query, [announcement.title, announcement.content, announcement.author])
    );
  });

  protected readonly filteredMakeups = computed(() => {
    const query = this.searchTerm().trim().toLowerCase();
    return this.makeups().filter((makeup) =>
      this.matchesQuery(query, [
        makeup.subject,
        makeup.professor,
        makeup.day,
        makeup.start,
        makeup.end,
        makeup.room,
      ])
    );
  });

  protected readonly notificationItems = computed<readonly NotificationItem[]>(() => [
    {
      label: 'Annonces',
      count: this.announcements().length,
      target: 'announcements',
    },
    {
      label: 'Notes publiees',
      count: this.grades().length,
      target: 'grades',
    },
    {
      label: 'Rattrapages',
      count: this.makeups().length,
      target: 'makeups',
    },
    {
      label: 'Examens',
      count: this.schedule().filter((session) => this.isExamSession(session)).length,
      target: 'exams',
    },
  ]);

  protected readonly notificationCount = computed(() =>
    this.notificationItems().reduce((total, item) => total + item.count, 0)
  );

  protected readonly scheduleDayGroups = computed<readonly ScheduleDayGroup[]>(() => {
    const dayOrder = ['Lundi', 'Mardi', 'Mercredi', 'Jeudi', 'Vendredi', 'Samedi'];
    const sessions = this.filteredSchedule();

    return dayOrder
      .map((day) => ({
        day,
        sessions: sessions
          .filter((session) => session.day.toLowerCase() === day.toLowerCase())
          .sort((first, second) => first.start.localeCompare(second.start)),
      }))
      .filter((group) => group.sessions.length > 0);
  });

  constructor() {
    this.loadDashboard();
  }

  private loadDashboard(): void {
    this.isDashboardLoading.set(true);

    this.dashboardApi.getDashboard().subscribe({
      next: (dashboard) => {
        this.profile.set(dashboard.profile ?? null);
        this.gradeSummary.set(dashboard.gradeSummary ?? null);
        this.subjectGrades.set(dashboard.subjectGrades ?? []);
        this.grades.set(dashboard.grades ?? []);
        this.schedule.set(dashboard.schedule ?? []);
        this.materials.set(dashboard.materials ?? []);
        this.announcements.set(dashboard.announcements ?? []);
        this.makeups.set(dashboard.makeups ?? []);
        this.absenceSummaries.set(dashboard.absenceSummaries ?? []);
        this.isDashboardLoading.set(false);
      },
      error: () => {
        this.clearDashboard();
        this.isDashboardLoading.set(false);
        this.toastMessage.set(
          'Impossible de charger les donnees etudiant. Verifiez la session puis reessayez.'
        );
      },
    });
  }

  protected setSection(section: StudentSection): void {
    this.activeSection.set(section);
  }

  protected logout(): void {
    this.authService.logout();
    this.router.navigate(['/']);
  }

  protected updateSearch(event: Event): void {
    this.searchTerm.set((event.target as HTMLInputElement).value);
  }

  protected downloadMaterial(material: StudentMaterialRow): void {
    this.dashboardApi.downloadMaterial(material.downloadUrl).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = material.fileName;
        link.click();
        URL.revokeObjectURL(url);
        this.toastMessage.set(`${material.title} est pret pour telechargement.`);
      },
      error: () => {
        this.toastMessage.set('Telechargement impossible pour ce support.');
      },
    });
  }

  private clearDashboard(): void {
    this.profile.set(null);
    this.gradeSummary.set(null);
    this.subjectGrades.set([]);
    this.grades.set([]);
    this.schedule.set([]);
    this.materials.set([]);
    this.announcements.set([]);
    this.makeups.set([]);
    this.absenceSummaries.set([]);
  }

  private matchesQuery(query: string, values: readonly string[]): boolean {
    return !query || values.some((value) => value.toLowerCase().includes(query));
  }

  private isExamSession(session: StudentScheduleRow): boolean {
    return session.type.toLowerCase().includes('examen');
  }

}
