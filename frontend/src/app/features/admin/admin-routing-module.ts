import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { UserRole } from '../../core/models/auth.model';
import { roleGuard } from '../../core/guards/role-guard';

const routes: Routes = [
  {
    path: 'login',
    title: 'Admin Login',
    loadComponent: () =>
      import('./pages/admin-login/admin-login').then(
        (module) => module.AdminLoginComponent
      ),
  },
  {
    path: '',
    title: 'Admin Dashboard',
    canActivate: [roleGuard],
    data: { roles: [UserRole.ADMIN] },
    loadComponent: () =>
      import('./pages/admin-dashboard/admin-dashboard').then(
        (module) => module.AdminDashboardComponent
      ),
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AdminRoutingModule { }
