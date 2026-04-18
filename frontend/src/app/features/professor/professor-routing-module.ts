import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { UserRole } from '../../core/models/auth.model';
import { roleGuard } from '../../core/guards/role-guard';

const routes: Routes = [
  {
    path: '',
    title: 'Professor Dashboard',
    canActivate: [roleGuard],
    data: { roles: [UserRole.PROFESSOR] },
    loadComponent: () =>
      import('./pages/professor-dashboard/professor-dashboard').then(
        (module) => module.ProfessorDashboardComponent
      ),
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ProfessorRoutingModule { }
