import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    title: 'Authentication',
    loadComponent: () =>
      import('./features/auth/pages/auth-page/auth-page').then(
        (module) => module.AuthPageComponent
      ),
  },
  {
    path: 'auth',
    pathMatch: 'full',
    redirectTo: '',
  },
  {
    path: 'admin',
    title: 'Admin Workspace',
    loadChildren: () =>
      import('./features/admin/admin-module').then((module) => module.AdminModule),
  },
  {
    path: 'professor',
    title: 'Professor Workspace',
    loadChildren: () =>
      import('./features/professor/professor-module').then((module) => module.ProfessorModule),
  },
  {
    path: 'student',
    title: 'Student Workspace',
    loadChildren: () =>
      import('./features/student/student-module').then((module) => module.StudentModule),
  },
  {
    path: '**',
    redirectTo: '',
  },
];
