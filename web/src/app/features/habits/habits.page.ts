import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatToolbarModule } from '@angular/material/toolbar';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
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
    MatListModule,
    MatCardModule,
    MatChipsModule,
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

      @if (error()) {
        <p class="error" role="alert">{{ error() }}</p>
      }

      @if (loading()) {
        <div class="loading">
          <mat-spinner diameter="36"></mat-spinner>
          <p>Chargement…</p>
        </div>
      } @else if (habits().length === 0) {
        <mat-card>
          <mat-card-content>
            <p class="empty">Aucune habitude pour l’instant. Ajoutes-en une.</p>
          </mat-card-content>
        </mat-card>
      } @else {
        <mat-card>
          <mat-list>
            @for (habit of habits(); track habit.id) {
              <mat-list-item>
                <span matListItemTitle>{{ habit.title }}</span>
                <span matListItemLine>
                  <mat-chip-set>
                    <mat-chip>streak {{ habit.streak }}</mat-chip>
                  </mat-chip-set>
                </span>
                <div matListItemMeta class="actions">
                  <button mat-stroked-button type="button" (click)="complete(habit)">
                    Compléter
                  </button>
                  <button mat-button color="warn" type="button" (click)="remove(habit)">
                    Supprimer
                  </button>
                </div>
              </mat-list-item>
            }
          </mat-list>
        </mat-card>
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
      letter-spacing: 0.02em;
    }

    .spacer {
      flex: 1;
    }

    .page {
      max-width: 44rem;
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
      margin-bottom: 1rem;
    }

    .title-field {
      flex: 1;
    }

    .actions {
      display: flex;
      gap: 0.4rem;
      align-items: center;
    }

    .loading {
      display: grid;
      justify-items: center;
      gap: 0.75rem;
      padding: 2rem 0;
      color: var(--mat-sys-on-surface-variant);
    }

    .empty {
      margin: 0;
      color: var(--mat-sys-on-surface-variant);
    }

    .error {
      color: var(--mat-sys-error);
    }

    mat-list-item {
      height: auto !important;
      min-height: 4.5rem;
    }
  `,
})
export class HabitsPage implements OnInit {
  private readonly habitsApi = inject(HabitService);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);

  readonly habits = signal<Habit[]>([]);
  readonly loading = signal(true);
  readonly creating = signal(false);
  readonly error = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(120)]],
  });

  ngOnInit(): void {
    this.reload();
  }

  create(): void {
    if (this.form.invalid) {
      return;
    }

    this.creating.set(true);
    this.error.set(null);
    const title = this.form.controls.title.value.trim();

    this.habitsApi.create({ title }).subscribe({
      next: (habit) => {
        this.habits.update((list) => [habit, ...list]);
        this.form.reset();
        this.creating.set(false);
      },
      error: () => {
        this.creating.set(false);
        this.error.set('Création impossible.');
      },
    });
  }

  complete(habit: Habit): void {
    this.habitsApi.complete(habit.id).subscribe({
      next: (updated) => {
        this.habits.update((list) => list.map((h) => (h.id === updated.id ? updated : h)));
      },
      error: () => this.error.set('Completion impossible.'),
    });
  }

  remove(habit: Habit): void {
    this.habitsApi.delete(habit.id).subscribe({
      next: () => {
        this.habits.update((list) => list.filter((h) => h.id !== habit.id));
      },
      error: () => this.error.set('Suppression impossible.'),
    });
  }

  logout(): void {
    this.auth.logout();
    void this.router.navigateByUrl('/login');
  }

  private reload(): void {
    this.loading.set(true);
    this.habitsApi.list().subscribe({
      next: (habits) => {
        this.habits.set(habits);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.error.set('Impossible de charger les habitudes.');
      },
    });
  }
}
