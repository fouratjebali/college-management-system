import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

type AdminSection = 'overview' | 'structure' | 'users' | 'calendar';

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
}

interface ExamRow {
  subject: string;
  group: string;
  date: string;
  room: string;
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

  protected readonly activeSection = signal<AdminSection>('overview');
  protected readonly searchTerm = signal('');
  protected readonly toastMessage = signal('Espace admin charge avec succes.');

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

  protected readonly stats: readonly StatCard[] = [
    { label: 'Etudiants', value: '1 248', trend: '+12 ce mois', tone: 'light' },
    { label: 'Professeurs', value: '86', trend: '7 departements', tone: 'steel' },
    { label: 'Cours actifs', value: '142', trend: 'Semestre 2', tone: 'warm' },
    { label: 'Groupes', value: '34', trend: '4 niveaux', tone: 'sand' },
  ];

  protected readonly activitySeries = [
    { label: 'Lun', value: 48 },
    { label: 'Mar', value: 68 },
    { label: 'Mer', value: 54 },
    { label: 'Jeu', value: 82 },
    { label: 'Ven', value: 76 },
  ] as const;

  protected readonly quickActions = [
    'Creer un departement',
    'Ajouter un utilisateur',
    'Planifier un examen',
    'Publier les notes',
  ] as const;

  protected readonly academicRows: readonly AcademicRow[] = [
    {
      code: 'GI',
      title: 'Genie Informatique',
      meta: '12 groupes, 38 matieres',
      status: 'Ouvert',
    },
    {
      code: 'GE',
      title: 'Genie Electrique',
      meta: '8 groupes, 24 matieres',
      status: 'Ouvert',
    },
    {
      code: 'MP',
      title: 'Maintenance Industrielle',
      meta: '6 groupes, 18 matieres',
      status: 'Audit',
    },
  ];

  protected readonly users: readonly UserRow[] = [
    {
      name: 'Amina Gharbi',
      email: 'amina.gharbi@issatso.tn',
      role: 'Administrateur',
      status: 'Active',
    },
    {
      name: 'Nour Ben Ali',
      email: 'nour.benali@issatso.tn',
      role: 'Professeur',
      status: 'Active',
    },
    {
      name: 'Yassine Mansouri',
      email: 'yassine.mansouri@issatso.tn',
      role: 'Etudiant',
      status: 'Pending',
    },
    {
      name: 'Sarra Trabelsi',
      email: 'sarra.trabelsi@issatso.tn',
      role: 'Professeur',
      status: 'Active',
    },
  ];

  protected readonly examRows: readonly ExamRow[] = [
    {
      subject: 'Architecture Logicielle',
      group: 'GI-3A',
      date: '22 Avril 2026, 09:00',
      room: 'Bloc B / Salle 204',
    },
    {
      subject: 'Bases de Donnees',
      group: 'GI-2B',
      date: '24 Avril 2026, 11:00',
      room: 'Bloc A / Labo 3',
    },
    {
      subject: 'Automatique',
      group: 'GE-2A',
      date: '27 Avril 2026, 14:00',
      room: 'Bloc C / Salle 102',
    },
  ];

  protected readonly filteredUsers = computed(() => {
    const query = this.searchTerm().trim().toLowerCase();

    if (!query) {
      return this.users;
    }

    return this.users.filter((user) =>
      [user.name, user.email, user.role, user.status].some((value) =>
        value.toLowerCase().includes(query)
      )
    );
  });

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
  });

  protected readonly examForm = this.formBuilder.nonNullable.group({
    subject: ['', [Validators.required, Validators.minLength(3)]],
    group: ['', [Validators.required]],
    date: ['', [Validators.required]],
    room: ['', [Validators.required]],
  });

  protected readonly gradeForm = this.formBuilder.nonNullable.group({
    evaluation: ['', [Validators.required, Validators.minLength(3)]],
    group: ['', [Validators.required]],
    visibility: ['Etudiants concernes', [Validators.required]],
  });

  protected setSection(section: AdminSection): void {
    this.activeSection.set(section);
    this.toastMessage.set(`${this.sectionLabel(section)} pret pour la gestion.`);
  }

  protected updateSearch(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.searchTerm.set(input.value);
  }

  protected submitStructure(): void {
    if (this.structureForm.invalid) {
      this.structureForm.markAllAsTouched();
      return;
    }

    const { entity, name } = this.structureForm.getRawValue();
    this.toastMessage.set(`${entity} "${name}" prepare pour creation via API.`);
  }

  protected submitUser(): void {
    if (this.userForm.invalid) {
      this.userForm.markAllAsTouched();
      return;
    }

    const { fullName, role } = this.userForm.getRawValue();
    this.toastMessage.set(`Compte ${role.toLowerCase()} pour ${fullName} prepare.`);
  }

  protected submitExam(): void {
    if (this.examForm.invalid) {
      this.examForm.markAllAsTouched();
      return;
    }

    const { subject, group } = this.examForm.getRawValue();
    this.toastMessage.set(`Examen ${subject} planifie dans le calendrier ${group}.`);
  }

  protected publishGrades(): void {
    if (this.gradeForm.invalid) {
      this.gradeForm.markAllAsTouched();
      return;
    }

    const { evaluation } = this.gradeForm.getRawValue();
    this.toastMessage.set(`Publication des notes "${evaluation}" prete a confirmer.`);
  }

  protected activateQuickAction(action: string): void {
    const target: AdminSection = action.includes('utilisateur')
      ? 'users'
      : action.includes('examen') || action.includes('notes')
        ? 'calendar'
        : 'structure';

    this.setSection(target);
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
