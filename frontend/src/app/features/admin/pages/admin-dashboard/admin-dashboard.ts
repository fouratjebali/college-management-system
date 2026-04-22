import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../../core/services/auth';
import {
  AdminAcademicYear,
  AdminAttendanceDetail,
  AdminAttendanceSession,
  AdminDashboardApi,
  AdminEliminationRecord,
  AdminExamPlanningOptions,
  AdminExamPlanningRequest,
  AdminNoteValidationDetail,
  AdminNoteValidationEvaluation,
  AdminPlannedExam,
  AdminSemester,
} from '../../services/admin-dashboard-api';

type AdminSection = 'overview' | 'academic-year' | 'structure' | 'users' | 'attendance' | 'calendar';

interface NavItem {
  id: AdminSection;
  label: string;
  description: string;
}

interface OverviewMetric {
  label: string;
  value: string;
  meta: string;
}

interface ChartSegment {
  label: string;
  value: number;
  percent: number;
  color: string;
}

interface DepartmentKpi {
  name: string;
  students: number;
  professors: number;
  groups: number;
  subjects: number;
  load: number;
}

interface SystemKpi {
  label: string;
  value: string;
  meta: string;
  progress: number;
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

interface ExamDraftRow extends AdminExamPlanningRequest {
  id: number;
}

interface ExamDayGroup {
  key: string;
  label: string;
  exams: readonly AdminPlannedExam[];
}

interface ExamWeekGroup {
  weekStart: string;
  label: string;
  status: string;
  days: readonly ExamDayGroup[];
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
  protected readonly isDashboardLoading = signal(true);
  protected readonly isAcademicYearsLoading = signal(true);
  protected readonly isExamPlanningLoading = signal(true);
  protected readonly isNoteValidationLoading = signal(true);
  protected readonly isAttendanceLoading = signal(true);

  protected readonly navItems: readonly NavItem[] = [
    {
      id: 'overview',
      label: 'Dashboard',
      description: 'Statistiques et activite recente',
    },
    {
      id: 'academic-year',
      label: 'Annees',
      description: 'Annees, semestres et verrouillage',
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
      id: 'attendance',
      label: 'Presences',
      description: 'Suivi et cloture des appels',
    },
    {
      id: 'calendar',
      label: 'Examens & Notes',
      description: 'Calendrier et publication',
    },
  ];

  protected readonly overviewMetrics = computed<readonly OverviewMetric[]>(() => {
    const students = this.countUsersByRole('Etudiant');
    const professors = this.countUsersByRole('Professeur');
    const activeUsers = this.users().filter((user) => user.status === 'Active').length;
    const totalUsers = this.users().length;
    const departments = this.academicRows().length;
    const groups = this.totalGroups();
    const subjects = this.totalSubjects();
    const attendanceSessions = this.todayAttendanceSessions().length;
    const openNoteFlows = this.noteValidationEvaluations().filter(
      (evaluation) => evaluation.status !== 'Publiee'
    ).length;

    return [
      {
        label: 'Etudiants',
        value: this.formatNumber(students),
        meta: `${this.formatNumber(groups)} groupe(s), ${this.formatPercent(this.percent(students, totalUsers))} des comptes`,
      },
      {
        label: 'Professeurs',
        value: this.formatNumber(professors),
        meta: `${this.formatNumber(subjects)} matiere(s) encadree(s)`,
      },
      {
        label: 'Departements',
        value: this.formatNumber(departments),
        meta: `${this.formatNumber(groups)} groupes et ${this.formatNumber(subjects)} matieres`,
      },
      {
        label: 'Systeme actif',
        value: `${this.formatPercent(this.percent(activeUsers, totalUsers))}`,
        meta: `${this.formatNumber(attendanceSessions)} seance(s) aujourd'hui, ${openNoteFlows} workflow(s) note`,
      },
    ];
  });

