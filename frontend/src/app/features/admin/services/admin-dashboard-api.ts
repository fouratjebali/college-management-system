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

export interface AdminPlannedExam {
  evaluationId: number;
  seanceId: number;
  subject: string;
  subjectCode: string;
  group: string;
  professor: string;
  date: string;
  day: string;
  startTime: string;
  endTime: string;
  room: string;
  type: string;
  scope: string;
  status: string;
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
}
