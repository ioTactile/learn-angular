import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { MatToolbarModule } from '@angular/material/toolbar';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
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
    MatTableModule,
    MatPaginatorModule,
    MatProgressSpinnerModule,
  ],
  template: `
    <mat-toolbar color="primary" class="toolbar">
      <span class="brand">Habit Tracker</span>
      <span class="spacer"></span>
      <a mat-button routerLink="/workspaces">Workspaces</a>
      @if (auth.isAdmin()) {
        <a mat-button routerLink="/admin">Admin</a>
      }
      <button mat-button type="button" (click)="logout()">Déconnexion</button>
    </mat-toolbar>

    <section class="page">
      <h1>Habitudes</h1>
      <p class="hint">
        Path : <code>/workspaces/:workspaceId</code> — query :
        <code>?q=&amp;page=&amp;size=</code>
      </p>

      <form class="create" [formGroup]="form" (ngSubmit)="create()">
        <mat-form-field appearance="outline" class="title-field">
          <mat-label>Nouvelle habitude</mat-label>
          <input matInput formControlName="title" placeholder="Nouvelle habitude…" maxlength="120" />
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
            <td mat-cell *matCellDef="let habit">
              <a [routerLink]="['/workspaces', workspaceId, 'habits', habit.id]">{{ habit.title }}</a>
            </td>
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
    .toolbar { position: sticky; top: 0; z-index: 2; }
    .brand { font-weight: 500; }
    .spacer { flex: 1; }
    .page { max-width: 52rem; margin: 1.5rem auto; padding: 0 1rem 3rem; }
    h1 { margin: 0 0 0.35rem; font: var(--mat-sys-headline-small); }
    .hint { color: var(--mat-sys-on-surface-variant); margin: 0 0 1rem; font-size: 0.9rem; }
    .create { display: flex; gap: 0.75rem; align-items: flex-start; margin-bottom: 0.5rem; }
    .title-field, .filter-field { flex: 1; width: 100%; }
    .habits-table { width: 100%; background: var(--mat-sys-surface); }
    .loading { display: grid; justify-items: center; gap: 0.75rem; padding: 2rem 0; }
    .empty { color: var(--mat-sys-on-surface-variant); }
    td button { margin-right: 0.35rem; }
    a { color: var(--mat-sys-primary); }
  `,
})
export class HabitsPage implements OnInit {
  readonly store = inject(HabitStore);
  readonly auth = inject(AuthService);
  private readonly confirm = inject(ConfirmDialogService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);
  private readonly destroyRef = inject(DestroyRef);

  readonly displayedColumns = ['title', 'streak', 'actions'];
  workspaceId = '';

  readonly form = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(120)]],
  });

  readonly filterControl = new FormControl('', { nonNullable: true });

  ngOnInit(): void {
    this.workspaceId = this.route.snapshot.paramMap.get('workspaceId') ?? '';
    const q = this.route.snapshot.queryParamMap.get('q') ?? '';
    const page = Number(this.route.snapshot.queryParamMap.get('page') ?? 0);
    const size = Number(this.route.snapshot.queryParamMap.get('size') ?? 10);

    this.filterControl.setValue(q, { emitEvent: false });
    this.store.initFromRoute(this.workspaceId, q, Number.isFinite(page) ? page : 0, size);

    this.filterControl.valueChanges
      .pipe(debounceTime(250), distinctUntilChanged(), takeUntilDestroyed(this.destroyRef))
      .subscribe((value) => {
        this.store.setFilter(value);
        this.syncQueryParams();
      });
  }

  onPage(event: PageEvent): void {
    this.store.setPage(event.pageIndex, event.pageSize);
    this.syncQueryParams();
  }

  create(): void {
    if (this.form.invalid) {
      return;
    }
    const title = this.form.controls.title.value.trim();
    this.store.create(title, () => this.form.reset());
    this.syncQueryParams();
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
    this.auth.logout().subscribe(() => void this.router.navigateByUrl('/login'));
  }

  /** Écrit les search params dans l’URL (partageable / F5). */
  private syncQueryParams(): void {
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {
        q: this.store.filter() || null,
        page: this.store.pageIndex() || null,
        size: this.store.pageSize() === 10 ? null : this.store.pageSize(),
      },
      queryParamsHandling: 'merge',
      replaceUrl: true,
    });
  }
}
