import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { MatToolbarModule } from '@angular/material/toolbar';
import { Router, RouterLink } from '@angular/router';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { ConfirmDialogService } from '../../core/ui/confirm-dialog.service';
import { Habit } from './habit.models';
import { HabitStore } from './habit.store';

@Component({
  selector: 'app-habits-page',
  providers: [HabitStore],
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatToolbarModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatTableModule,
    MatPaginatorModule,
    MatProgressSpinnerModule,
  ],
  template: `
    <mat-toolbar color="primary" class="toolbar">
      <span class="brand">Habit Tracker</span>
      <span class="spacer"></span>
      @if (auth.isAdmin()) {
        <a mat-button routerLink="/admin">Admin</a>
      }
      <button mat-button type="button" (click)="logout()">Déconnexion</button>
    </mat-toolbar>

    <section class="page">
      <h1>Mes habitudes</h1>

      <form class="create" [formGroup]="form" (ngSubmit)="create()">
        <mat-form-field appearance="outline" class="title-field">
          <mat-label>Nouvelle habitude</mat-label>
          <input
            matInput
            formControlName="title"
            placeholder="Nouvelle habitude…"
            maxlength="120"
          />
        </mat-form-field>
        <button
          mat-flat-button
          color="primary"
          type="submit"
          [disabled]="form.invalid || store.creating()"
        >
          Ajouter
        </button>
      </form>

      <mat-form-field appearance="outline" class="filter-field">
        <mat-label>Filtrer</mat-label>
        <input matInput [formControl]="filterControl" placeholder="Rechercher…" />
      </mat-form-field>

      @if (store.loading()) {
        <div class="loading">
          <mat-spinner diameter="36"></mat-spinner>
          <p>Chargement…</p>
        </div>
      } @else if (store.habits().length === 0) {
        <p class="empty">Aucune habitude pour l’instant. Ajoutes-en une.</p>
      } @else {
        <table mat-table [dataSource]="store.habits()" class="habits-table">
          <ng-container matColumnDef="title">
            <th mat-header-cell *matHeaderCellDef>Titre</th>
            <td mat-cell *matCellDef="let habit">{{ habit.title }}</td>
          </ng-container>

          <ng-container matColumnDef="streak">
            <th mat-header-cell *matHeaderCellDef>Streak</th>
            <td mat-cell *matCellDef="let habit">streak {{ habit.streak }}</td>
          </ng-container>

          <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef>Actions</th>
            <td mat-cell *matCellDef="let habit">
              <button mat-stroked-button type="button" (click)="complete(habit)">Compléter</button>
              <button mat-button color="warn" type="button" (click)="remove(habit)">
                Supprimer
              </button>
            </td>
          </ng-container>

          <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
          <tr mat-row *matRowDef="let row; columns: displayedColumns"></tr>
        </table>

        <mat-paginator
          [length]="store.totalElements()"
          [pageIndex]="store.pageIndex()"
          [pageSize]="store.pageSize()"
          [pageSizeOptions]="[5, 10, 20]"
          (page)="onPage($event)"
        />
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

    .create {
      display: flex;
      gap: 0.75rem;
      align-items: flex-start;
      margin-bottom: 0.5rem;
    }

    .title-field,
    .filter-field {
      flex: 1;
      width: 100%;
    }

    .habits-table {
      width: 100%;
      background: var(--mat-sys-surface);
    }

    .loading {
      display: grid;
      justify-items: center;
      gap: 0.75rem;
      padding: 2rem 0;
    }

    .empty {
      color: var(--mat-sys-on-surface-variant);
    }

    td button {
      margin-right: 0.35rem;
    }
  `,
})
export class HabitsPage implements OnInit {
  readonly store = inject(HabitStore);
  readonly auth = inject(AuthService);
  private readonly confirm = inject(ConfirmDialogService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);
  private readonly destroyRef = inject(DestroyRef);

  readonly displayedColumns = ['title', 'streak', 'actions'];

  readonly form = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(120)]],
  });

  readonly filterControl = new FormControl('', { nonNullable: true });

  ngOnInit(): void {
    this.filterControl.valueChanges
      .pipe(debounceTime(250), distinctUntilChanged(), takeUntilDestroyed(this.destroyRef))
      .subscribe((value) => this.store.setFilter(value));

    this.store.load();
  }

  onPage(event: PageEvent): void {
    this.store.setPage(event.pageIndex, event.pageSize);
  }

  create(): void {
    if (this.form.invalid) {
      return;
    }

    const title = this.form.controls.title.value.trim();
    this.store.create(title, () => this.form.reset());
  }

  complete(habit: Habit): void {
    this.store.complete(habit);
  }

  remove(habit: Habit): void {
    this.confirm
      .confirm({
        title: 'Supprimer l’habitude ?',
        message: `« ${habit.title} » sera définitivement supprimée.`,
        confirmLabel: 'Supprimer',
      })
      .subscribe((ok) => {
        if (ok) {
          this.store.remove(habit);
        }
      });
  }

  logout(): void {
    this.auth.logout();
    void this.router.navigateByUrl('/login');
  }
}
