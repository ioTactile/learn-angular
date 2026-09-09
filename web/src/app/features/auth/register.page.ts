import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-register-page',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
  ],
  template: `
    <section class="auth">
      <mat-card>
        <mat-card-header>
          <mat-card-title>Créer un compte</mat-card-title>
          <mat-card-subtitle>Inscription Habit Tracker.</mat-card-subtitle>
        </mat-card-header>

        <mat-card-content>
          <form [formGroup]="form" (ngSubmit)="submit()" class="form">
            <mat-form-field appearance="outline">
              <mat-label>Email</mat-label>
              <input matInput type="email" formControlName="email" autocomplete="email" />
            </mat-form-field>

            <mat-form-field appearance="outline">
              <mat-label>Mot de passe</mat-label>
              <input
                matInput
                type="password"
                formControlName="password"
                autocomplete="new-password"
              />
            </mat-form-field>

            @if (error()) {
              <p class="error" role="alert">{{ error() }}</p>
            }

            <button
              mat-flat-button
              color="primary"
              type="submit"
              [disabled]="form.invalid || loading()"
            >
              {{ loading() ? 'Création…' : "S'inscrire" }}
            </button>
          </form>
        </mat-card-content>

        <mat-card-actions align="end">
          <a mat-button routerLink="/login">Se connecter</a>
        </mat-card-actions>
      </mat-card>
    </section>
  `,
  styles: `
    .auth {
      max-width: 26rem;
      margin: 4rem auto;
      padding: 0 1rem;
    }

    .form {
      display: grid;
      gap: 0.25rem;
      margin-top: 0.5rem;
    }

    mat-form-field {
      width: 100%;
    }

    .error {
      margin: 0;
      color: var(--mat-sys-error);
      font-size: 0.9rem;
    }
  `,
})
export class RegisterPage {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
  });

  submit(): void {
    if (this.form.invalid) {
      return;
    }

    this.loading.set(true);
    this.error.set(null);

    this.auth.register(this.form.getRawValue()).subscribe({
      next: () => {
        this.loading.set(false);
        void this.router.navigateByUrl('/habits');
      },
      error: (err: { status?: number }) => {
        this.loading.set(false);
        this.error.set(
          err.status === 409
            ? 'Cet email est déjà utilisé.'
            : "Impossible de créer le compte.",
        );
      },
    });
  }
}