  protected readonly roleDistribution = computed<readonly ChartSegment[]>(() => {
    const roles = [
      { label: 'Etudiants', role: 'Etudiant', color: '#dfd0b8' },
      { label: 'Professeurs', role: 'Professeur', color: '#948979' },
      { label: 'Admins', role: 'Administrateur', color: '#6f7f8f' },
    ];
    const total = this.users().length;

    return roles.map((item) => {
      const value = this.countUsersByRole(item.role);
      return {
        label: item.label,
        value,
        percent: this.percent(value, total),
        color: item.color,
      };
    });
  });

  protected readonly roleDonutStyle = computed(() => {
    let offset = 0;
    const segments = this.roleDistribution()
      .filter((segment) => segment.percent > 0)
      .map((segment) => {
        const start = offset;
        offset += segment.percent;
        return `${segment.color} ${start}% ${offset}%`;
      });

    return `conic-gradient(${segments.length ? segments.join(', ') : 'rgba(223, 208, 184, 0.14) 0 100%'})`;
  });

  protected readonly departmentKpis = computed<readonly DepartmentKpi[]>(() => {
    const departments = this.academicRows().map((row) => {
      const students = this.users().filter(
        (user) => user.role === 'Etudiant' && user.department === row.title
      ).length;
      const professors = this.users().filter(
        (user) => user.role === 'Professeur' && user.department === row.title
      ).length;
      const structure = this.parseAcademicMeta(row.meta);

      return {
        name: row.title,
        students,
        professors,
        groups: structure.groups,
        subjects: structure.subjects,
        load: students + professors + structure.groups + structure.subjects,
      };
    });
    const maxLoad = Math.max(...departments.map((department) => department.load), 0);

    return departments
      .sort((first, second) => second.load - first.load)
      .map((department) => ({
        ...department,
        load: Math.max(8, this.percent(department.load, maxLoad)),
      }));
  });

  protected readonly systemKpis = computed<readonly SystemKpi[]>(() => {
    const plannedExams = this.plannedExams().length;
    const publishedExams = this.plannedExams().filter((exam) => this.isPublishedExam(exam)).length;
    const noteFlows = this.noteValidationEvaluations().length;
    const publishedNotes = this.noteValidationEvaluations().reduce(
      (total, evaluation) => total + evaluation.publishedCount,
      0
    );
    const totalNotes = this.noteValidationEvaluations().reduce(
      (total, evaluation) => total + evaluation.totalNotes,
      0
    );
    const attendanceSessions = this.todayAttendanceSessions();
    const expected = attendanceSessions.reduce((total, session) => total + session.expectedCount, 0);
    const recorded = attendanceSessions.reduce((total, session) => total + session.recordedCount, 0);
    const eliminations = this.eliminations().length;
    const notified = this.eliminations().filter((record) => record.status === 'Renseigne').length;

    return [
      {
        label: 'Examens publies',
        value: `${publishedExams}/${plannedExams}`,
        meta: 'Calendrier examens',
        progress: this.percent(publishedExams, plannedExams),
      },
      {
        label: 'Notes publiees',
        value: `${publishedNotes}/${totalNotes}`,
        meta: `${noteFlows} evaluation(s) suivie(s)`,
        progress: this.percent(publishedNotes, totalNotes),
      },
      {
        label: 'Presences saisies',
        value: `${recorded}/${expected}`,
        meta: `Seances du ${this.todayAttendanceDay()}`,
        progress: this.percent(recorded, expected),
      },
      {
        label: 'Eliminations renseignees',
        value: `${notified}/${eliminations}`,
        meta: 'Suivi absence critique',
        progress: this.percent(notified, eliminations),
      },
    ];
  });

  protected readonly quickActions = [
    'Gerer les semestres',
    'Creer un departement',
    'Ajouter un utilisateur',
    'Superviser les presences',
    'Planifier un examen',
    'Publier les notes',
  ] as const;

  protected readonly academicRows = signal<readonly AcademicRow[]>([]);

  protected readonly users = signal<readonly UserRow[]>([]);

