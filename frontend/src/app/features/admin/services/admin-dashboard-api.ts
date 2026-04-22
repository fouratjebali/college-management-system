import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

export interface AdminStat {
  label: string;
  value: string;
  trend: string;
  tone: 'light' | 'warm' | 'steel' | 'sand';
}

export interface AdminAcademicRow {
  code: string;
  title: string;
  meta: string;
  status: string;
}

export interface AdminUserRow {
  name: string;
  email: string;
  role: string;
  status: 'Active' | 'Pending';
  department: string;
  group: string;
  specialty: string;
}

export interface AdminExamRow {
  subject: string;
  group: string;
  date: string;
  room: string;
  type: string;
  scope: string;
}

export interface AdminDashboardResponse {
  stats: AdminStat[];
  academicRows: AdminAcademicRow[];
  users: AdminUserRow[];
  exams: AdminExamRow[];
}

export interface AdminSemester {
  id: number;
  academicYearId: number;
  code: string;
  name: string;
  startDate: string;
  endDate: string;
  active: boolean;
  locked: boolean;
  status: string;
}

export interface AdminAcademicYear {
  id: number;
  label: string;
  startDate: string;
  endDate: string;
  active: boolean;
  locked: boolean;
  status: string;
  semesters: AdminSemester[];
}

export interface AdminAcademicYearRequest {
  label: string;
  startDate: string;
  endDate: string;
  active: boolean;
  locked: boolean;
}

export interface AdminSemesterRequest {
  code: string;
  name: string;
  startDate: string;
  endDate: string;
  active: boolean;
  locked: boolean;
}

export interface AdminPlanningOption {
  id: number;
  label: string;
  meta: string;
}

export interface AdminExamPlanningOptions {
  departments: AdminPlanningOption[];
  groups: AdminPlanningOption[];
  subjects: AdminPlanningOption[];
  professors: AdminPlanningOption[];
  activeAcademicYear: string;
  activeSemester: string;
}

export interface AdminExamPlanningRequest {
  evaluationType: string;
  subjectId: number;
  groupId: number;
  professorId: number;
  examDate: string;
  startTime: string;
  endTime: string;
  building: string;
  room: string;
  details: string;
}

export interface AdminExamPlanningBulkRequest {
  exams: AdminExamPlanningRequest[];
}

export interface AdminPlannedExam {
  evaluationId: number;
  seanceId: number;
  subject: string;
  subjectCode: string;
  group: string;
  professor: string;
  date: string;
  isoDate: string;
  day: string;
  weekStart: string;
  startTime: string;
  endTime: string;
  room: string;
  type: string;
  scope: string;
  status: string;
  publishedAt: string;
}

export interface AdminNoteValidationEvaluation {
  evaluationId: number;
  label: string;
  type: string;
  subject: string;
  group: string;
  professor: string;
  date: string;
  totalNotes: number;
  draftCount: number;
  submittedCount: number;
  validatedCount: number;
  rejectedCount: number;
  publishedCount: number;
  status: string;
}

export interface AdminNoteValidationStudent {
  noteId: number;
  studentId: number;
  studentName: string;
  matricule: string;
  value: string;
  status: string;
  remark: string;
  validationRemark: string;
}

export interface AdminNoteValidationDetail {
  evaluation: AdminNoteValidationEvaluation;
  notes: AdminNoteValidationStudent[];
}

export interface AdminAttendanceSession {
  sessionId: number;
  subject: string;
  groupId: number;
  group: string;
  department: string;
  professor: string;
  day: string;
  startTime: string;
  endTime: string;
  room: string;
  type: string;
  expectedCount: number;
  recordedCount: number;
  presentCount: number;
  lateCount: number;
  absentCount: number;
  missingCount: number;
  absenceRate: string;
  status: string;
  closedAt: string;
  lastEntryAt: string;
  collectiveAbsenceStatus: string;
  collectiveAbsenceReportedAt: string;
  collectiveAbsenceConfirmedAt: string;
}

export interface AdminAttendanceStudent {
  presenceId: number | null;
  studentId: number;
  studentName: string;
  matricule: string;
  status: string;
  date: string;
}

export interface AdminAttendanceDetail {
  session: AdminAttendanceSession;
  students: AdminAttendanceStudent[];
}

export interface AdminEliminationRecord {
  id: number;
  studentId: number;
  studentName: string;
  matricule: string;
  group: string;
  subject: string;
  typeSeance: string;
  absenceCount: number;
  status: string;
  detectedAt: string;
  notifiedAt: string;
}

@Injectable({
  providedIn: 'root',
})
export class AdminDashboardApi {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/admin';

  getDashboard(): Observable<AdminDashboardResponse> {
    return this.http.get<AdminDashboardResponse>(`${this.apiUrl}/dashboard`);
  }

  getAcademicYears(): Observable<AdminAcademicYear[]> {
    return this.http.get<AdminAcademicYear[]>(`${this.apiUrl}/academic-years`);
  }

  createAcademicYear(request: AdminAcademicYearRequest): Observable<AdminAcademicYear> {
    return this.http.post<AdminAcademicYear>(`${this.apiUrl}/academic-years`, request);
  }

