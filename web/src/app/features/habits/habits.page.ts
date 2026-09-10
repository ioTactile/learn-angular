import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
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
import { Router } from '@angular/router';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { ToastService } from '../../core/ui/toast.service';
import { Habit } from './habit.models';
import { HabitService } from './habit.service';

@Component({
  selector: 'app-habits-page',
  imports: [
    ReactiveFormsModule,
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
          [disabled]="form.invalid || creating()"
        >
          Ajouter
        </button>
      </form>

      <mat-form-field appearance="outline" class="filter-field">
        <mat-label>Filtrer</mat-label>
        <input matInput [formControl]="filterControl" placeholder="Rechercher…" />
      </mat-form-field>

      @if (loading()) {
        <div class="loading">
          <mat-spinner diameter="36"></mat-spinner>
          <p>Chargement…</p>
        </div>
      } @else if (habits().length === 0) {
        <p class="empty">Aucune habitude pour l’instant. Ajoutes-en une.</p>
      } @else {
        <table mat-table [dataSource]="habits()" class="habits-table">
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
          [length]="totalElements()"
          [pageIndex]="pageIndex()"
          [pageSize]="pageSize()"
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
  private readonly habitsApi = inject(HabitService);
  private readonly auth = inject(AuthService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);
  private readonly destroyRef = inject(DestroyRef);

  readonly displayedColumns = ['title', 'streak', 'actions'];
  readonly habits = signal<Habit[]>([]);
  readonly loading = signal(true);
  readonly creating = signal(false);
  readonly filter = signal('');
  readonly pageIndex = signal(0);
  readonly pageSize = signal(10);
  readonly totalElements = signal(0);

  readonly form = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(120)]],
  });

  readonly filterControl = new FormControl('', { nonNullable: true });

  ngOnInit(): void {
    this.filterControl.valueChanges
      .pipe(debounceTime(250), distinctUntilChanged(), takeUntilDestroyed(this.destroyRef))
      .subscribe((value) => {
        this.filter.set(value);
        this.pageIndex.set(0);
        this.reload();
      });

    this.reload();
  }

  onPage(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.reload();
  }

  create(): void {
    if (this.form.invalid) {
      return;
    }

    this.creating.set(true);
    const title = this.form.controls.title.value.trim();

    this.habitsApi.create({ title }).subscribe({
      next: () => {
        this.form.reset();
        this.creating.set(false);
        this.pageIndex.set(0);
        this.toast.success('Habitude ajoutée');
        this.reload();
      },
      error: () => {
        this.creating.set(false);
        this.toast.error('Création impossible.');
      },
    });
  }

  complete(habit: Habit): void {
    this.habitsApi.complete(habit.id).subscribe({
      next: (updated) => {
        this.habits.update((list) => list.map((h) => (h.id === updated.id ? updated : h)));
        this.toast.success(`Complétée — streak ${updated.streak}`);
      },
      error: () => this.toast.error('Completion impossible.'),
    });
  }

  remove(habit: Habit): void {
    this.habitsApi.delete(habit.id).subscribe({
      next: () => {
        this.toast.success('Habitude supprimée');
        this.reload();
      },
      error: () => this.toast.error('Suppression impossible.'),
    });
  }

  logout(): void {
    this.auth.logout();
    void this.router.navigateByUrl('/login');
  }

  private reload(): void {
    this.loading.set(true);
    this.habitsApi
      .list({
        page: this.pageIndex(),
        size: this.pageSize(),
        q: this.filter(),
      })
      .subscribe({
        next: (page) => {
          this.habits.set(page.content);
          this.totalElements.set(page.totalElements);
          this.loading.set(false);
        },
        error: () => {
          this.loading.set(false);
          this.toast.error('Impossible de charger les habitudes.');
        },
      });
  }
}
