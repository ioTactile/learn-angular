import { provideRouter, Router } from '@angular/router';
import { render, screen } from '@testing-library/angular';
import userEvent from '@testing-library/user-event';
import { of, throwError } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { LoginPage } from './login.page';

describe('LoginPage', () => {
  it('soumet le formulaire puis redirige vers /workspaces', async () => {
    const user = userEvent.setup();
    const login = vi.fn().mockReturnValue(of({ accessToken: 'jwt', tokenType: 'Bearer' }));

    const view = await render(LoginPage, {
      providers: [
        provideRouter([]),
        {
          provide: AuthService,
          useValue: { login },
        },
      ],
    });
    const navigateByUrl = vi
      .spyOn(view.fixture.componentRef.injector.get(Router), 'navigateByUrl')
      .mockResolvedValue(true);

    await user.type(screen.getByLabelText('Email'), 'alice@example.com');
    await user.type(screen.getByLabelText('Mot de passe'), 'Secret123!');
    await user.click(screen.getByRole('button', { name: 'Se connecter' }));

    expect(login).toHaveBeenCalledWith({
      email: 'alice@example.com',
      password: 'Secret123!',
    });
    expect(navigateByUrl).toHaveBeenCalledWith('/workspaces');
  });

  it("affiche un message d'erreur si l'authentification échoue", async () => {
    const user = userEvent.setup();
    const login = vi.fn().mockReturnValue(throwError(() => new Error('401')));

    await render(LoginPage, {
      providers: [
        provideRouter([]),
        {
          provide: AuthService,
          useValue: { login },
        },
      ],
    });

    await user.type(screen.getByLabelText('Email'), 'alice@example.com');
    await user.type(screen.getByLabelText('Mot de passe'), 'WrongPass1');
    await user.click(screen.getByRole('button', { name: 'Se connecter' }));

    expect((await screen.findByRole('alert')).textContent).toContain(
      'Email ou mot de passe invalide.',
    );
  });
});
