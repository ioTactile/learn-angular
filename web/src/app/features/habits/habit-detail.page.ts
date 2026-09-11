import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatToolbarModule } from '@angular/material/toolbar';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { ToastService } from '../../core/ui/toast.service';
import { Habit, HabitCompletion } from './habit.models';
import { HabitService } from './habit.service';

@Component({
  selector: 'app-habit-detail-page',
  imports: [
    DatePipe,
    ReactiveFormsModule,
    RouterLink,
    MatToolbarModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
  ],
  template: `
    <mat-toolbar color="primary" class="toolbar">
      <span class="brand">Journal</span>
      <span class="spacer"></span>
      <a mat-button [routerLink]="['/workspaces', workspaceId]">Retour liste</a>
      <button mat-button type="button" (click)="logout()">Déconnexion</button>
    </mat-toolbar>

    <section class="page">
      @if (loading()) {
        <div class="loading"><mat-spinner diameter="36"></mat-spinner></div>
      } @else if (habit(); as h) {
        <h1>{{ h.title }}</h1>
        <p class="meta">streak {{ h.streak }} · path <code>/habits/:habitId</code></p>

        <form class="complete" [formGroup]="completeForm" (ngSubmit)="complete()">
          <mat-form-field appearance="outline" class="grow">
            <mat-label>Note du jour (optionnel)</mat-label>
            <input matInput formControlName="note" maxlength="500" />
          </mat-form-field>
          <button mat-flat-button color="primary" type="submit">Compléter</button>
        </form>

        <form class="range" [formGroup]="rangeForm" (ngSubmit)="applyRange()">
          <mat-form-field appearance="outline">
            <mat-label>From</mat-label>
            <input matInput type="date" formControlName="from" />
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>To</mat-label>
            <input matInput type="date" formControlName="to" />
          </mat-form-field>
          <button mat-stroked-button type="submit">Filtrer journal</button>
        </form>
        <p class="hint">Search params : <code>?from=&amp;to=</code></p>

        <ul class="journal">
          @for (entry of completions(); track entry.id) {
            <li>
              <strong>{{ entry.completedOn | date: 'dd/MM/yyyy' }}</strong>
              @if (entry.note) {
                — {{ entry.note }}
              }
            </li>
          } @empty {
            <li class="empty">Aucune completion sur cette période.</li>
          }
        </ul>
      }
    </section>
  `,
  styles: `
    .toolbar { position: sticky; top: 0; z-index: 2; }
    .brand { font-weight: 500; }
    .spacer { flex: 1; }
    .page { max-width: 40rem; margin: 1.5rem auto; padding: 0 1rem 3rem; }
    h1 { margin: 0 0 0.35rem; font: var(--mat-sys-headline-small); }
    .meta, .hint, .empty { color: var(--mat-sys-on-surface-variant); }
    .complete, .range { display: flex; gap: 0.75rem; flex-wrap: wrap; align-items: flex-start; margin: 1rem 0; }
    .grow { flex: 1; min-width: 12rem; }
    .journal { list-style: none; padding: 0; margin: 1rem 0 0; display: grid; gap: 0.5rem; }
    .journal li { padding: 0.65rem 0.75rem; background: var(--mat-sys-surface-container-low); border-radius: 0.5rem; }
    .loading { display: grid; justify-items: center; padding: 2rem 0; }
  `,
})
export class HabitDetailPage implements OnInit {
  private readonly api = inject(HabitService);
  private readonly auth = inject(AuthService);
  private readonly toast = inject(ToastService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);

  workspaceId = '';
  habitId = '';

  readonly habit = signal<Habit | null>(null);
  readonly completions = signal<HabitCompletion[]>([]);
  readonly loading = signal(true);

  readonly completeForm = this.fb.nonNullable.group({ note: [''] });
  readonly rangeForm = this.fb.nonNullable.group({
    from: [''],
    to: [''],
  });

  ngOnInit(): void {
    this.workspaceId = this.route.snapshot.paramMap.get('workspaceId') ?? '';
    this.habitId = this.route.snapshot.paramMap.get('habitId') ?? '';
    const from = this.route.snapshot.queryParamMap.get('from') ?? '';
    const to = this.route.snapshot.queryParamMap.get('to') ?? '';
    this.rangeForm.patchValue({ from, to });
    this.reload();
  }

  complete(): void {
    const note = this.completeForm.controls.note.value.trim();
    this.api.complete(this.habitId, note || null).subscribe({
      next: (updated) => {
        this.habit.set(updated);
        this.completeForm.reset();
        this.toast.success(`Complétée — streak ${updated.streak}`);
        this.reloadCompletions();
      },
      error: () => this.toast.error('Completion impossible.'),
    });
  }

  applyRange(): void {
    const { from, to } = this.rangeForm.getRawValue();
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { from: from || null, to: to || null },
      queryParamsHandling: 'merge',
      replaceUrl: true,
    });
    this.reloadCompletions();
  }

  logout(): void {
    this.auth.logout();
    void this.router.navigateByUrl('/login');
  }

  private reload(): void {
    this.loading.set(true);
    this.api.get(this.habitId).subscribe({
      next: (habit) => {
        this.habit.set(habit);
        this.loading.set(false);
        this.reloadCompletions();
      },
      error: () => {
        this.loading.set(false);
        this.toast.error('Habitude introuvable.');
        void this.router.navigate(['/workspaces', this.workspaceId]);
      },
    });
  }

  private reloadCompletions(): void {
    const { from, to } = this.rangeForm.getRawValue();
    this.api.listCompletions(this.habitId, { from: from || undefined, to: to || undefined }).subscribe({
      next: (entries) => this.completions.set(entries),
      error: () => this.toast.error('Journal indisponible.'),
    });
  }
}
