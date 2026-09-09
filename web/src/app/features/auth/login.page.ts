import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-login-page',
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <section class="auth">
      <h1>Connexion</h1>
      <p class="lede">Habit Tracker — entre avec ton compte.</p>

      <form [formGroup]="form" (ngSubmit)="submit()">
        <label>
          Email
          <input type="email" formControlName="email" autocomplete="email" />
        </label>
        <label>
          Mot de passe
          <input type="password" formControlName="password" autocomplete="current-password" />
        </label>

        @if (error()) {
          <p class="error" role="alert">{{ error() }}</p>
        }

        <button type="submit" [disabled]="form.invalid || loading()">
          {{ loading() ? 'Connexion…' : 'Se connecter' }}
        </button>
      </form>

      <p class="switch">
        Pas de compte ?
        <a routerLink="/register">Créer un compte</a>
      </p>
    </section>
  `,
  styles: `
    .auth {
      max-width: 24rem;
      margin: 4rem auto;
      padding: 0 1rem;
    }
    h1 {
      margin: 0 0 0.35rem;
      font-size: 1.75rem;
    }
    .lede {
      margin: 0 0 1.5rem;
      color: var(--muted);
    }
    form {
      display: grid;
      gap: 1rem;
    }
    label {
      display: grid;
      gap: 0.35rem;
      font-size: 0.9rem;
    }
    input {
      padding: 0.65rem 0.75rem;
      border: 1px solid var(--border);
      border-radius: 0.4rem;
      background: var(--surface);
      color: inherit;
      font: inherit;
    }
    button {
      margin-top: 0.25rem;
      padding: 0.7rem 1rem;
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
    .error {
      margin: 0;
      color: var(--danger);
      font-size: 0.9rem;
    }
    .switch {
      margin-top: 1.25rem;
      color: var(--muted);
    }
    a {
      color: var(--accent);
    }
  `,
})
export class LoginPage {
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

    this.auth.login(this.form.getRawValue()).subscribe({
      next: () => {
        this.loading.set(false);
        void this.router.navigateByUrl('/habits');
      },
      error: () => {
        this.loading.set(false);
        this.error.set('Email ou mot de passe invalide.');
      },
    });
  }
}
