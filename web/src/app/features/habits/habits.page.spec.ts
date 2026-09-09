import { Router } from '@angular/router';
import { render, screen } from '@testing-library/angular';
import userEvent from '@testing-library/user-event';
import { of } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { Habit } from './habit.models';
import { HabitService } from './habit.service';
import { HabitsPage } from './habits.page';

describe('HabitsPage', () => {
  const initialHabits: Habit[] = [
    {
      id: 'h1',
      title: 'Drink water',
      createdAt: '2026-09-09T10:00:00Z',
      streak: 0,
      lastCompletedOn: null,
    },
  ];

  it('affiche les habitudes chargées puis permet de créer une habitude', async () => {
    const user = userEvent.setup();
    const list = vi.fn().mockReturnValue(of(initialHabits));
    const create = vi.fn().mockReturnValue(
      of({
        id: 'h2',
        title: 'Read 10 pages',
        createdAt: '2026-09-09T11:00:00Z',
        streak: 0,
        lastCompletedOn: null,
      }),
    );

    await render(HabitsPage, {
      providers: [
        {
          provide: HabitService,
          useValue: {
            list,
            create,
            complete: vi.fn(),
            delete: vi.fn(),
          },
        },
        {
          provide: AuthService,
          useValue: { logout: vi.fn() },
        },
        {
          provide: Router,
          useValue: { navigateByUrl: vi.fn() },
        },
      ],
    });

    expect(await screen.findByText('Drink water')).toBeTruthy();

    await user.type(screen.getByPlaceholderText('Nouvelle habitude…'), 'Read 10 pages');
    await user.click(screen.getByRole('button', { name: 'Ajouter' }));

    expect(create).toHaveBeenCalledWith({ title: 'Read 10 pages' });
    expect(await screen.findByText('Read 10 pages')).toBeTruthy();
  });

  it('complète une habitude puis met à jour le streak affiché', async () => {
    const user = userEvent.setup();
    const complete = vi.fn().mockReturnValue(
      of({
        ...initialHabits[0],
        streak: 1,
        lastCompletedOn: '2026-09-09',
      }),
    );

    await render(HabitsPage, {
      providers: [
        {
          provide: HabitService,
          useValue: {
            list: vi.fn().mockReturnValue(of(initialHabits)),
            create: vi.fn(),
            complete,
            delete: vi.fn(),
          },
        },
        {
          provide: AuthService,
          useValue: { logout: vi.fn() },
        },
        {
          provide: Router,
          useValue: { navigateByUrl: vi.fn() },
        },
      ],
    });

    await screen.findByText('Drink water');
    await user.click(screen.getByRole('button', { name: 'Compléter' }));

    expect(complete).toHaveBeenCalledWith('h1');
    expect(await screen.findByText('streak 1')).toBeTruthy();
  });

  it('supprime une habitude de la liste', async () => {
    const user = userEvent.setup();
    const remove = vi.fn().mockReturnValue(of(void 0));

    await render(HabitsPage, {
      providers: [
        {
          provide: HabitService,
          useValue: {
            list: vi.fn().mockReturnValue(of(initialHabits)),
            create: vi.fn(),
            complete: vi.fn(),
            delete: remove,
          },
        },
        {
          provide: AuthService,
          useValue: { logout: vi.fn() },
        },
        {
          provide: Router,
          useValue: { navigateByUrl: vi.fn() },
        },
      ],
    });

    await screen.findByText('Drink water');
    await user.click(screen.getByRole('button', { name: 'Supprimer' }));

    expect(remove).toHaveBeenCalledWith('h1');
    expect(screen.queryByText('Drink water')).toBeNull();
  });
});
