import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { UserRole } from '../../core/models/auth.model';
import { roleGuard } from '../../core/guards/role-guard';

const routes: Routes = [
  {
    path: '',
    title: 'Student Dashboard',
    canActivate: [roleGuard],
    data: { roles: [UserRole.STUDENT] },
    loadComponent: () =>
      import('./pages/student-dashboard/student-dashboard').then(
        (module) => module.StudentDashboardComponent
      ),
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class StudentRoutingModule { }
