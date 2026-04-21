import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../../core/services/auth';
import { AdminDashboardApi } from '../../services/admin-dashboard-api';

type AdminSection = 'overview' | 'structure' | 'users' | 'calendar';
type PlanningMode = 'single' | 'weekly';

interface NavItem {
  id: AdminSection;
  label: string;
  description: string;
}

interface StatCard {
  label: string;
  value: string;
  trend: string;
  tone: 'light' | 'warm' | 'steel' | 'sand';
}

interface AcademicRow {
  code: string;
  title: string;
  meta: string;
  status: string;
}

interface UserRow {
  name: string;
  email: string;
  role: string;
  status: 'Active' | 'Pending';
  department: string;
  group: string;
  specialty: string;
}

interface ExamRow {
  subject: string;
  group: string;
  date: string;
  room: string;
  type: string;
  scope: string;
}

interface ProfessorDepartmentGroup {
  department: string;
  professors: readonly UserRow[];
}

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminDashboardComponent {
  private readonly formBuilder = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly dashboardApi = inject(AdminDashboardApi);

  protected readonly activeSection = signal<AdminSection>('overview');
  protected readonly searchTerm = signal('');
  protected readonly toastMessage = signal('Espace admin charge avec succes.');
  protected readonly selectedDepartment = signal('Tous');
  protected readonly selectedGroup = signal('Tous');
  protected readonly selectedRole = signal('Tous');
  protected readonly editingUserEmail = signal<string | null>(null);
  protected readonly planningMode = signal<PlanningMode>('single');
  protected readonly isDashboardLoading = signal(true);

  protected readonly navItems: readonly NavItem[] = [
    {
      id: 'overview',
      label: 'Dashboard',
      description: 'Statistiques et activite recente',
    },
    {
      id: 'structure',
      label: 'Structure',
      description: 'Departements, matieres et groupes',
    },
    {
      id: 'users',
      label: 'Utilisateurs',
      description: 'Comptes, roles et statuts',
    },
    {
      id: 'calendar',
      label: 'Examens & Notes',
      description: 'Calendrier et publication',
    },
  ];

  protected readonly stats = signal<readonly StatCard[]>([]);

  protected readonly activitySeries = computed(() => {
    const stats = this.stats();
    const values = stats.map((stat) => this.parseStatValue(stat.value));
    const maxValue = Math.max(...values, 0);

    if (stats.length === 0 || maxValue === 0) {
      return [];
    }

    return stats.map((stat, index) => ({
      label: stat.label,
      value: Math.max(8, Math.round((values[index] / maxValue) * 100)),
    }));
  });

  protected readonly quickActions = [
    'Creer un departement',
    'Ajouter un utilisateur',
    'Planifier un examen',
    'Publier les notes',
  ] as const;

  protected readonly academicRows = signal<readonly AcademicRow[]>([]);

  protected readonly users = signal<readonly UserRow[]>([]);

  protected readonly examRows = signal<readonly ExamRow[]>([]);

  protected readonly filteredUsers = computed(() => {
    const query = this.searchTerm().trim().toLowerCase();
    const department = this.selectedDepartment();
    const group = this.selectedGroup();
    const role = this.selectedRole();

    return this.users().filter((user) => {
      const matchesQuery =
        !query ||
        [user.name, user.email, user.role, user.status, user.department, user.group, user.specialty].some(
          (value) => value.toLowerCase().includes(query)
        );
      const matchesDepartment = department === 'Tous' || user.department === department;
      const matchesRole = role === 'Tous' || user.role === role;
      const matchesGroup =
        group === 'Tous' ||
        user.group === group ||
        (user.role === 'Professeur' && group === 'Tous les groupes');

      return matchesQuery && matchesDepartment && matchesRole && matchesGroup;
    });
  });

  protected readonly departmentOptions = computed(() => {
    const departments = new Set<string>();

    this.academicRows().forEach((row) => {
      if (row.title) {
        departments.add(row.title);
      }
    });

    this.users().forEach((user) => {
      if (user.department && user.department !== 'Non affecte') {
        departments.add(user.department);
      }
    });

    return ['Tous', ...Array.from(departments).sort((first, second) => first.localeCompare(second))];
  });

  protected readonly groupOptions = computed(() => {
    const department = this.selectedDepartment();
    const scopedUsers =
      department === 'Tous'
        ? this.users()
        : this.users().filter((user) => user.department === department);

    return [
      'Tous',
      ...Array.from(new Set(scopedUsers.map((user) => user.group))).filter(
        (group) => group && group !== 'Non affecte'
      ),
    ];
  });

  protected readonly departmentFormOptions = computed(() =>
    this.departmentOptions().filter(
      (department) => department !== 'Tous' && department !== 'Administration'
    )
  );

  protected readonly groupFormOptions = computed(() =>
    Array.from(
      new Set(
        this.users()
          .map((user) => user.group)
          .filter((group) => group && group !== 'Non affecte' && group !== 'Tous les groupes')
      )
    ).sort((first, second) => first.localeCompare(second))
  );

  protected readonly professorsByDepartment = computed<readonly ProfessorDepartmentGroup[]>(() =>
    this.departmentOptions()
      .filter((department) => department !== 'Tous')
      .map((department) => ({
        department,
        professors: this.users().filter(
          (user) => user.role === 'Professeur' && user.department === department
        ),
      }))
      .filter((group) => group.professors.length > 0)
  );

  protected readonly structureForm = this.formBuilder.nonNullable.group({
    entity: ['Departement', [Validators.required]],
    name: ['', [Validators.required, Validators.minLength(3)]],
    code: ['', [Validators.required, Validators.maxLength(12)]],
    owner: ['', [Validators.required, Validators.minLength(3)]],
  });

  protected readonly userForm = this.formBuilder.nonNullable.group({
    fullName: ['', [Validators.required, Validators.minLength(3)]],
    email: ['', [Validators.required, Validators.email]],
    role: ['Etudiant', [Validators.required]],
    status: ['Active', [Validators.required]],
    department: ['', [Validators.required]],
    group: [''],
    specialty: ['', [Validators.required, Validators.minLength(2)]],
  });

  protected readonly examForm = this.formBuilder.nonNullable.group({
    planningMode: ['single', [Validators.required]],
    evaluationType: ['DS', [Validators.required]],
    subject: ['', [Validators.required, Validators.minLength(3)]],
    department: ['', [Validators.required]],
    group: ['', [Validators.required]],
    startDate: ['', [Validators.required]],
    day: ['Lundi', [Validators.required]],
    time: ['09:00', [Validators.required]],
    room: ['', [Validators.required]],
    supervisor: ['', [Validators.required]],
    details: [''],
  });

  protected readonly gradeForm = this.formBuilder.nonNullable.group({
    evaluation: ['', [Validators.required, Validators.minLength(3)]],
    group: ['', [Validators.required]],
    visibility: ['Etudiants concernes', [Validators.required]],
  });

  constructor() {
    this.loadDashboard();
  }

  private loadDashboard(): void {
    this.isDashboardLoading.set(true);

    this.dashboardApi.getDashboard().subscribe({
      next: (dashboard) => {
        this.stats.set(this.normalizeStats(dashboard.stats ?? []));
        this.academicRows.set(this.normalizeAcademicRows(dashboard.academicRows ?? []));
        this.users.set((dashboard.users ?? []).map((user) => this.normalizeUser(user)));
        this.examRows.set((dashboard.exams ?? []).map((exam) => this.normalizeExam(exam)));
        this.isDashboardLoading.set(false);
        this.toastMessage.set('Donnees admin chargees depuis la base de donnees.');
      },
      error: () => {
        this.stats.set([]);
        this.academicRows.set([]);
        this.users.set([]);
        this.examRows.set([]);
        this.isDashboardLoading.set(false);
        this.toastMessage.set(
          'Impossible de charger les donnees administratives. Veuillez vous reconnecter ou reessayer plus tard.'
        );
      },
    });
  }

  protected setSection(section: AdminSection): void {
    this.activeSection.set(section);
    this.toastMessage.set(`${this.sectionLabel(section)} est disponible.`);
  }

  protected logout(): void {
    this.authService.logout();
    this.router.navigate(['/']);
  }

  protected updateSearch(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.searchTerm.set(input.value);
  }

  protected updateDepartmentFilter(event: Event): void {
    this.selectedDepartment.set(this.readSelectValue(event));
    this.selectedGroup.set('Tous');
  }

  protected updateGroupFilter(event: Event): void {
    this.selectedGroup.set(this.readSelectValue(event));
  }

  protected updateRoleFilter(event: Event): void {
    this.selectedRole.set(this.readSelectValue(event));
  }

  protected updatePlanningMode(event: Event): void {
    const mode = this.readSelectValue(event) as PlanningMode;
    this.planningMode.set(mode);
    this.examForm.controls.planningMode.setValue(mode);
  }

  protected submitStructure(): void {
    if (this.structureForm.invalid) {
      this.structureForm.markAllAsTouched();
      return;
    }

    const { entity, name } = this.structureForm.getRawValue();
    this.toastMessage.set(`${entity} "${name}" valide pour enregistrement.`);
  }

  protected submitUser(): void {
    if (this.userForm.invalid) {
      this.userForm.markAllAsTouched();
      return;
    }

    const rawUser = this.userForm.getRawValue();
    const editingEmail = this.editingUserEmail();
    const action = editingEmail ? 'Modification' : 'Creation';

    this.toastMessage.set(
      `${action} de ${rawUser.fullName} validee. Les donnees affichees restent synchronisees.`
    );

    this.cancelUserEdit();
  }

  protected editUser(user: UserRow): void {
    if (user.role === 'Administrateur') {
      this.toastMessage.set('Les comptes administrateurs ne sont pas modifies depuis ce panneau.');
      return;
    }

    this.editingUserEmail.set(user.email);
    this.userForm.setValue({
      fullName: user.name,
      email: user.email,
      role: user.role,
      status: user.status,
      department: user.department,
      group: user.group === 'Tous les groupes' ? '' : user.group,
      specialty: user.specialty,
    });
    this.toastMessage.set(`Modification de ${user.name} ouverte.`);
  }

  protected cancelUserEdit(): void {
    this.editingUserEmail.set(null);
    this.userForm.reset({
      fullName: '',
      email: '',
      role: 'Etudiant',
      status: 'Active',
      department: '',
      group: '',
      specialty: '',
    });
  }

  protected submitExam(): void {
    if (this.examForm.invalid) {
      this.examForm.markAllAsTouched();
      return;
    }

    const { evaluationType, group, planningMode, subject } = this.examForm.getRawValue();
    const isWeekly = planningMode === 'weekly';

    this.toastMessage.set(
      isWeekly
        ? `Semaine de ${evaluationType} pour ${group} validee.`
        : `${evaluationType} ${subject} pour ${group} valide.`
    );
  }

  protected publishGrades(): void {
    if (this.gradeForm.invalid) {
      this.gradeForm.markAllAsTouched();
      return;
    }

    const { evaluation } = this.gradeForm.getRawValue();
    this.toastMessage.set(`Publication des notes "${evaluation}" validee.`);
  }

  protected activateQuickAction(action: string): void {
    const target: AdminSection = action.includes('utilisateur')
      ? 'users'
      : action.includes('examen') || action.includes('notes')
        ? 'calendar'
        : 'structure';

    this.setSection(target);
  }

  private normalizeStats(stats: readonly Partial<StatCard>[]): StatCard[] {
    const tones: StatCard['tone'][] = ['light', 'steel', 'warm', 'sand'];

    return stats
      .filter((stat) => Boolean(stat.label))
      .map((stat, index) => ({
        label: this.cleanText(stat.label, 'Indicateur'),
        value: this.cleanText(stat.value, '0'),
        trend: this.cleanText(stat.trend, 'Base de donnees'),
        tone: this.isStatTone(stat.tone) ? stat.tone : tones[index % tones.length],
      }));
  }

  private normalizeAcademicRows(rows: readonly Partial<AcademicRow>[]): AcademicRow[] {
    return rows
      .filter((row) => Boolean(row.title || row.code))
      .map((row) => ({
        code: this.cleanText(row.code, this.buildCodeFromName(row.title)),
        title: this.cleanText(row.title, 'Departement non renseigne'),
        meta: this.cleanText(row.meta, 'Details non renseignes'),
        status: this.cleanText(row.status, 'Statut non renseigne'),
      }));
  }

  private normalizeUser(user: Partial<UserRow>): UserRow {
    const role = this.cleanText(user.role, 'Utilisateur');

    return {
      name: this.cleanText(user.name, 'Utilisateur sans nom'),
      email: this.cleanText(user.email, 'Email non renseigne'),
      role,
      status: user.status === 'Active' || user.status === 'Pending' ? user.status : 'Pending',
      department: this.cleanText(user.department, 'Non affecte'),
      group: this.cleanText(user.group, role === 'Professeur' ? 'Tous les groupes' : 'Non affecte'),
      specialty: this.cleanText(user.specialty, 'Non renseigne'),
    };
  }

  private normalizeExam(exam: Partial<ExamRow>): ExamRow {
    return {
      subject: this.cleanText(exam.subject, 'Evaluation non renseignee'),
      group: this.cleanText(exam.group, 'Groupe non renseigne'),
      date: this.cleanText(exam.date, 'Date non renseignee'),
      room: this.cleanText(exam.room, 'Salle non renseignee'),
      type: this.cleanText(exam.type, 'Type non renseigne'),
      scope: this.cleanText(exam.scope, 'Departement non renseigne'),
    };
  }

  private parseStatValue(value: string): number {
    const numericValue = Number(value.replace(/[^\d.-]/g, ''));
    return Number.isFinite(numericValue) ? numericValue : 0;
  }

  private cleanText(value: string | null | undefined, fallback: string): string {
    const normalizedValue = value?.trim();
    return normalizedValue ? normalizedValue : fallback;
  }

  private isStatTone(value: unknown): value is StatCard['tone'] {
    return value === 'light' || value === 'warm' || value === 'steel' || value === 'sand';
  }

  private buildCodeFromName(name: string | null | undefined): string {
    const code = name
      ?.split(/\s+/)
      .filter(Boolean)
      .map((word) => word.charAt(0).toUpperCase())
      .join('')
      .slice(0, 3);

    return code || 'DEP';
  }

  private readSelectValue(event: Event): string {
    return (event.target as HTMLSelectElement).value;
  }

  protected hasControlError(
    form: { get(path: string): AbstractControl | null },
    controlName: string,
    errorCode?: string
  ): boolean {
    const control = form.get(controlName);

    if (!control || !control.touched) {
      return false;
    }

    return errorCode ? control.hasError(errorCode) : control.invalid;
  }

  private sectionLabel(section: AdminSection): string {
    return this.navItems.find((item) => item.id === section)?.label ?? 'Module admin';
  }
}
