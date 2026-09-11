import { provideRouter, Router } from '@angular/router';
import { render, screen } from '@testing-library/angular';
import userEvent from '@testing-library/user-event';
import { of, throwError } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { RegisterPage } from './register.page';

describe('RegisterPage', () => {
  it("crée un compte puis redirige vers /workspaces", async () => {
    const user = userEvent.setup();
    const register = vi.fn().mockReturnValue(of({ accessToken: 'jwt', tokenType: 'Bearer' }));

    const view = await render(RegisterPage, {
      providers: [
        provideRouter([]),
        {
          provide: AuthService,
          useValue: { register },
        },
      ],
    });
    const navigateByUrl = vi
      .spyOn(view.fixture.componentRef.injector.get(Router), 'navigateByUrl')
      .mockResolvedValue(true);

    await user.type(screen.getByLabelText('Email'), 'bob@example.com');
    await user.type(screen.getByLabelText('Mot de passe'), 'Secret123!');
    await user.click(screen.getByRole('button', { name: "S'inscrire" }));

    expect(register).toHaveBeenCalledWith({
      email: 'bob@example.com',
      password: 'Secret123!',
    });
    expect(navigateByUrl).toHaveBeenCalledWith('/workspaces');
  });

  it('affiche un message si email déjà utilisé (409)', async () => {
    const user = userEvent.setup();
    const register = vi.fn().mockReturnValue(throwError(() => ({ status: 409 })));

    await render(RegisterPage, {
      providers: [
        provideRouter([]),
        {
          provide: AuthService,
          useValue: { register },
        },
      ],
    });

    await user.type(screen.getByLabelText('Email'), 'dup@example.com');
    await user.type(screen.getByLabelText('Mot de passe'), 'Secret123!');
    await user.click(screen.getByRole('button', { name: "S'inscrire" }));

    expect((await screen.findByRole('alert')).textContent).toContain(
      'Cet email est déjà utilisé.',
    );
  });

  it("affiche un message générique pour une autre erreur", async () => {
    const user = userEvent.setup();
    const register = vi.fn().mockReturnValue(throwError(() => ({ status: 500 })));

    await render(RegisterPage, {
      providers: [
        provideRouter([]),
        {
          provide: AuthService,
          useValue: { register },
        },
      ],
    });

    await user.type(screen.getByLabelText('Email'), 'err@example.com');
    await user.type(screen.getByLabelText('Mot de passe'), 'Secret123!');
    await user.click(screen.getByRole('button', { name: "S'inscrire" }));

    expect((await screen.findByRole('alert')).textContent).toContain(
      'Impossible de créer le compte.',
    );
  });
});
