import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

export interface ProfessorStat {
  label: string;
  value: string;
  trend: string;
  tone: 'light' | 'warm' | 'steel' | 'sand';
}

export interface ProfessorTeachingRow {
  id: number;
  code: string;
  title: string;
  semester: string;
  year: string;
  department: string;
  coefficient: string;
}

export interface ProfessorGroupRow {
  id: number;
  label: string;
  level: string;
  year: string;
  department: string;
  studentCount: number;
}

export interface ProfessorSessionRow {
  id: number;
  teachingId: number;
  subject: string;
  groupId: number;
  group: string;
  day: string;
  start: string;
  end: string;
  room: string;
  type: string;
  collectiveAbsenceStatus: string;
}

export interface ProfessorEvaluationRow {
  id: number;
  sessionId: number;
  label: string;
  type: string;
  date: string;
  coefficient: string;
  groupId: number;
  group: string;
  subject: string;
}

export interface ProfessorStudentRow {
  id: number;
  name: string;
  email: string;
  matricule: string;
  groupId: number;
  group: string;
  level: string;
}

export interface ProfessorGradeRow {
  studentId: number;
  evaluationId: number;
  value: string;
  status: string;
  remark: string;
}

export interface ProfessorAttendanceRow {
  studentId: number;
  sessionId: number;
  status: string;
  date: string;
}

export interface ProfessorMaterialRow {
  id: number;
  teachingId: number;
  title: string;
  fileName: string;
  fileType: string;
  size: string;
  date: string;
  subject: string;
}

export interface ProfessorDashboardResponse {
  stats: ProfessorStat[];
  teachings: ProfessorTeachingRow[];
  groups: ProfessorGroupRow[];
  sessions: ProfessorSessionRow[];
  evaluations: ProfessorEvaluationRow[];
  students: ProfessorStudentRow[];
  grades: ProfessorGradeRow[];
  attendance: ProfessorAttendanceRow[];
  materials: ProfessorMaterialRow[];
}

export interface SaveGradePayload {
  valeur: number;
  statut: string;
  remarque: string;
  evaluation: { id: number };
  etudiant: { id: number };
}

export interface SaveAttendancePayload {
  statut: string;
  dateSaisie: string;
  seance: { id: number };
  etudiant: { id: number };
}

export interface CreateSessionPayload {
  typeSeance: string;
  joursemaine: string;
  heureDebut: string;
  heureFin: string;
  salle: string;
  batiment: string;
  enseignement: { id: number };
  groupe: { id: number };
}

@Injectable({
  providedIn: 'root',
})
export class ProfessorDashboardApi {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/professor';

  getDashboard(): Observable<ProfessorDashboardResponse> {
    return this.http.get<ProfessorDashboardResponse>(`${this.apiUrl}/dashboard`);
  }

  saveGrade(payload: SaveGradePayload): Observable<unknown> {
    return this.http.post('/api/notes', payload);
  }

  saveAttendance(payload: SaveAttendancePayload): Observable<unknown> {
    return this.http.post('/api/presences', payload);
  }

  reportCollectiveAbsence(sessionId: number): Observable<unknown> {
    return this.http.patch(`${this.apiUrl}/attendance/${sessionId}/collective-absence`, {});
  }

  createMakeupSession(payload: CreateSessionPayload): Observable<unknown> {
    return this.http.post('/api/schedules', payload);
  }

  uploadMaterial(titre: string, enseignementId: number, file: File): Observable<ProfessorMaterialRow> {
    const data = new FormData();
    data.append('titre', titre);
    data.append('enseignementId', enseignementId.toString());
    data.append('file', file);

    return this.http.post<ProfessorMaterialRow>('/api/supports/upload', data);
  }
}