  protected readonly examRows = signal<readonly ExamRow[]>([]);
  protected readonly academicYears = signal<readonly AdminAcademicYear[]>([]);
  protected readonly examPlanningOptions = signal<AdminExamPlanningOptions | null>(null);
  protected readonly plannedExams = signal<readonly AdminPlannedExam[]>([]);
  protected readonly examDrafts = signal<readonly ExamDraftRow[]>([]);
  protected readonly noteValidationEvaluations = signal<readonly AdminNoteValidationEvaluation[]>([]);
  protected readonly selectedNoteValidationDetail = signal<AdminNoteValidationDetail | null>(null);
  protected readonly attendanceSessions = signal<readonly AdminAttendanceSession[]>([]);
  protected readonly selectedAttendanceDetail = signal<AdminAttendanceDetail | null>(null);
  protected readonly eliminations = signal<readonly AdminEliminationRecord[]>([]);
  private examDraftCounter = 0;

  protected readonly activeAcademicYear = computed<AdminAcademicYear | null>(() =>
    this.academicYears().find((academicYear) => academicYear.active) ?? this.academicYears()[0] ?? null
  );

  protected readonly activeSemester = computed<AdminSemester | null>(() =>
    this.academicYears()
      .flatMap((academicYear) => academicYear.semesters)
      .find((semester) => semester.active) ?? null
  );

  protected readonly semesterCount = computed(() =>
    this.academicYears().reduce((total, academicYear) => total + academicYear.semesters.length, 0)
  );

  protected readonly plannedExamWeeks = computed<readonly ExamWeekGroup[]>(() => {
    const exams = [...this.plannedExams()].sort((first, second) =>
      `${first.isoDate} ${first.startTime}`.localeCompare(`${second.isoDate} ${second.startTime}`)
    );
    const weeks = new Map<string, AdminPlannedExam[]>();

    exams.forEach((exam) => {
      const weekStart = exam.weekStart || exam.isoDate;
      weeks.set(weekStart, [...(weeks.get(weekStart) ?? []), exam]);
    });

    return Array.from(weeks.entries()).map(([weekStart, weekExams]) => {
      const days = new Map<string, AdminPlannedExam[]>();
      weekExams.forEach((exam) => {
        const dayKey = `${exam.day} ${exam.date}`;
        days.set(dayKey, [...(days.get(dayKey) ?? []), exam]);
      });

      return {
        weekStart,
        label: `Semaine du ${this.formatIsoDate(weekStart)}`,
        status: weekExams.every((exam) => this.isPublishedExam(exam)) ? 'Publiee' : 'Brouillon',
        days: Array.from(days.entries()).map(([key, dayExams]) => ({
          key,
          label: key,
          exams: dayExams,
        })),
      };
    });
  });

  protected readonly selectedNoteEvaluation = computed(() =>
    this.selectedNoteValidationDetail()?.evaluation ?? null
  );

  protected readonly todayAttendanceDay = computed(() => this.currentFrenchDay());

  protected readonly attendanceStats = computed(() => {
    const sessions = this.todayAttendanceSessions();
    const expected = sessions.reduce((total, session) => total + session.expectedCount, 0);
    const recorded = sessions.reduce((total, session) => total + session.recordedCount, 0);
    const absent = sessions.reduce((total, session) => total + session.absentCount, 0);
    const closed = sessions.filter((session) => this.isClosedAttendanceSession(session)).length;

    return [
      { label: 'Seances', value: sessions.length.toString(), meta: 'Sessions suivies' },
      { label: 'Saisies', value: `${recorded}/${expected}`, meta: 'Presences renseignees' },
      { label: 'Absences', value: absent.toString(), meta: 'Absences declarees' },
      { label: 'Cloturees', value: closed.toString(), meta: 'Appels verrouilles' },
    ];
  });

  protected readonly filteredAttendanceSessions = computed(() => {
    const query = this.searchTerm().trim().toLowerCase();

    return this.todayAttendanceSessions().filter((session) =>
      this.matchesText(query, [
        session.subject,
        session.group,
        session.department,
        session.professor,
        session.day,
        session.room,
        session.type,
        session.status,
      ])
    );
  });

