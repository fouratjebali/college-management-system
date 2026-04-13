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
    path: '**',
    redirectTo: '',
  },
];
