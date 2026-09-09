import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { Habit } from './habit.models';
import { HabitService } from './habit.service';

@Component({
  selector: 'app-habits-page',
  imports: [ReactiveFormsModule],
  template: `
    <section class="page">
      <header class="top">
        <div>
          <p class="brand">Habit Tracker</p>
          <h1>Mes habitudes</h1>
        </div>
        <button type="button" class="ghost" (click)="logout()">Déconnexion</button>
      </header>

      <form class="create" [formGroup]="form" (ngSubmit)="create()">
        <input
          type="text"
          formControlName="title"
          placeholder="Nouvelle habitude…"
          maxlength="120"
        />
        <button type="submit" [disabled]="form.invalid || creating()">Ajouter</button>
      </form>

      @if (error()) {
        <p class="error" role="alert">{{ error() }}</p>
      }

      @if (loading()) {
        <p class="muted">Chargement…</p>
      } @else if (habits().length === 0) {
        <p class="muted">Aucune habitude pour l’instant. Ajoutes-en une.</p>
      } @else {
        <ul class="list">
          @for (habit of habits(); track habit.id) {
            <li>
              <div class="info">
                <strong>{{ habit.title }}</strong>
                <span class="streak">streak {{ habit.streak }}</span>
              </div>
              <div class="actions">
                <button type="button" (click)="complete(habit)">Compléter</button>
                <button type="button" class="danger" (click)="remove(habit)">Supprimer</button>
              </div>
            </li>
          }
        </ul>
      }
    </section>
  `,
  styles: `
    .page {
      max-width: 40rem;
      margin: 2.5rem auto;
      padding: 0 1rem 3rem;
    }
    .top {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      gap: 1rem;
      margin-bottom: 1.5rem;
    }
    .brand {
      margin: 0;
      font-size: 0.8rem;
      letter-spacing: 0.04em;
      text-transform: uppercase;
      color: var(--muted);
    }
    h1 {
      margin: 0.2rem 0 0;
      font-size: 1.85rem;
    }
    .create {
      display: flex;
      gap: 0.5rem;
      margin-bottom: 1.25rem;
    }
    .create input {
      flex: 1;
      padding: 0.7rem 0.8rem;
      border: 1px solid var(--border);
      border-radius: 0.4rem;
      background: var(--surface);
      color: inherit;
      font: inherit;
    }
    button {
      padding: 0.65rem 0.9rem;
      border: 0;
      border-radius: 0.4rem;
      background: var(--accent);
      color: #fff;
      font: inherit;
      cursor: pointer;
    }
    button:disabled {
      opacity: 0.55;
      cursor: not-allowed;
    }
    .ghost {
      background: transparent;
      color: var(--muted);
      border: 1px solid var(--border);
    }
    .danger {
      background: transparent;
      color: var(--danger);
      border: 1px solid color-mix(in srgb, var(--danger) 35%, transparent);
    }
    .list {
      list-style: none;
      margin: 0;
      padding: 0;
      display: grid;
      gap: 0.75rem;
    }
    li {
      display: flex;
      justify-content: space-between;
      gap: 1rem;
      align-items: center;
      padding: 0.9rem 1rem;
      border: 1px solid var(--border);
      border-radius: 0.5rem;
      background: var(--surface);
    }
    .info {
      display: grid;
      gap: 0.2rem;
    }
    .streak {
      font-size: 0.85rem;
      color: var(--muted);
    }
    .actions {
      display: flex;
      gap: 0.4rem;
      flex-shrink: 0;
    }
    .muted {
      color: var(--muted);
    }
    .error {
      color: var(--danger);
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
        this.error.set("Création impossible.");
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