  protected readonly filteredEliminations = computed(() => {
    const query = this.searchTerm().trim().toLowerCase();

    return this.eliminations().filter((record) =>
      this.matchesText(query, [
        record.studentName,
        record.matricule,
        record.group,
        record.subject,
        record.typeSeance,
        record.status,
      ])
    );
  });

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
    evaluationType: ['DS', [Validators.required]],
    subjectId: [0, [Validators.required, Validators.min(1)]],
    groupId: [0, [Validators.required, Validators.min(1)]],
    professorId: [0, [Validators.required, Validators.min(1)]],
    startDate: ['', [Validators.required]],
    startTime: ['09:00', [Validators.required]],
    endTime: ['11:00', [Validators.required]],
    building: ['Bloc B', [Validators.required]],
    room: ['', [Validators.required]],
    details: [''],
  });

  protected readonly noteValidationForm = this.formBuilder.nonNullable.group({
    remark: [''],
  });

  protected readonly academicYearForm = this.formBuilder.nonNullable.group({
    label: ['2026-2027', [Validators.required, Validators.minLength(9)]],
    startDate: ['2026-09-01', [Validators.required]],
    endDate: ['2027-06-30', [Validators.required]],
    active: [false],
    locked: [false],
  });

  protected readonly semesterForm = this.formBuilder.nonNullable.group({
    academicYearId: [0, [Validators.required, Validators.min(1)]],
    code: ['S1', [Validators.required, Validators.maxLength(8)]],
    name: ['Semestre 1', [Validators.required, Validators.minLength(3)]],
    startDate: ['2026-09-01', [Validators.required]],
    endDate: ['2027-01-31', [Validators.required]],
    active: [false],
    locked: [false],
  });

  constructor() {
    this.loadDashboard();
    this.loadAcademicYears();
    this.loadExamPlanning();
    this.loadNoteValidation();
    this.loadAttendanceSupervision();
  }

  private loadDashboard(): void {
    this.isDashboardLoading.set(true);

    this.dashboardApi.getDashboard().subscribe({
      next: (dashboard) => {
        this.academicRows.set(this.normalizeAcademicRows(dashboard.academicRows ?? []));
        this.users.set((dashboard.users ?? []).map((user) => this.normalizeUser(user)));
        this.examRows.set((dashboard.exams ?? []).map((exam) => this.normalizeExam(exam)));
        this.isDashboardLoading.set(false);
        this.toastMessage.set('Donnees admin chargees depuis la base de donnees.');
      },
      error: () => {
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

    this.dashboardApi
      .createPlannedExam(this.buildExamPlanningRequest())
      .subscribe({
        next: (exam) => {
          this.toastMessage.set(`${exam.type} ${exam.subject} ajoute en brouillon pour ${exam.group}.`);
          this.loadExamPlanning();
          this.loadDashboard();
        },
        error: (error) => {
          this.toastMessage.set(
            error.error?.message ||
              error.error?.error ||
              'Impossible de planifier cet examen. Verifiez les conflits de salle, groupe ou professeur.'
          );
        },
      });
  }

  protected addExamDraft(): void {
    if (this.examForm.invalid) {
      this.examForm.markAllAsTouched();
      return;
    }

    const draft = {
      ...this.buildExamPlanningRequest(),
      id: ++this.examDraftCounter,
    };

    this.examDrafts.update((drafts) => [...drafts, draft]);
    this.toastMessage.set(`${this.optionLabel('subject', draft.subjectId)} ajoute au lot hebdomadaire.`);
  }

  protected removeExamDraft(draftId: number): void {
    this.examDrafts.update((drafts) => drafts.filter((draft) => draft.id !== draftId));
  }

  protected submitExamDrafts(): void {
    const drafts = this.examDrafts();

    if (drafts.length === 0) {
      this.toastMessage.set('Ajoutez au moins un examen au lot avant de planifier la semaine.');
      return;
    }

    this.dashboardApi
      .createPlannedExams({ exams: drafts.map(({ id, ...request }) => request) })
      .subscribe({
        next: (exams) => {
          this.examDrafts.set([]);
          this.toastMessage.set(`${exams.length} examen(s) planifie(s) en brouillon.`);
          this.loadExamPlanning();
          this.loadDashboard();
        },
        error: (error) => {
          this.toastMessage.set(
            error.error?.message ||
              error.error?.error ||
              'Impossible de planifier ce lot. Verifiez les conflits de salle, groupe ou professeur.'
          );
        },
      });
  }

  protected publishPlannedExam(exam: AdminPlannedExam): void {
    this.dashboardApi.publishPlannedExam(exam.evaluationId).subscribe({
      next: (publishedExam) => {
        this.toastMessage.set(`${publishedExam.type} ${publishedExam.subject} publie pour ${publishedExam.group}.`);
        this.loadExamPlanning();
      },
      error: () => {
        this.toastMessage.set("Impossible de publier cet examen.");
      },
    });
  }

  protected publishExamWeek(weekStart: string): void {
    this.dashboardApi.publishExamWeek(weekStart).subscribe({
      next: (exams) => {
        this.toastMessage.set(`${exams.length} examen(s) publie(s) pour la semaine.`);
        this.loadExamPlanning();
      },
      error: () => {
        this.toastMessage.set("Impossible de publier cette semaine d'examens.");
      },
    });
  }

  protected isPublishedExam(exam: AdminPlannedExam): boolean {
    return exam.status.toLowerCase() === 'publie';
  }

  protected optionLabel(kind: 'subject' | 'group' | 'professor', id: number): string {
    const options = this.examPlanningOptions();
    const source =
      kind === 'subject'
        ? options?.subjects
        : kind === 'group'
          ? options?.groups
          : options?.professors;

    return source?.find((option) => option.id === id)?.label ?? 'Non renseigne';
  }

  protected selectNoteEvaluation(evaluationId: number): void {
    this.dashboardApi.getNoteValidationDetail(evaluationId).subscribe({
      next: (detail) => {
        this.selectedNoteValidationDetail.set(detail);
        this.noteValidationForm.reset({ remark: '' });
      },
      error: () => {
        this.selectedNoteValidationDetail.set(null);
        this.toastMessage.set('Impossible de charger les notes de cette evaluation.');
      },
    });
  }

  protected validateSelectedNotes(): void {
    this.applyNoteDecision('validate');
  }

  protected rejectSelectedNotes(): void {
    this.applyNoteDecision('reject');
  }

  protected publishSelectedNotes(): void {
    this.applyNoteDecision('publish');
  }

  protected selectAttendanceSession(sessionId: number): void {
    this.dashboardApi.getAttendanceDetail(sessionId).subscribe({
      next: (detail) => {
        this.selectedAttendanceDetail.set(detail);
        this.toastMessage.set(`Appel ${detail.session.subject} - ${detail.session.group} charge.`);
      },
      error: () => {
        this.selectedAttendanceDetail.set(null);
        this.toastMessage.set("Impossible de charger le detail de cette seance.");
      },
    });
  }

  protected closeAttendanceSession(session: AdminAttendanceSession): void {
    this.dashboardApi.closeAttendanceSession(session.sessionId).subscribe({
      next: (detail) => {
        this.selectedAttendanceDetail.set(detail);
        this.loadAttendanceSupervision();
        this.toastMessage.set(`Appel ${detail.session.subject} cloture.`);
      },
      error: () => {
        this.toastMessage.set("Impossible de cloturer cette seance de presence.");
      },
    });
  }

  protected reopenAttendanceSession(session: AdminAttendanceSession): void {
    this.dashboardApi.reopenAttendanceSession(session.sessionId).subscribe({
      next: (detail) => {
        this.selectedAttendanceDetail.set(detail);
        this.loadAttendanceSupervision();
        this.toastMessage.set(`Appel ${detail.session.subject} rouvert.`);
      },
      error: () => {
        this.toastMessage.set("Impossible de rouvrir cette seance de presence.");
      },
    });
  }

  protected markCollectiveAbsence(session: AdminAttendanceSession): void {
    this.dashboardApi.markCollectiveAbsence(session.sessionId).subscribe({
      next: (detail) => {
        this.selectedAttendanceDetail.set(detail);
        this.loadAttendanceSupervision();
        this.loadEliminations();
        this.toastMessage.set(`Absence collective validee pour ${detail.session.group}.`);
      },
      error: () => {
        this.toastMessage.set("Impossible de valider l'absence collective.");
      },
    });
  }

  protected isClosedAttendanceSession(session: AdminAttendanceSession): boolean {
    return session.status.toLowerCase() === 'cloturee';
  }

  protected hasCollectiveAbsenceSignal(session: AdminAttendanceSession): boolean {
    return session.collectiveAbsenceStatus === 'Signalee';
  }

  protected notifyEliminatedStudent(record: AdminEliminationRecord): void {
    this.dashboardApi.notifyEliminatedStudent(record.id).subscribe({
      next: (updatedRecord) => {
        this.eliminations.update((records) =>
          records.map((item) => (item.id === updatedRecord.id ? updatedRecord : item))
        );
        this.toastMessage.set(`${updatedRecord.studentName} est renseigne sur son elimination.`);
      },
      error: () => {
        this.toastMessage.set("Impossible de renseigner cet etudiant.");
      },
    });
  }

  protected submitAcademicYear(): void {
    if (this.academicYearForm.invalid) {
      this.academicYearForm.markAllAsTouched();
      return;
    }

    this.dashboardApi.createAcademicYear(this.academicYearForm.getRawValue()).subscribe({
      next: () => {
        this.toastMessage.set('Annee universitaire creee avec succes.');
        this.loadAcademicYears();
      },
      error: () => {
        this.toastMessage.set("Impossible de creer l'annee universitaire.");
      },
    });
  }

  protected submitSemester(): void {
    if (this.semesterForm.invalid) {
      this.semesterForm.markAllAsTouched();
      return;
    }

    const { academicYearId, code, name, startDate, endDate, active, locked } =
      this.semesterForm.getRawValue();

    this.dashboardApi
      .createSemester(academicYearId, {
        code,
        name,
        startDate,
        endDate,
        active,
        locked,
      })
      .subscribe({
        next: () => {
          this.toastMessage.set('Semestre cree avec succes.');
          this.loadAcademicYears();
        },
        error: () => {
          this.toastMessage.set('Impossible de creer le semestre pour cette annee.');
        },
      });
  }

  protected activateAcademicYear(academicYear: AdminAcademicYear): void {
    this.dashboardApi.activateAcademicYear(academicYear.id).subscribe({
      next: () => {
        this.toastMessage.set(`${academicYear.label} est maintenant l'annee active.`);
        this.loadAcademicYears();
      },
      error: () => {
        this.toastMessage.set("Impossible d'activer cette annee universitaire.");
      },
    });
  }

  protected activateSemester(semester: AdminSemester): void {
    this.dashboardApi.activateSemester(semester.id).subscribe({
      next: () => {
        this.toastMessage.set(`${semester.name} est maintenant le semestre actif.`);
        this.loadAcademicYears();
      },
      error: () => {
        this.toastMessage.set("Impossible d'activer ce semestre.");
      },
    });
  }

  protected toggleSemesterLock(semester: AdminSemester): void {
    const locked = !semester.locked;

    this.dashboardApi.setSemesterLocked(semester.id, locked).subscribe({
      next: () => {
        this.toastMessage.set(`${semester.name} ${locked ? 'verrouille' : 'deverrouille'}.`);
        this.loadAcademicYears();
      },
      error: () => {
        this.toastMessage.set('Impossible de modifier le verrouillage du semestre.');
      },
    });
  }

  protected activateQuickAction(action: string): void {
    const target: AdminSection = action.includes('utilisateur')
      ? 'users'
      : action.includes('semestre') || action.includes('annee')
        ? 'academic-year'
      : action.includes('presence')
        ? 'attendance'
      : action.includes('examen') || action.includes('notes')
        ? 'calendar'
        : 'structure';

    this.setSection(target);
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

  private cleanText(value: string | null | undefined, fallback: string): string {
    const normalizedValue = value?.trim();
    return normalizedValue ? normalizedValue : fallback;
  }

  private countUsersByRole(role: string): number {
    return this.users().filter((user) => user.role === role).length;
  }

  private totalGroups(): number {
    return this.academicRows().reduce((total, row) => total + this.parseAcademicMeta(row.meta).groups, 0);
  }

  private totalSubjects(): number {
    return this.academicRows().reduce((total, row) => total + this.parseAcademicMeta(row.meta).subjects, 0);
  }

  private parseAcademicMeta(meta: string): { groups: number; subjects: number } {
    const groups = Number(meta.match(/(\d+)\s+groupes?/i)?.[1] ?? 0);
    const subjects = Number(meta.match(/(\d+)\s+matieres?/i)?.[1] ?? 0);

    return {
      groups: Number.isFinite(groups) ? groups : 0,
      subjects: Number.isFinite(subjects) ? subjects : 0,
    };
  }

  private percent(value: number, total: number): number {
    if (!Number.isFinite(value) || !Number.isFinite(total) || total <= 0) {
      return 0;
    }

    return Math.min(100, Math.round((value / total) * 100));
  }

  private formatNumber(value: number): string {
    return new Intl.NumberFormat('fr-FR').format(value);
  }

  protected formatPercent(value: number): string {
    return `${value}%`;
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

  private loadAcademicYears(): void {
    this.isAcademicYearsLoading.set(true);

    this.dashboardApi.getAcademicYears().subscribe({
      next: (academicYears) => {
        this.academicYears.set(academicYears ?? []);
        this.isAcademicYearsLoading.set(false);

        const currentYear = this.activeAcademicYear();
        if (currentYear && this.semesterForm.controls.academicYearId.value === 0) {
          this.semesterForm.controls.academicYearId.setValue(currentYear.id);
        }
      },
      error: () => {
        this.academicYears.set([]);
        this.isAcademicYearsLoading.set(false);
        this.toastMessage.set('Impossible de charger les annees universitaires.');
      },
    });
  }

  private loadExamPlanning(): void {
    this.isExamPlanningLoading.set(true);

    this.dashboardApi.getExamPlanningOptions().subscribe({
      next: (options) => {
        this.examPlanningOptions.set(options);
        this.prefillExamPlanningForm(options);
      },
      error: () => {
        this.examPlanningOptions.set(null);
        this.toastMessage.set('Impossible de charger les options de planification des examens.');
      },
    });

    this.dashboardApi.getPlannedExams().subscribe({
      next: (exams) => {
        this.plannedExams.set(exams ?? []);
        this.isExamPlanningLoading.set(false);
      },
      error: () => {
        this.plannedExams.set([]);
        this.isExamPlanningLoading.set(false);
        this.toastMessage.set('Impossible de charger le calendrier des examens.');
      },
    });
  }

  private loadNoteValidation(): void {
    this.isNoteValidationLoading.set(true);

    this.dashboardApi.getNoteValidationEvaluations().subscribe({
      next: (evaluations) => {
        this.noteValidationEvaluations.set(evaluations ?? []);
        this.isNoteValidationLoading.set(false);

        const selectedId = this.selectedNoteEvaluation()?.evaluationId;
        const nextSelection = selectedId
          ? evaluations.find((evaluation) => evaluation.evaluationId === selectedId)
          : evaluations[0];

        if (nextSelection) {
          this.selectNoteEvaluation(nextSelection.evaluationId);
        } else {
          this.selectedNoteValidationDetail.set(null);
        }
      },
      error: () => {
        this.noteValidationEvaluations.set([]);
        this.selectedNoteValidationDetail.set(null);
        this.isNoteValidationLoading.set(false);
        this.toastMessage.set('Impossible de charger le workflow de validation des notes.');
      },
    });
  }

  private loadAttendanceSupervision(): void {
    this.isAttendanceLoading.set(true);

    this.dashboardApi.getAttendanceSessions().subscribe({
      next: (sessions) => {
        this.attendanceSessions.set(sessions ?? []);
        this.isAttendanceLoading.set(false);
        this.loadEliminations();

        const selectedId = this.selectedAttendanceDetail()?.session.sessionId;
        const todaySessions = this.todayAttendanceSessions();
        const nextSelection = selectedId
          ? todaySessions.find((session) => session.sessionId === selectedId)
          : todaySessions[0];

        if (nextSelection) {
          this.selectAttendanceSession(nextSelection.sessionId);
        } else {
          this.selectedAttendanceDetail.set(null);
        }
      },
      error: () => {
        this.attendanceSessions.set([]);
        this.selectedAttendanceDetail.set(null);
        this.isAttendanceLoading.set(false);
        this.toastMessage.set('Impossible de charger la supervision des presences.');
      },
    });
  }

  private loadEliminations(): void {
    this.dashboardApi.getEliminations().subscribe({
      next: (records) => {
        this.eliminations.set(records ?? []);
      },
      error: () => {
        this.eliminations.set([]);
        this.toastMessage.set("Impossible de charger la liste des etudiants elimines.");
      },
    });
  }

  private applyNoteDecision(action: 'validate' | 'reject' | 'publish'): void {
    const evaluation = this.selectedNoteEvaluation();

    if (!evaluation) {
      this.toastMessage.set('Selectionnez une evaluation avant de continuer.');
      return;
    }

    const remark = this.noteValidationForm.controls.remark.value;
    const request =
      action === 'validate'
        ? this.dashboardApi.validateNotes(evaluation.evaluationId, remark)
        : action === 'reject'
          ? this.dashboardApi.rejectNotes(evaluation.evaluationId, remark)
          : this.dashboardApi.publishNotes(evaluation.evaluationId, remark);

    request.subscribe({
      next: (detail) => {
        this.selectedNoteValidationDetail.set(detail);
        this.noteValidationForm.reset({ remark: '' });
        this.loadNoteValidation();
        this.toastMessage.set(this.noteDecisionMessage(action, detail.evaluation.label));
      },
      error: (error) => {
        this.toastMessage.set(
          error.error?.message ||
            error.error?.error ||
            'Impossible de traiter les notes selectionnees.'
        );
      },
    });
  }

  private noteDecisionMessage(action: 'validate' | 'reject' | 'publish', label: string): string {
    if (action === 'validate') {
      return `Notes "${label}" validees.`;
    }
    if (action === 'reject') {
      return `Notes "${label}" retournees au professeur.`;
    }
    return `Notes "${label}" publiees aux etudiants.`;
  }

  private prefillExamPlanningForm(options: AdminExamPlanningOptions): void {
    if (this.examForm.controls.subjectId.value === 0 && options.subjects[0]) {
      this.examForm.controls.subjectId.setValue(options.subjects[0].id);
    }

    if (this.examForm.controls.groupId.value === 0 && options.groups[0]) {
      this.examForm.controls.groupId.setValue(options.groups[0].id);
    }

    if (this.examForm.controls.professorId.value === 0 && options.professors[0]) {
      this.examForm.controls.professorId.setValue(options.professors[0].id);
    }
  }

  private buildExamPlanningRequest(): AdminExamPlanningRequest {
    const { evaluationType, subjectId, groupId, professorId, startDate, startTime, endTime, building, room, details } =
      this.examForm.getRawValue();

    return {
      evaluationType,
      subjectId,
      groupId,
      professorId,
      examDate: startDate,
      startTime,
      endTime,
      building,
      room,
      details,
    };
  }

  private formatIsoDate(value: string): string {
    const [year, month, day] = value.split('-');
    return year && month && day ? `${day}/${month}/${year}` : value;
  }

  private matchesText(query: string, values: readonly string[]): boolean {
    return !query || values.some((value) => value.toLowerCase().includes(query));
  }

  private todayAttendanceSessions(): readonly AdminAttendanceSession[] {
    const day = this.currentFrenchDay();

    return this.attendanceSessions().filter((session) => session.day.toLowerCase() === day.toLowerCase());
  }

  private currentFrenchDay(): string {
    return ['Dimanche', 'Lundi', 'Mardi', 'Mercredi', 'Jeudi', 'Vendredi', 'Samedi'][new Date().getDay()];
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
