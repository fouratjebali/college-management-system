import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

const routes: Routes = [
  {
    path: '',
    title: 'Admin Dashboard',
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