  activateAcademicYear(id: number): Observable<AdminAcademicYear> {
    return this.http.patch<AdminAcademicYear>(`${this.apiUrl}/academic-years/${id}/activate`, {});
  }

  createSemester(academicYearId: number, request: AdminSemesterRequest): Observable<AdminAcademicYear> {
    return this.http.post<AdminAcademicYear>(
      `${this.apiUrl}/academic-years/${academicYearId}/semesters`,
      request
    );
  }

  activateSemester(id: number): Observable<AdminAcademicYear> {
    return this.http.patch<AdminAcademicYear>(
      `${this.apiUrl}/academic-years/semesters/${id}/activate`,
      {}
    );
  }

  setSemesterLocked(id: number, locked: boolean): Observable<AdminAcademicYear> {
    return this.http.patch<AdminAcademicYear>(
      `${this.apiUrl}/academic-years/semesters/${id}/lock?locked=${locked}`,
      {}
    );
  }

  getExamPlanningOptions(): Observable<AdminExamPlanningOptions> {
    return this.http.get<AdminExamPlanningOptions>(`${this.apiUrl}/exam-planning/options`);
  }

  getPlannedExams(): Observable<AdminPlannedExam[]> {
    return this.http.get<AdminPlannedExam[]>(`${this.apiUrl}/exam-planning/exams`);
  }

  createPlannedExam(request: AdminExamPlanningRequest): Observable<AdminPlannedExam> {
    return this.http.post<AdminPlannedExam>(`${this.apiUrl}/exam-planning/exams`, request);
  }

  createPlannedExams(request: AdminExamPlanningBulkRequest): Observable<AdminPlannedExam[]> {
    return this.http.post<AdminPlannedExam[]>(`${this.apiUrl}/exam-planning/exams/bulk`, request);
  }

  publishPlannedExam(evaluationId: number): Observable<AdminPlannedExam> {
    return this.http.patch<AdminPlannedExam>(
      `${this.apiUrl}/exam-planning/exams/${evaluationId}/publish`,
      {}
    );
  }

  publishExamWeek(weekStart: string): Observable<AdminPlannedExam[]> {
    return this.http.patch<AdminPlannedExam[]>(
      `${this.apiUrl}/exam-planning/weeks/publish`,
      { weekStart }
    );
  }

  getNoteValidationEvaluations(): Observable<AdminNoteValidationEvaluation[]> {
    return this.http.get<AdminNoteValidationEvaluation[]>(`${this.apiUrl}/note-validation/evaluations`);
  }

  getNoteValidationDetail(evaluationId: number): Observable<AdminNoteValidationDetail> {
    return this.http.get<AdminNoteValidationDetail>(
      `${this.apiUrl}/note-validation/evaluations/${evaluationId}`
    );
  }

  validateNotes(evaluationId: number, remark: string): Observable<AdminNoteValidationDetail> {
    return this.http.patch<AdminNoteValidationDetail>(
      `${this.apiUrl}/note-validation/evaluations/${evaluationId}/validate`,
      { remark }
    );
  }

  rejectNotes(evaluationId: number, remark: string): Observable<AdminNoteValidationDetail> {
    return this.http.patch<AdminNoteValidationDetail>(
      `${this.apiUrl}/note-validation/evaluations/${evaluationId}/reject`,
      { remark }
    );
  }

  publishNotes(evaluationId: number, remark: string): Observable<AdminNoteValidationDetail> {
    return this.http.patch<AdminNoteValidationDetail>(
      `${this.apiUrl}/note-validation/evaluations/${evaluationId}/publish`,
      { remark }
    );
  }

  getAttendanceSessions(): Observable<AdminAttendanceSession[]> {
    return this.http.get<AdminAttendanceSession[]>(`${this.apiUrl}/attendance-supervision/sessions`);
  }

  getAttendanceDetail(sessionId: number): Observable<AdminAttendanceDetail> {
    return this.http.get<AdminAttendanceDetail>(
      `${this.apiUrl}/attendance-supervision/sessions/${sessionId}`
    );
  }

  closeAttendanceSession(sessionId: number): Observable<AdminAttendanceDetail> {
    return this.http.patch<AdminAttendanceDetail>(
      `${this.apiUrl}/attendance-supervision/sessions/${sessionId}/close`,
      {}
    );
  }

  reopenAttendanceSession(sessionId: number): Observable<AdminAttendanceDetail> {
    return this.http.patch<AdminAttendanceDetail>(
      `${this.apiUrl}/attendance-supervision/sessions/${sessionId}/reopen`,
      {}
    );
  }

  markCollectiveAbsence(sessionId: number): Observable<AdminAttendanceDetail> {
    return this.http.patch<AdminAttendanceDetail>(
      `${this.apiUrl}/attendance-supervision/sessions/${sessionId}/collective-absence`,
      {}
    );
  }

  getEliminations(): Observable<AdminEliminationRecord[]> {
    return this.http.get<AdminEliminationRecord[]>(
      `${this.apiUrl}/attendance-supervision/eliminations`
    );
  }

  notifyEliminatedStudent(id: number): Observable<AdminEliminationRecord> {
    return this.http.patch<AdminEliminationRecord>(
      `${this.apiUrl}/attendance-supervision/eliminations/${id}/notify`,
      {}
    );
  }
}
