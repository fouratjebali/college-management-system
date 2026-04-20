import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

export interface StudentProfile {
  id: number;
  name: string;
  email: string;
  matricule: string;
  level: string;
  group: string;
  department: string;
  year: string;
}

export interface StudentStat {
  label: string;
  value: string;
  trend: string;
  tone: 'light' | 'warm' | 'steel' | 'sand';
}

export interface StudentGradeSummary {
  average: string;
  complete: boolean;
  expectedCount: number;
  receivedCount: number;
  message: string;
}

export interface StudentEvaluationGrade {
  type: string;
  label: string;
  value: string;
  coefficient: string;
  date: string;
  status: string;
  remark: string;
  published: boolean;
}

export interface StudentSubjectGrade {
  subject: string;
  professor: string;
  subjectCoefficient: string;
  average: string;
  complete: boolean;
  expectedCount: number;
  receivedCount: number;
  evaluations: StudentEvaluationGrade[];
}

export interface StudentGradeRow {
  id: number;
  subject: string;
  evaluation: string;
  type: string;
  date: string;
  value: string;
  coefficient: string;
  status: string;
  remark: string;
}

export interface StudentScheduleRow {
  id: number;
  subject: string;
  professor: string;
  day: string;
  start: string;
  end: string;
  room: string;
  type: string;
}

export interface StudentMaterialRow {
  id: number;
  title: string;
  subject: string;
  fileName: string;
  fileType: string;
  size: string;
  date: string;
  downloadUrl: string;
}

export interface StudentAnnouncementRow {
  id: number;
  title: string;
  content: string;
  publicationDate: string;
  expirationDate: string;
  author: string;
}

export interface StudentMakeupRow {
  id: number;
  subject: string;
  professor: string;
  day: string;
  start: string;
  end: string;
  room: string;
}

export interface StudentAttendanceRow {
  id: number;
  subject: string;
  session: string;
  status: string;
  date: string;
}

export interface StudentDashboardResponse {
  profile: StudentProfile;
  stats: StudentStat[];
  gradeSummary: StudentGradeSummary;
  subjectGrades: StudentSubjectGrade[];
  grades: StudentGradeRow[];
  schedule: StudentScheduleRow[];
  materials: StudentMaterialRow[];
  announcements: StudentAnnouncementRow[];
  makeups: StudentMakeupRow[];
  attendance: StudentAttendanceRow[];
}

@Injectable({
  providedIn: 'root',
})
export class StudentDashboardApi {
  private readonly http = inject(HttpClient);

  getDashboard(): Observable<StudentDashboardResponse> {
    return this.http.get<StudentDashboardResponse>('/api/student/dashboard');
  }

  downloadMaterial(url: string): Observable<Blob> {
    return this.http.get(url, { responseType: 'blob' });
  }
}
