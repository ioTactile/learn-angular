import { Routes } from '@angular/router';
import { adminGuard, authGuard, guestGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'habits' },
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/auth/login.page').then((m) => m.LoginPage),
  },
  {
    path: 'register',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/auth/register.page').then((m) => m.RegisterPage),
  },
  {
    path: 'habits',
    canActivate: [authGuard],
    loadComponent: () => import('./features/habits/habits.page').then((m) => m.HabitsPage),
  },
  {
    path: 'admin',
    canActivate: [authGuard, adminGuard],
    loadComponent: () => import('./features/admin/admin.page').then((m) => m.AdminPage),
  },
  { path: '**', redirectTo: 'habits' },
];
