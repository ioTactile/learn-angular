import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { MatToolbarModule } from '@angular/material/toolbar';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { ToastService } from '../../core/ui/toast.service';
import { AdminService, AdminUser } from './admin.service';

@Component({
  selector: 'app-admin-page',
  imports: [
    DatePipe,
    RouterLink,
    MatToolbarModule,
    MatButtonModule,
    MatTableModule,
    MatProgressSpinnerModule,
  ],
  template: `
    <mat-toolbar color="primary" class="toolbar">
      <span class="brand">Admin</span>
      <span class="spacer"></span>
      <a mat-button routerLink="/workspaces">Workspaces</a>
      <button mat-button type="button" (click)="logout()">Déconnexion</button>
    </mat-toolbar>

    <section class="page">
      <h1>Utilisateurs</h1>

      @if (loading()) {
        <div class="loading">
          <mat-spinner diameter="36"></mat-spinner>
        </div>
      } @else {
        <table mat-table [dataSource]="users()" class="users-table">
          <ng-container matColumnDef="email">
            <th mat-header-cell *matHeaderCellDef>Email</th>
            <td mat-cell *matCellDef="let user">{{ user.email }}</td>
          </ng-container>

          <ng-container matColumnDef="role">
            <th mat-header-cell *matHeaderCellDef>Rôle</th>
            <td mat-cell *matCellDef="let user">{{ user.role }}</td>
          </ng-container>

          <ng-container matColumnDef="createdAt">
            <th mat-header-cell *matHeaderCellDef>Créé</th>
            <td mat-cell *matCellDef="let user">
              {{ user.createdAt | date: 'dd/MM/yyyy HH:mm' }}
            </td>
          </ng-container>

          <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
          <tr mat-row *matRowDef="let row; columns: displayedColumns"></tr>
        </table>
      }
    </section>
  `,
  styles: `
    .toolbar {
      position: sticky;
      top: 0;
      z-index: 2;
    }

    .brand {
      font-weight: 500;
    }

    .spacer {
      flex: 1;
    }

    .page {
      max-width: 52rem;
      margin: 1.5rem auto;
      padding: 0 1rem 3rem;
    }

    h1 {
      margin: 0 0 1rem;
      font: var(--mat-sys-headline-small);
    }

    .users-table {
      width: 100%;
      background: var(--mat-sys-surface);
    }

    .loading {
      display: grid;
      justify-items: center;
      padding: 2rem 0;
    }
  `,
})
export class AdminPage implements OnInit {
  private readonly adminApi = inject(AdminService);
  private readonly auth = inject(AuthService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);

  readonly displayedColumns = ['email', 'role', 'createdAt'];
  readonly users = signal<AdminUser[]>([]);
  readonly loading = signal(true);

  ngOnInit(): void {
    this.adminApi.listUsers().subscribe({
      next: (users) => {
        this.users.set(users);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.toast.error('Accès admin refusé ou API indisponible.');
      },
    });
  }

  logout(): void {
    this.auth.logout().subscribe(() => void this.router.navigateByUrl('/login'));
  }
}
