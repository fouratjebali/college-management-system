import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { AuthService } from '../../../../core/services/auth';
import {
  CreateSessionPayload,
  ProfessorAttendanceRow,
  ProfessorDashboardApi,
  ProfessorEvaluationRow,
  ProfessorEvaluationPayload,
  ProfessorGradeRow,
  ProfessorGroupRow,
  ProfessorMaterialRow,
  ProfessorSessionRow,
  ProfessorStat,
  ProfessorStudentRow,
  ProfessorTeachingRow,
  SaveAttendancePayload,
  SaveGradePayload,
} from '../../services/professor-dashboard-api';

type ProfessorSection =
  | 'overview'
  | 'schedule'
  | 'evaluations'
  | 'grades'
  | 'attendance'
  | 'materials';

interface NavItem {
  id: ProfessorSection;
  label: string;
  description: string;
}

interface GradeDraft {
  value: string;
  status: string;
  remark: string;
}

interface ScheduleDayGroup {
  day: string;
  sessions: readonly ProfessorSessionRow[];
}

@Component({
  selector: 'app-professor-dashboard',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './professor-dashboard.html',
  styleUrl: './professor-dashboard.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProfessorDashboardComponent {
  private readonly formBuilder = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly dashboardApi = inject(ProfessorDashboardApi);

  protected readonly activeSection = signal<ProfessorSection>('overview');
  protected readonly searchTerm = signal('');
  protected readonly toastMessage = signal('Espace professeur charge avec succes.');
  protected readonly isDashboardLoading = signal(true);
  protected readonly selectedGradeGroupId = signal<number | null>(null);
  protected readonly selectedEvaluationId = signal<number | null>(null);
  protected readonly selectedAttendanceGroupId = signal<number | null>(null);
  protected readonly selectedSessionId = signal<number | null>(null);
  protected readonly selectedMaterialTeachingId = signal<number | null>(null);
  protected readonly selectedFileName = signal('');
  protected readonly editingEvaluationId = signal<number | null>(null);

  protected readonly stats = signal<readonly ProfessorStat[]>([]);
  protected readonly teachings = signal<readonly ProfessorTeachingRow[]>([]);
  protected readonly groups = signal<readonly ProfessorGroupRow[]>([]);
  protected readonly sessions = signal<readonly ProfessorSessionRow[]>([]);
  protected readonly evaluations = signal<readonly ProfessorEvaluationRow[]>([]);
  protected readonly students = signal<readonly ProfessorStudentRow[]>([]);
  protected readonly grades = signal<readonly ProfessorGradeRow[]>([]);
  protected readonly attendance = signal<readonly ProfessorAttendanceRow[]>([]);
  protected readonly materials = signal<readonly ProfessorMaterialRow[]>([]);
  private readonly gradeDrafts = signal<Record<number, GradeDraft>>({});
  private readonly attendanceDrafts = signal<Record<number, string>>({});
  private selectedFile: File | null = null;

  protected readonly navItems: readonly NavItem[] = [
    {
      id: 'overview',
      label: 'Dashboard',
      description: 'Enseignements, groupes et seances',
    },
    {
      id: 'schedule',
      label: 'Emploi du temps',
      description: 'Jours, horaires, salles et groupes',
    },
    {
      id: 'evaluations',
      label: 'Evaluations',
      description: 'DS, examens et coefficients',
    },
    {
      id: 'grades',
      label: 'Saisie notes',
      description: 'Evaluations et envoi batch',
    },
    {
      id: 'attendance',
      label: 'Presences',
      description: 'Appel par seance',
    },
    {
      id: 'materials',
      label: 'Rattrapages & Supports',
      description: 'Seances et fichiers',
    },
  ];

  protected readonly quickActions = [
    'Saisir des notes',
    'Creer une evaluation',
    'Faire appel',
    'Voir emploi du temps',
    'Creer un rattrapage',
    'Ajouter un support',
  ] as const;

  protected readonly gradeForm = this.formBuilder.nonNullable.group({
    groupId: [0, [Validators.required, Validators.min(1)]],
    evaluationId: [0, [Validators.required, Validators.min(1)]],
  });

  protected readonly attendanceForm = this.formBuilder.nonNullable.group({
    groupId: [0, [Validators.required, Validators.min(1)]],
    sessionId: [0, [Validators.required, Validators.min(1)]],
  });

  protected readonly evaluationForm = this.formBuilder.nonNullable.group({
    libelle: ['', [Validators.required, Validators.minLength(3)]],
    typeEvaluation: ['DS', [Validators.required]],
    dateEvaluation: ['', [Validators.required]],
    sessionId: [0, [Validators.required, Validators.min(1)]],
  });

  protected readonly makeupForm = this.formBuilder.nonNullable.group({
    teachingId: [0, [Validators.required, Validators.min(1)]],
    groupId: [0, [Validators.required, Validators.min(1)]],
    typeSeance: ['Rattrapage', [Validators.required]],
    day: ['Samedi', [Validators.required]],
    start: ['09:00', [Validators.required]],
    end: ['10:30', [Validators.required]],
    batiment: ['Bloc B', [Validators.required]],
    salle: ['', [Validators.required]],
  });

  protected readonly materialForm = this.formBuilder.nonNullable.group({
    teachingId: [0, [Validators.required, Validators.min(1)]],
    title: ['', [Validators.required, Validators.minLength(3)]],
  });

  protected readonly selectedEvaluation = computed(() =>
    this.evaluations().find((evaluation) => evaluation.id === this.selectedEvaluationId()) ?? null
  );

  protected readonly selectedSession = computed(() =>
    this.sessions().find((session) => session.id === this.selectedSessionId()) ?? null
  );

  protected readonly filteredTeachings = computed(() => {
    const query = this.searchTerm().trim().toLowerCase();

    return this.teachings().filter((teaching) =>
      this.matchesQuery(query, [
        teaching.code,
        teaching.title,
        teaching.semester,
        teaching.year,
        teaching.department,
      ])
    );
  });

  protected readonly upcomingSessions = computed(() => {
    const query = this.searchTerm().trim().toLowerCase();

    return this.sessions()
      .filter((session) =>
        this.matchesQuery(query, [
          session.subject,
          session.group,
          session.day,
          session.start,
          session.end,
          session.room,
          session.type,
        ])
      )
      .slice(0, 6);
  });

  protected readonly filteredGroups = computed(() => {
    const query = this.searchTerm().trim().toLowerCase();

    return this.groups().filter((group) =>
      this.matchesQuery(query, [group.label, group.level, group.year, group.department])
    );
  });

  protected readonly gradeEvaluations = computed(() => {
    const groupId = this.selectedGradeGroupId();

    return this.evaluations().filter((evaluation) => !groupId || evaluation.groupId === groupId);
  });

  protected readonly filteredEvaluations = computed(() => {
    const query = this.searchTerm().trim().toLowerCase();

    return this.evaluations().filter((evaluation) =>
      this.matchesQuery(query, [
        evaluation.label,
        evaluation.type,
        evaluation.date,
        evaluation.group,
        evaluation.subject,
      ])
    );
  });

  protected readonly attendanceSessions = computed(() => {
    const groupId = this.selectedAttendanceGroupId();

    return this.sessions().filter((session) => !groupId || session.groupId === groupId);
  });

  protected readonly gradeStudents = computed(() => {
    const groupId = this.selectedGradeGroupId();

    if (!groupId) {
      return [];
    }

    return this.students().filter((student) => student.groupId === groupId);
  });

  protected readonly attendanceStudents = computed(() => {
    const groupId = this.selectedAttendanceGroupId();

    if (!groupId) {
      return [];
    }

    return this.students().filter((student) => student.groupId === groupId);
  });

  protected readonly attendanceSummary = computed(() => {
    const students = this.attendanceStudents();
    const absent = students.filter((student) => this.isAbsent(student.id)).length;

    return {
      absent,
      present: students.length - absent,
      total: students.length,
    };
  });

  protected readonly scheduleDayGroups = computed<readonly ScheduleDayGroup[]>(() => {
    const dayOrder = ['Lundi', 'Mardi', 'Mercredi', 'Jeudi', 'Vendredi', 'Samedi'];

    return dayOrder
      .map((day) => ({
        day,
        sessions: this.sessions()
          .filter((session) => session.day.toLowerCase() === day.toLowerCase())
          .sort((first, second) => first.start.localeCompare(second.start)),
      }))
      .filter((group) => group.sessions.length > 0);
  });

  protected readonly materialsForTeaching = computed(() => {
    const teachingId = this.selectedMaterialTeachingId();
    const query = this.searchTerm().trim().toLowerCase();

    return this.materials().filter((material) => {
      const matchesTeaching = !teachingId || material.teachingId === teachingId;
      const matchesQuery = this.matchesQuery(query, [
        material.title,
        material.fileName,
        material.subject,
        material.fileType,
      ]);

      return matchesTeaching && matchesQuery;
    });
  });

  constructor() {
    this.loadDashboard();
  }

  private loadDashboard(): void {
    this.isDashboardLoading.set(true);

    this.dashboardApi.getDashboard().subscribe({
      next: (dashboard) => {
        this.stats.set(dashboard.stats ?? []);
        this.teachings.set(dashboard.teachings ?? []);
        this.groups.set(dashboard.groups ?? []);
        this.sessions.set(dashboard.sessions ?? []);
        this.evaluations.set(dashboard.evaluations ?? []);
        this.students.set(dashboard.students ?? []);
        this.grades.set(dashboard.grades ?? []);
        this.attendance.set(dashboard.attendance ?? []);
        this.materials.set(dashboard.materials ?? []);
        this.prepareDefaultSelections();
        this.isDashboardLoading.set(false);
        this.toastMessage.set('Donnees professeur synchronisees avec la base.');
      },
      error: () => {
        this.clearDashboard();
        this.isDashboardLoading.set(false);
        this.toastMessage.set(
          'Impossible de charger les donnees professeur. Verifiez la session puis reessayez.'
        );
      },
    });
  }

  protected setSection(section: ProfessorSection): void {
    this.activeSection.set(section);
  }

  protected activateQuickAction(action: string): void {
    const target: ProfessorSection = action.includes('notes')
      ? 'grades'
      : action.includes('evaluation')
        ? 'evaluations'
      : action.includes('appel')
        ? 'attendance'
        : action.includes('emploi')
          ? 'schedule'
          : 'materials';

    this.setSection(target);
  }

  protected logout(): void {
    this.authService.logout();
    this.router.navigate(['/']);
  }

  protected updateSearch(event: Event): void {
    this.searchTerm.set((event.target as HTMLInputElement).value);
  }

  protected updateGradeGroup(event: Event): void {
    const groupId = Number((event.target as HTMLSelectElement).value);
    this.selectGradeGroup(groupId);
  }

  protected updateEvaluation(event: Event): void {
    this.selectEvaluation(Number((event.target as HTMLSelectElement).value));
  }

  protected updateAttendanceGroup(event: Event): void {
    const groupId = Number((event.target as HTMLSelectElement).value);
    this.selectAttendanceGroup(groupId);
  }

  protected updateSession(event: Event): void {
    this.selectSession(Number((event.target as HTMLSelectElement).value));
  }

  protected updateEvaluationSession(event: Event): void {
    this.evaluationForm.controls.sessionId.setValue(Number((event.target as HTMLSelectElement).value));
  }

  protected updateMaterialTeaching(event: Event): void {
    const teachingId = Number((event.target as HTMLSelectElement).value);
    this.selectedMaterialTeachingId.set(teachingId > 0 ? teachingId : null);
  }

  protected updateGradeValue(studentId: number, event: Event): void {
    const draft = this.getGradeDraft(studentId);
    this.gradeDrafts.update((drafts) => ({
      ...drafts,
      [studentId]: {
        ...draft,
        value: (event.target as HTMLInputElement).value,
      },
    }));
  }

  protected updateGradeStatus(studentId: number, event: Event): void {
    const draft = this.getGradeDraft(studentId);
    this.gradeDrafts.update((drafts) => ({
      ...drafts,
      [studentId]: {
        ...draft,
        status: (event.target as HTMLSelectElement).value,
      },
    }));
  }

  protected updateGradeRemark(studentId: number, event: Event): void {
    const draft = this.getGradeDraft(studentId);
    this.gradeDrafts.update((drafts) => ({
      ...drafts,
      [studentId]: {
        ...draft,
        remark: (event.target as HTMLInputElement).value,
      },
    }));
  }

  protected toggleAttendanceStatus(studentId: number): void {
    const nextStatus = this.isAbsent(studentId) ? 'Present' : 'Absent';

    this.attendanceDrafts.update((drafts) => ({
      ...drafts,
      [studentId]: nextStatus,
    }));
  }

  protected markAllAttendance(status: 'Present' | 'Absent'): void {
    const drafts: Record<number, string> = {};

    this.attendanceStudents().forEach((student) => {
      drafts[student.id] = status;
    });

    this.attendanceDrafts.set(drafts);
    this.toastMessage.set(
      status === 'Present'
        ? 'Tous les etudiants sont marques presents.'
        : 'Tous les etudiants sont marques absents.'
    );
  }

  protected resetAttendanceDrafts(): void {
    const sessionId = this.selectedSessionId();

    if (!sessionId) {
      this.attendanceDrafts.set({});
      return;
    }

    this.seedAttendanceDrafts(sessionId);
    this.toastMessage.set("L'appel est reinitialise pour la seance selectionnee.");
  }

  protected submitGrades(): void {
    const evaluation = this.selectedEvaluation();

    if (!this.selectedGradeGroupId() || !evaluation || this.gradeStudents().length === 0) {
      this.toastMessage.set('Selectionnez un groupe et une evaluation avec des etudiants.');
      return;
    }

    const payloads = this.gradeStudents()
      .map((student): SaveGradePayload | null => {
        const draft = this.getGradeDraft(student.id);
        const value = Number(draft.value);

        if (!Number.isFinite(value) || draft.value.trim() === '') {
          return null;
        }

        return {
          valeur: value,
          statut: draft.status,
          remarque: draft.remark,
          evaluation: { id: evaluation.id },
          etudiant: { id: student.id },
        };
      })
      .filter((payload): payload is SaveGradePayload => payload !== null);

    if (payloads.length === 0) {
      this.toastMessage.set('Aucune note valide a enregistrer.');
      return;
    }

    forkJoin(payloads.map((payload) => this.dashboardApi.saveGrade(payload))).subscribe({
      next: () => {
        this.toastMessage.set(`${payloads.length} note(s) envoyee(s) en batch.`);
        this.loadDashboard();
      },
      error: () => {
        this.toastMessage.set('Erreur pendant la sauvegarde des notes.');
      },
    });
  }

  protected submitEvaluation(): void {
    if (this.evaluationForm.invalid) {
      this.evaluationForm.markAllAsTouched();
      return;
    }

    const raw = this.evaluationForm.getRawValue();
    const payload: ProfessorEvaluationPayload = {
      libelle: raw.libelle.trim(),
      typeEvaluation: raw.typeEvaluation,
      dateEvaluation: raw.dateEvaluation,
      seanceId: raw.sessionId,
    };
    const editingId = this.editingEvaluationId();
    const request = editingId
      ? this.dashboardApi.updateEvaluation(editingId, payload)
      : this.dashboardApi.createEvaluation(payload);

    request.subscribe({
      next: () => {
        this.toastMessage.set(
          editingId ? 'Evaluation modifiee avec succes.' : 'Evaluation creee avec succes.'
        );
        this.resetEvaluationForm();
        this.loadDashboard();
      },
      error: (error) => {
        this.toastMessage.set(
          error.error?.message ||
            error.error?.error ||
            "Impossible d'enregistrer l'evaluation."
        );
      },
    });
  }

  protected editEvaluation(evaluation: ProfessorEvaluationRow): void {
    this.editingEvaluationId.set(evaluation.id);
    this.evaluationForm.setValue({
      libelle: evaluation.label,
      typeEvaluation: evaluation.type,
      dateEvaluation: this.toDateTimeInputValue(evaluation.date),
      sessionId: evaluation.sessionId,
    });
    this.toastMessage.set(`Modification de ${evaluation.label}.`);
  }

  protected cancelEvaluationEdit(): void {
    this.resetEvaluationForm();
    this.toastMessage.set('Creation evaluation disponible.');
  }

  protected deleteEvaluation(evaluation: ProfessorEvaluationRow): void {
    this.dashboardApi.deleteEvaluation(evaluation.id).subscribe({
      next: () => {
        this.toastMessage.set(`Evaluation ${evaluation.label} supprimee.`);
        if (this.editingEvaluationId() === evaluation.id) {
          this.resetEvaluationForm();
        }
        this.loadDashboard();
      },
      error: (error) => {
        this.toastMessage.set(
          error.error?.message ||
            error.error?.error ||
            "Impossible de supprimer une evaluation contenant deja des notes."
        );
      },
    });
  }

  protected submitAttendance(): void {
    const session = this.selectedSession();

    if (!this.selectedAttendanceGroupId() || !session || this.attendanceStudents().length === 0) {
      this.toastMessage.set('Selectionnez un groupe et une seance avec des etudiants.');
      return;
    }

    const now = new Date().toISOString();
    const payloads = this.attendanceStudents().map(
      (student): SaveAttendancePayload => ({
        statut: this.getAttendanceStatus(student.id),
        dateSaisie: now,
        seance: { id: session.id },
        etudiant: { id: student.id },
      })
    );

    forkJoin(payloads.map((payload) => this.dashboardApi.saveAttendance(payload))).subscribe({
      next: () => {
        this.toastMessage.set(`${payloads.length} presence(s) sauvegardee(s).`);
        this.loadDashboard();
      },
      error: () => {
        this.toastMessage.set('Erreur pendant la sauvegarde des presences.');
      },
    });
  }

  protected reportCollectiveAbsence(): void {
    const session = this.selectedSession();

    if (!session) {
      this.toastMessage.set('Selectionnez une seance avant de signaler une absence collective.');
      return;
    }

    this.dashboardApi.reportCollectiveAbsence(session.id).subscribe({
      next: () => {
        this.toastMessage.set(`Absence collective signalee pour ${session.subject} - ${session.group}.`);
        this.loadDashboard();
      },
      error: (error) => {
        this.toastMessage.set(
          error.error?.message ||
            error.error?.error ||
            "Impossible de signaler l'absence collective."
        );
      },
    });
  }

  protected submitMakeupSession(): void {
    if (this.makeupForm.invalid) {
      this.makeupForm.markAllAsTouched();
      return;
    }

    const raw = this.makeupForm.getRawValue();
    const payload: CreateSessionPayload = {
      typeSeance: raw.typeSeance,
      joursemaine: raw.day,
      heureDebut: raw.start,
      heureFin: raw.end,
      salle: raw.salle,
      batiment: raw.batiment,
      enseignement: { id: raw.teachingId },
      groupe: { id: raw.groupId },
    };

    this.dashboardApi.createMakeupSession(payload).subscribe({
      next: () => {
        this.toastMessage.set('Seance de rattrapage creee avec succes.');
        this.loadDashboard();
      },
      error: () => {
        this.toastMessage.set('Creation du rattrapage impossible. Verifiez les conflits horaires.');
      },
    });
  }

  protected selectMaterialFile(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.item(0) ?? null;
    this.selectedFile = file;
    this.selectedFileName.set(file?.name ?? '');
  }

  protected submitMaterial(): void {
    if (this.materialForm.invalid || !this.selectedFile) {
      this.materialForm.markAllAsTouched();
      this.toastMessage.set('Choisissez un enseignement, un titre et un fichier.');
      return;
    }

    const { teachingId, title } = this.materialForm.getRawValue();

    this.dashboardApi.uploadMaterial(title, teachingId, this.selectedFile).subscribe({
      next: () => {
        this.toastMessage.set('Support de cours envoye avec succes.');
        this.selectedFile = null;
        this.selectedFileName.set('');
        this.materialForm.controls.title.reset('');
        this.loadDashboard();
      },
      error: () => {
        this.toastMessage.set('Upload du support impossible.');
      },
    });
  }

  protected getGradeDraft(studentId: number): GradeDraft {
    return this.gradeDrafts()[studentId] ?? {
      value: '',
      status: 'Soumise',
      remark: '',
    };
  }

  protected getAttendanceStatus(studentId: number): string {
    return this.attendanceDrafts()[studentId] ?? 'Present';
  }

  protected isAbsent(studentId: number): boolean {
    return this.getAttendanceStatus(studentId) === 'Absent';
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

  private prepareDefaultSelections(): void {
    const firstTeaching = this.teachings()[0];
    const firstGroup = this.groups()[0];

    if (firstTeaching) {
      this.makeupForm.controls.teachingId.setValue(firstTeaching.id);
      this.materialForm.controls.teachingId.setValue(firstTeaching.id);
      this.selectedMaterialTeachingId.set(firstTeaching.id);
    }

    if (firstGroup) {
      this.makeupForm.controls.groupId.setValue(firstGroup.id);
      this.selectGradeGroup(firstGroup.id);
      this.selectAttendanceGroup(firstGroup.id);
    }

    if (this.sessions()[0] && !this.evaluationForm.controls.sessionId.value) {
      this.evaluationForm.controls.sessionId.setValue(this.sessions()[0].id);
    }
  }

  private resetEvaluationForm(): void {
    this.editingEvaluationId.set(null);
    this.evaluationForm.reset({
      libelle: '',
      typeEvaluation: 'DS',
      dateEvaluation: '',
      sessionId: this.sessions()[0]?.id ?? 0,
    });
  }

  private toDateTimeInputValue(date: string): string {
    const [day, month, rest] = date.split('/');
    const [year, time] = rest?.split(' ') ?? [];

    if (!day || !month || !year || !time) {
      return '';
    }

    return `${year}-${month.padStart(2, '0')}-${day.padStart(2, '0')}T${time}`;
  }

  private selectGradeGroup(groupId: number): void {
    this.selectedGradeGroupId.set(groupId > 0 ? groupId : null);
    this.gradeForm.controls.groupId.setValue(groupId);

    const firstEvaluation = this.evaluations().find((evaluation) => evaluation.groupId === groupId);
    this.selectEvaluation(firstEvaluation?.id ?? 0);
  }

  private selectEvaluation(evaluationId: number): void {
    this.selectedEvaluationId.set(evaluationId > 0 ? evaluationId : null);
    this.gradeForm.controls.evaluationId.setValue(evaluationId);
    this.seedGradeDrafts(evaluationId);
  }

  private selectAttendanceGroup(groupId: number): void {
    this.selectedAttendanceGroupId.set(groupId > 0 ? groupId : null);
    this.attendanceForm.controls.groupId.setValue(groupId);

    const firstSession = this.sessions().find((session) => session.groupId === groupId);
    this.selectSession(firstSession?.id ?? 0);
  }

  private selectSession(sessionId: number): void {
    this.selectedSessionId.set(sessionId > 0 ? sessionId : null);
    this.attendanceForm.controls.sessionId.setValue(sessionId);
    this.seedAttendanceDrafts(sessionId);
  }

  private seedGradeDrafts(evaluationId: number): void {
    const drafts: Record<number, GradeDraft> = {};

    this.gradeStudents().forEach((student) => {
      const existingGrade = this.grades().find(
        (grade) => grade.evaluationId === evaluationId && grade.studentId === student.id
      );

      drafts[student.id] = {
        value: existingGrade?.value ?? '',
        status: existingGrade?.status ?? 'Soumise',
        remark: existingGrade?.remark ?? '',
      };
    });

    this.gradeDrafts.set(drafts);
  }

  private seedAttendanceDrafts(sessionId: number): void {
    const drafts: Record<number, string> = {};

    this.attendanceStudents().forEach((student) => {
      const existingAttendance = this.attendance().find(
        (item) => item.sessionId === sessionId && item.studentId === student.id
      );
      drafts[student.id] = existingAttendance?.status === 'Absent' ? 'Absent' : 'Present';
    });

    this.attendanceDrafts.set(drafts);
  }

  private clearDashboard(): void {
    this.stats.set([]);
    this.teachings.set([]);
    this.groups.set([]);
    this.sessions.set([]);
    this.evaluations.set([]);
    this.students.set([]);
    this.grades.set([]);
    this.attendance.set([]);
    this.materials.set([]);
    this.gradeDrafts.set({});
    this.attendanceDrafts.set({});
  }

  private matchesQuery(query: string, values: readonly string[]): boolean {
    return !query || values.some((value) => value.toLowerCase().includes(query));
  }

}
