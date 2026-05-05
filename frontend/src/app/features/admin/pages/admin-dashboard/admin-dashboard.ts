import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../../core/services/auth';
import { ShellPreferencesService } from '../../../../core/services/shell-preferences';
import { ThemeService } from '../../../../core/services/theme';
import {
  NotificationCenterComponent,
  NotificationCenterItem,
} from '../../../../shared/components/notification-center/notification-center';
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
  AdminPlanningOption,
  AdminPlannedExam,
  AdminSemester,
  AdminUserPageResponse,
} from '../../services/admin-dashboard-api';

type AdminSection =
  | 'overview'
  | 'academic-year'
  | 'structure'
  | 'users'
  | 'attendance'
  | 'exams'
  | 'notes';

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
  dayLabel?: string;
  roomSource?: string;
  guardLabel?: string;
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

interface NoteWorkflowStage {
  label: string;
  value: string;
  meta: string;
  tone: 'submitted' | 'validated' | 'rejected' | 'published';
}

interface TpExamRequest {
  id: number;
  subjectId: number;
  groupId: number;
  professorId: number;
  subject: string;
  group: string;
  professor: string;
  requestedDate: string;
  preferredRoom: string;
  reason: string;
  status: 'En attente' | 'Planifiee';
}

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, NotificationCenterComponent],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminDashboardComponent {
  private readonly formBuilder = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly dashboardApi = inject(AdminDashboardApi);
  private readonly themeService = inject(ThemeService);
  private readonly shellPreferences = inject(ShellPreferencesService);

  protected readonly activeSection = signal<AdminSection>('overview');
  protected readonly themeLabel = this.themeService.label;
  protected readonly sidebarCollapsed = this.shellPreferences.sidebarCollapsed;
  protected readonly searchTerm = signal('');
  protected readonly toastMessage = signal('');
  protected readonly selectedDepartment = signal('Tous');
  protected readonly selectedGroup = signal('Tous');
  protected readonly selectedRole = signal('Tous');
  protected readonly editingUserEmail = signal<string | null>(null);
  protected readonly isDashboardLoading = signal(true);
  protected readonly isUsersLoading = signal(true);
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
      id: 'exams',
      label: 'Examens',
      description: 'Planning et publication',
    },
    {
      id: 'notes',
      label: 'Notes',
      description: 'Validation et publication',
    },
  ];

  protected readonly overviewMetrics = computed<readonly OverviewMetric[]>(() => {
    const students = this.countUsersByRole('Etudiant');
    const professors = this.countUsersByRole('Professeur');
    const activeUsers = this.activeUserElements();
    const totalUsers = this.totalUserElements();
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
    const total = this.totalUserElements();

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
      const userSummary = this.departmentUserSummaries().find(
        (summary) => summary.department === row.title
      );
      const structure = this.parseAcademicMeta(row.meta);

      return {
        name: row.title,
        students: userSummary?.students ?? 0,
        professors: userSummary?.professors ?? 0,
        groups: structure.groups,
        subjects: structure.subjects,
        load: (userSummary?.students ?? 0) + (userSummary?.professors ?? 0) + structure.groups + structure.subjects,
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

  protected readonly notificationItems = computed<readonly NotificationCenterItem[]>(() => {
    const noteFlows = this.noteValidationEvaluations()
      .filter((evaluation) => evaluation.status !== 'Publiee')
      .map((evaluation) => ({
        id: `admin-note-${evaluation.evaluationId}-${evaluation.status}`,
        title: `Notes a traiter - ${evaluation.subject}`,
        description: `${evaluation.group} / ${evaluation.submittedCount} soumise(s), ${evaluation.validatedCount} validee(s)`,
        category: 'Notes',
        meta: evaluation.professor,
        priority: evaluation.rejectedCount > 0 ? ('critical' as const) : ('high' as const),
        target: 'notes',
      }));
    const examDrafts = this.plannedExams()
      .filter((exam) => !this.isPublishedExam(exam))
      .map((exam) => ({
        id: `admin-exam-${exam.evaluationId}-${exam.status}`,
        title: `Examen non publie - ${exam.subject}`,
        description: `${exam.group} / ${exam.day} ${exam.startTime}-${exam.endTime}`,
        category: 'Examens',
        meta: exam.status,
        priority: 'normal' as const,
        target: 'exams',
      }));
    const attendanceAlerts = this.todayAttendanceSessions()
      .filter((session) => session.missingCount > 0 || session.collectiveAbsenceStatus !== 'Aucune')
      .map((session) => ({
        id: `admin-attendance-${session.sessionId}-${session.status}-${session.collectiveAbsenceStatus}`,
        title: `Presence a verifier - ${session.subject}`,
        description: `${session.group} / ${session.missingCount} appel(s) manquant(s)`,
        category: 'Presences',
        meta: session.collectiveAbsenceStatus,
        priority: session.collectiveAbsenceStatus !== 'Aucune' ? ('critical' as const) : ('high' as const),
        target: 'attendance',
      }));
    const eliminations = this.eliminations()
      .filter((record) => record.status !== 'Renseigne')
      .map((record) => ({
        id: `admin-elimination-${record.id}-${record.status}`,
        title: `Absence critique - ${record.studentName}`,
        description: `${record.subject} / ${record.absenceCount} absence(s)`,
        category: 'Eliminations',
        meta: record.group,
        priority: 'critical' as const,
        target: 'attendance',
      }));

    return [...eliminations, ...attendanceAlerts, ...noteFlows, ...examDrafts];
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
  protected readonly usersPage = signal(0);
  protected readonly usersPageSize = signal(20);
  protected readonly usersTotalElements = signal(0);
  protected readonly usersTotalPages = signal(0);
  protected readonly totalUserElements = signal(0);
  protected readonly activeUserElements = signal(0);
  protected readonly userRoleCounts = signal<Record<string, number>>({});
  protected readonly userDepartmentOptions = signal<readonly string[]>([]);
  protected readonly userGroupOptions = signal<readonly string[]>([]);
  protected readonly departmentUserSummaries = signal<
    readonly { department: string; students: number; professors: number }[]
  >([]);
  protected readonly userProfessorsByDepartment = signal<readonly ProfessorDepartmentGroup[]>([]);

  protected readonly examRows = signal<readonly ExamRow[]>([]);
  protected readonly academicYears = signal<readonly AdminAcademicYear[]>([]);
  protected readonly examPlanningOptions = signal<AdminExamPlanningOptions | null>(null);
  protected readonly plannedExams = signal<readonly AdminPlannedExam[]>([]);
  protected readonly examDrafts = signal<readonly ExamDraftRow[]>([]);
  protected readonly selectedTpRequestId = signal<number | null>(null);
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

  protected readonly filteredNoteValidationEvaluations = computed(() => {
    const query = this.searchTerm().trim().toLowerCase();

    return this.noteValidationEvaluations().filter((evaluation) =>
      this.matchesText(query, [
        evaluation.label,
        evaluation.type,
        evaluation.subject,
        evaluation.group,
        evaluation.professor,
        evaluation.status,
      ])
    );
  });

  protected readonly noteValidationSummary = computed(() => {
    const evaluations = this.noteValidationEvaluations();
    const totalNotes = evaluations.reduce((total, evaluation) => total + evaluation.totalNotes, 0);
    const submitted = evaluations.reduce((total, evaluation) => total + evaluation.submittedCount, 0);
    const validated = evaluations.reduce((total, evaluation) => total + evaluation.validatedCount, 0);
    const rejected = evaluations.reduce((total, evaluation) => total + evaluation.rejectedCount, 0);
    const published = evaluations.reduce((total, evaluation) => total + evaluation.publishedCount, 0);
    const pendingEvaluations = evaluations.filter((evaluation) => evaluation.status !== 'Publiee').length;

    return {
      evaluations: evaluations.length,
      pendingEvaluations,
      submitted,
      validated,
      rejected,
      published,
      totalNotes,
    };
  });

  protected readonly selectedNoteKpis = computed(() => {
    const evaluation = this.selectedNoteEvaluation();

    if (!evaluation) {
      return [];
    }

    return [
      { label: 'Soumises', value: evaluation.submittedCount.toString() },
      { label: 'Validees', value: evaluation.validatedCount.toString() },
      { label: 'Rejetees', value: evaluation.rejectedCount.toString() },
      { label: 'Publiees', value: evaluation.publishedCount.toString() },
    ];
  });

  protected readonly noteWorkflowStages = computed<readonly NoteWorkflowStage[]>(() => {
    const summary = this.noteValidationSummary();

    return [
      {
        label: 'Soumises',
        value: this.formatNumber(summary.submitted),
        meta: 'En attente de controle',
        tone: 'submitted',
      },
      {
        label: 'Validees',
        value: this.formatNumber(summary.validated),
        meta: 'Pretes pour publication',
        tone: 'validated',
      },
      {
        label: 'Rejetees',
        value: this.formatNumber(summary.rejected),
        meta: 'Correction demandee',
        tone: 'rejected',
      },
      {
        label: 'Publiees',
        value: `${this.formatNumber(summary.published)}/${this.formatNumber(summary.totalNotes)}`,
        meta: 'Visibles par les etudiants',
        tone: 'published',
      },
    ];
  });

  protected readonly selectedNotePublicationRatio = computed(() => {
    const evaluation = this.selectedNoteEvaluation();
    return evaluation ? this.percent(evaluation.publishedCount, evaluation.totalNotes) : 0;
  });

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
    return this.users();
  });

  protected readonly departmentOptions = computed(() => {
    const departments = new Set<string>();

    this.academicRows().forEach((row) => {
      if (row.title) {
        departments.add(row.title);
      }
    });

    this.userDepartmentOptions().forEach((department) => departments.add(department));

    return ['Tous', ...Array.from(departments).sort((first, second) => first.localeCompare(second))];
  });

  protected readonly groupOptions = computed(() => {
    return [
      'Tous',
      ...Array.from(new Set(this.userGroupOptions())).filter((group) => group && group !== 'Non affecte'),
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
        this.userGroupOptions()
          .filter((group) => group && group !== 'Non affecte' && group !== 'Tous les groupes')
      )
    ).sort((first, second) => first.localeCompare(second))
  );

  protected readonly tpExamRequests = computed<readonly TpExamRequest[]>(() => {
    const subjects = this.examPlanningOptions()?.subjects ?? [];
    const groups = this.examPlanningOptions()?.groups ?? [];
    const professors = this.examPlanningOptions()?.professors ?? [];

    if (!subjects.length || !groups.length || !professors.length) {
      return [];
    }

    return subjects.slice(0, 4).map((subject, index) => ({
      id: index + 1,
      subjectId: subject.id,
      groupId: groups[index % groups.length].id,
      professorId: professors[index % professors.length].id,
      subject: subject.label,
      group: groups[index % groups.length].label,
      professor: professors[index % professors.length].label,
      requestedDate: this.offsetDate(2 + index),
      preferredRoom: `Labo ${index + 1}`,
      reason:
        index % 2 === 0
          ? 'Evaluation pratique necessitant une salle machine.'
          : 'Controle TP demande apres cloture du chapitre pratique.',
      status: this.selectedTpRequestId() === index + 1 ? 'Planifiee' : 'En attente',
    }));
  });

  protected readonly selectedTpRequest = computed(() =>
    this.tpExamRequests().find((request) => request.id === this.selectedTpRequestId()) ?? null
  );

  protected readonly examPlanningStats = computed(() => {
    const drafts = this.examDrafts();
    const ds = drafts.filter((draft) => draft.evaluationType === 'DS').length;
    const exams = drafts.filter((draft) => draft.evaluationType === 'Examen').length;
    const rooms = new Set(drafts.map((draft) => draft.room)).size;
    const guards = new Set(drafts.map((draft) => draft.professorId)).size;

    return [
      { label: 'Epreuves generees', value: drafts.length.toString(), meta: `${ds} DS / ${exams} examen(s)` },
      { label: 'Salles mobilisees', value: rooms.toString(), meta: 'Rotation selon disponibilite' },
      { label: 'Surveillants', value: guards.toString(), meta: 'Affectation automatique' },
    ];
  });

  protected readonly professorsByDepartment = computed<readonly ProfessorDepartmentGroup[]>(() =>
    this.userProfessorsByDepartment()
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
    evaluationType: ['Examen TP', [Validators.required]],
    subjectId: [0, [Validators.required, Validators.min(1)]],
    groupId: [0, [Validators.required, Validators.min(1)]],
    professorId: [0, [Validators.required, Validators.min(1)]],
    startDate: ['', [Validators.required]],
    startTime: ['09:00', [Validators.required]],
    endTime: ['10:30', [Validators.required]],
    building: ['Bloc B', [Validators.required]],
    room: ['', [Validators.required]],
    details: [''],
  });

  protected readonly examWeekForm = this.formBuilder.nonNullable.group({
    evaluationType: ['DS', [Validators.required]],
    departmentId: [0, [Validators.required, Validators.min(1)]],
    startDate: ['', [Validators.required]],
    endDate: ['', [Validators.required]],
    building: ['Bloc B', [Validators.required]],
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
    this.loadUsersPage(0);
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
        this.examRows.set((dashboard.exams ?? []).map((exam) => this.normalizeExam(exam)));
        this.isDashboardLoading.set(false);
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

  private loadUsersPage(page: number): void {
    this.isUsersLoading.set(true);

    this.dashboardApi
      .getUsersPage({
        page,
        size: this.usersPageSize(),
        search: this.searchTerm().trim(),
        role: this.selectedRole(),
        department: this.selectedDepartment(),
        group: this.selectedGroup(),
      })
      .subscribe({
        next: (response) => {
          this.applyUsersPage(response);
          this.isUsersLoading.set(false);
        },
        error: () => {
          this.users.set([]);
          this.usersTotalElements.set(0);
          this.usersTotalPages.set(0);
          this.isUsersLoading.set(false);
          this.toastMessage.set("Impossible de charger l'annuaire pagine.");
        },
      });
  }

  private applyUsersPage(response: AdminUserPageResponse): void {
    this.users.set((response.content ?? []).map((user) => this.normalizeUser(user)));
    this.usersPage.set(response.page ?? 0);
    this.usersPageSize.set(response.size ?? this.usersPageSize());
    this.usersTotalElements.set(response.totalElements ?? 0);
    this.usersTotalPages.set(response.totalPages ?? 0);
    this.totalUserElements.set(response.totalUserElements ?? response.totalElements ?? 0);
    this.activeUserElements.set(response.activeUserElements ?? 0);
    this.userRoleCounts.set(response.roleCounts ?? {});
    this.userDepartmentOptions.set(response.departments ?? []);
    this.userGroupOptions.set(response.groups ?? []);
    this.departmentUserSummaries.set(response.departmentSummaries ?? []);
    this.userProfessorsByDepartment.set(
      (response.professorsByDepartment ?? []).map((group) => ({
        department: group.department,
        professors: (group.professors ?? []).map((professor) => this.normalizeUser(professor)),
      }))
    );
  }

  protected setSection(section: AdminSection): void {
    this.activeSection.set(section);
    if (section === 'users') {
      this.loadUsersPage(this.usersPage());
    }
  }

  protected openNotificationTarget(item: NotificationCenterItem): void {
    this.setSection(item.target as AdminSection);
  }

  protected logout(): void {
    this.authService.logout();
    this.router.navigate(['/']);
  }

  protected toggleTheme(): void {
    this.themeService.toggle();
  }

  protected toggleSidebar(): void {
    this.shellPreferences.toggleSidebar();
  }

  protected updateSearch(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.searchTerm.set(input.value);
    if (this.activeSection() === 'users') {
      this.loadUsersPage(0);
    }
  }

  protected updateDepartmentFilter(event: Event): void {
    this.selectedDepartment.set(this.readSelectValue(event));
    this.selectedGroup.set('Tous');
    this.loadUsersPage(0);
  }

  protected updateGroupFilter(event: Event): void {
    this.selectedGroup.set(this.readSelectValue(event));
    this.loadUsersPage(0);
  }

  protected updateRoleFilter(event: Event): void {
    this.selectedRole.set(this.readSelectValue(event));
    this.loadUsersPage(0);
  }

  protected updateUsersPageSize(event: Event): void {
    const size = Number(this.readSelectValue(event));
    this.usersPageSize.set(Number.isFinite(size) ? size : 20);
    this.loadUsersPage(0);
  }

  protected goToUsersPage(page: number): void {
    this.loadUsersPage(page);
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
    if (this.examForm.controls.evaluationType.value !== 'Examen TP' || !this.selectedTpRequest()) {
      this.toastMessage.set("Un examen TP doit etre lie a une demande professeur.");
      return;
    }

    if (this.examForm.invalid) {
      this.examForm.markAllAsTouched();
      return;
    }

    this.dashboardApi
      .createPlannedExam(this.buildExamPlanningRequest())
      .subscribe({
        next: (exam) => {
          this.toastMessage.set(`${exam.type} ${exam.subject} ajoute en brouillon pour ${exam.group}.`);
          this.selectedTpRequestId.set(null);
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
    if (this.examWeekForm.invalid) {
      this.examWeekForm.markAllAsTouched();
      return;
    }

    this.generateExamWeek();
  }

  protected removeExamDraft(draftId: number): void {
    this.examDrafts.update((drafts) => drafts.filter((draft) => draft.id !== draftId));
  }

  protected clearGeneratedWeek(): void {
    this.examDrafts.set([]);
  }

  protected planTpRequest(request: TpExamRequest): void {
    this.selectedTpRequestId.set(request.id);
    this.examForm.setValue({
      evaluationType: 'Examen TP',
      subjectId: request.subjectId,
      groupId: request.groupId,
      professorId: request.professorId,
      startDate: request.requestedDate,
      startTime: '14:00',
      endTime: '15:30',
      building: 'Bloc TP',
      room: request.preferredRoom,
      details: request.reason,
    });
  }

  protected generateExamWeek(): void {
    if (this.examWeekForm.invalid) {
      this.examWeekForm.markAllAsTouched();
      return;
    }

    const { evaluationType, departmentId, startDate, endDate, building } = this.examWeekForm.getRawValue();
    const dates = this.businessDaysBetween(startDate, endDate);
    const groups = this.examGroupsForDepartment(departmentId);
    const subjects = this.examSubjectsForDepartment(departmentId);
    const professors = this.examPlanningOptions()?.professors ?? [];
    const rooms = ['Salle 101', 'Salle 104', 'Salle 201', 'Salle 204', 'Amphi A'];
    const duration = evaluationType === 'DS' ? 60 : 90;
    const startSlots = evaluationType === 'DS' ? ['09:00', '10:30', '12:00'] : ['09:00', '11:00'];

    if (!groups.length || !subjects.length) {
      this.toastMessage.set('Aucun groupe ou matiere disponible pour cette filiere.');
      return;
    }

    const drafts = groups.flatMap((group, groupIndex) =>
      subjects.map((subject, subjectIndex) => {
        const index = groupIndex * subjects.length + subjectIndex;
        const date = dates[Math.floor(index / startSlots.length) % dates.length] ?? startDate;
        const startTime = startSlots[index % startSlots.length];
        const professor = professors[index % Math.max(1, professors.length)];
        const room = rooms[index % rooms.length];

        return {
          id: ++this.examDraftCounter,
          evaluationType,
          subjectId: subject.id,
          groupId: group.id,
          professorId: professor?.id ?? 0,
          examDate: date,
          startTime,
          endTime: this.addMinutes(startTime, duration),
          building,
          room,
          details: `Genere automatiquement pour la filiere ${this.optionLabel('department', departmentId)}`,
          dayLabel: this.frenchDayLabel(date),
          roomSource: 'Disponible',
          guardLabel: professor?.label ?? 'Surveillant a definir',
        };
      })
    );

    this.examDrafts.set(drafts);
    this.toastMessage.set(
      `${drafts.length} epreuve(s) generee(s) pour la filiere ${this.optionLabel('department', departmentId)}.`
    );
  }

  protected shiftExamDraft(draftId: number, minutes: number): void {
    this.examDrafts.update((drafts) =>
      drafts.map((draft) =>
        draft.id === draftId
          ? {
              ...draft,
              startTime: this.addMinutes(draft.startTime, minutes),
              endTime: this.addMinutes(draft.endTime, minutes),
            }
          : draft
      )
    );
  }

  protected rotateDraftRoom(draftId: number): void {
    const rooms = ['Salle 101', 'Salle 104', 'Salle 201', 'Salle 204', 'Amphi A'];

    this.examDrafts.update((drafts) =>
      drafts.map((draft) => {
        if (draft.id !== draftId) {
          return draft;
        }

        const nextRoom = rooms[(rooms.indexOf(draft.room) + 1) % rooms.length] ?? rooms[0];

        return {
          ...draft,
          room: nextRoom,
          roomSource: 'Modifiee',
        };
      })
    );
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
          const weekStart = exams[0]?.weekStart;

          if (weekStart) {
            this.dashboardApi.publishExamWeek(weekStart).subscribe({
              next: (publishedExams) => {
                this.toastMessage.set(`${publishedExams.length} examen(s) publie(s) pour la semaine.`);
                this.loadExamPlanning();
                this.loadDashboard();
              },
              error: () => {
                this.toastMessage.set(
                  `${exams.length} examen(s) planifie(s), publication semaine a relancer depuis le calendrier.`
                );
                this.loadExamPlanning();
                this.loadDashboard();
              },
            });
            return;
          }

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

  protected optionLabel(kind: 'subject' | 'group' | 'professor' | 'department', id: number): string {
    const options = this.examPlanningOptions();
    const source =
      kind === 'subject'
        ? options?.subjects
        : kind === 'group'
          ? options?.groups
          : kind === 'department'
            ? options?.departments
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
      : action.includes('examen')
        ? 'exams'
      : action.includes('notes')
        ? 'notes'
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
    return this.userRoleCounts()[role] ?? 0;
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

  protected noteProgressPercent(evaluation: AdminNoteValidationEvaluation): number {
    return this.percent(evaluation.publishedCount, evaluation.totalNotes);
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

    if (this.examWeekForm.controls.departmentId.value === 0 && options.departments[0]) {
      this.examWeekForm.controls.departmentId.setValue(options.departments[0].id);
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

  private examGroupsForDepartment(departmentId: number): readonly AdminPlanningOption[] {
    const departmentLabel = this.optionLabel('department', departmentId).toLowerCase();
    const groups = this.examPlanningOptions()?.groups ?? [];

    return groups.filter((group) => group.meta.toLowerCase() === departmentLabel);
  }

  private examSubjectsForDepartment(departmentId: number): readonly AdminPlanningOption[] {
    const subjects = this.examPlanningOptions()?.subjects ?? [];
    const departmentLabel = this.optionLabel('department', departmentId).toLowerCase();
    const scoped = subjects.filter((subject) => {
      const meta = `${subject.label} ${subject.meta}`.toLowerCase();
      return departmentLabel !== 'non renseigne' && meta.includes(departmentLabel);
    });

    return scoped.length > 0 ? scoped : subjects;
  }

  private businessDaysBetween(startDate: string, endDate: string): string[] {
    const start = new Date(`${startDate}T00:00:00`);
    const end = new Date(`${endDate}T00:00:00`);

    if (Number.isNaN(start.getTime()) || Number.isNaN(end.getTime()) || start > end) {
      return startDate ? [startDate] : [];
    }

    const dates: string[] = [];
    const cursor = new Date(start);

    while (cursor <= end && dates.length < 6) {
      const day = cursor.getDay();
      if (day >= 1 && day <= 6) {
        dates.push(this.toIsoDate(cursor));
      }
      cursor.setDate(cursor.getDate() + 1);
    }

    return dates.length > 0 ? dates : [startDate];
  }

  private offsetDate(days: number): string {
    const date = new Date();
    date.setDate(date.getDate() + days);
    return this.toIsoDate(date);
  }

  private toIsoDate(date: Date): string {
    const year = date.getFullYear();
    const month = `${date.getMonth() + 1}`.padStart(2, '0');
    const day = `${date.getDate()}`.padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  private addMinutes(time: string, minutes: number): string {
    const [hours, mins] = time.split(':').map((value) => Number(value));
    const total = (Number.isFinite(hours) ? hours : 0) * 60 + (Number.isFinite(mins) ? mins : 0) + minutes;
    const normalized = ((total % 1440) + 1440) % 1440;
    const nextHours = Math.floor(normalized / 60).toString().padStart(2, '0');
    const nextMinutes = (normalized % 60).toString().padStart(2, '0');
    return `${nextHours}:${nextMinutes}`;
  }

  private frenchDayLabel(value: string): string {
    const date = new Date(`${value}T00:00:00`);
    const days = ['Dimanche', 'Lundi', 'Mardi', 'Mercredi', 'Jeudi', 'Vendredi', 'Samedi'];
    return Number.isNaN(date.getTime()) ? value : days[date.getDay()];
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

}
