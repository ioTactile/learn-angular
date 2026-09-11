import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { MatToolbarModule } from '@angular/material/toolbar';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { ToastService } from '../../core/ui/toast.service';
import { Workspace } from './workspace.models';
import { WorkspaceService } from './workspace.service';

@Component({
  selector: 'app-workspaces-page',
  imports: [
    DatePipe,
    ReactiveFormsModule,
    RouterLink,
    MatToolbarModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatTableModule,
    MatProgressSpinnerModule,
  ],
  template: `
    <mat-toolbar color="primary" class="toolbar">
      <span class="brand">Workspaces</span>
      <span class="spacer"></span>
      @if (auth.isAdmin()) {
        <a mat-button routerLink="/admin">Admin</a>
      }
      <button mat-button type="button" (click)="logout()">Déconnexion</button>
    </mat-toolbar>

    <section class="page">
      <h1>Mes espaces</h1>
      <p class="hint">Un workspace regroupe des habitudes. L’URL /workspaces/:id porte le path param.</p>

      <form class="create" [formGroup]="form" (ngSubmit)="create()">
        <mat-form-field appearance="outline" class="grow">
          <mat-label>Nouvel espace</mat-label>
          <input matInput formControlName="name" maxlength="120" />
        </mat-form-field>
        <button mat-flat-button color="primary" type="submit" [disabled]="form.invalid || creating()">
          Créer
        </button>
      </form>

      @if (loading()) {
        <div class="loading"><mat-spinner diameter="36"></mat-spinner></div>
      } @else {
        <table mat-table [dataSource]="workspaces()" class="table">
          <ng-container matColumnDef="name">
            <th mat-header-cell *matHeaderCellDef>Nom</th>
            <td mat-cell *matCellDef="let ws">
              <a [routerLink]="['/workspaces', ws.id]">{{ ws.name }}</a>
            </td>
          </ng-container>
          <ng-container matColumnDef="createdAt">
            <th mat-header-cell *matHeaderCellDef>Créé</th>
            <td mat-cell *matCellDef="let ws">{{ ws.createdAt | date: 'dd/MM/yyyy HH:mm' }}</td>
          </ng-container>
          <tr mat-header-row *matHeaderRowDef="columns"></tr>
          <tr mat-row *matRowDef="let row; columns: columns"></tr>
        </table>
      }
    </section>
  `,
  styles: `
    .toolbar { position: sticky; top: 0; z-index: 2; }
    .brand { font-weight: 500; }
    .spacer { flex: 1; }
    .page { max-width: 40rem; margin: 1.5rem auto; padding: 0 1rem 3rem; }
    h1 { margin: 0 0 0.35rem; font: var(--mat-sys-headline-small); }
    .hint { color: var(--mat-sys-on-surface-variant); margin: 0 0 1rem; }
    .create { display: flex; gap: 0.75rem; align-items: flex-start; margin-bottom: 1rem; }
    .grow { flex: 1; }
    .table { width: 100%; background: var(--mat-sys-surface); }
    .loading { display: grid; justify-items: center; padding: 2rem 0; }
    a { color: var(--mat-sys-primary); }
  `,
})
export class WorkspacesPage implements OnInit {
  private readonly api = inject(WorkspaceService);
  readonly auth = inject(AuthService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);

  readonly columns = ['name', 'createdAt'];
  readonly workspaces = signal<Workspace[]>([]);
  readonly loading = signal(true);
  readonly creating = signal(false);

  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(120)]],
  });

  ngOnInit(): void {
    this.reload();
  }

  create(): void {
    if (this.form.invalid) {
      return;
    }
    this.creating.set(true);
    this.api.create({ name: this.form.controls.name.value.trim() }).subscribe({
      next: (ws) => {
        this.creating.set(false);
        this.form.reset();
        this.toast.success('Espace créé');
        void this.router.navigate(['/workspaces', ws.id]);
      },
      error: () => {
        this.creating.set(false);
        this.toast.error('Création impossible.');
      },
    });
  }

  logout(): void {
    this.auth.logout();
    void this.router.navigateByUrl('/login');
  }

  private reload(): void {
    this.loading.set(true);
    this.api.list().subscribe({
      next: (list) => {
        this.workspaces.set(list);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.toast.error('Impossible de charger les workspaces.');
      },
    });
  }
}
