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

@Injectable({
  providedIn: 'root',
})
export class AdminDashboardApi {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/admin';

  getDashboard(): Observable<AdminDashboardResponse> {
    return this.http.get<AdminDashboardResponse>(`${this.apiUrl}/dashboard`);
  }
}
