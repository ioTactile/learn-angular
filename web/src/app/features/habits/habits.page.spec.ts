import { convertToParamMap } from '@angular/router';
import { ActivatedRoute, Router } from '@angular/router';
import { render, screen } from '@testing-library/angular';
import userEvent from '@testing-library/user-event';
import { of } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { ConfirmDialogService } from '../../core/ui/confirm-dialog.service';
import { ToastService } from '../../core/ui/toast.service';
import { Habit } from './habit.models';
import { HabitService } from './habit.service';
import { HabitsPage } from './habits.page';

describe('HabitsPage', () => {
  const initialHabits: Habit[] = [
    {
      id: 'h1',
      workspaceId: 'ws1',
      title: 'Drink water',
      createdAt: '2026-09-09T10:00:00Z',
      streak: 0,
      lastCompletedOn: null,
    },
  ];

  const pageOf = (content: Habit[]) => ({
    content,
    page: 0,
    size: 10,
    totalElements: content.length,
    totalPages: 1,
  });

  const toast = { success: vi.fn(), error: vi.fn() };
  const confirm = { confirm: vi.fn().mockReturnValue(of(true)) };

  const providers = (habitsApi: object) => [
    { provide: HabitService, useValue: habitsApi },
    {
      provide: AuthService,
      useValue: {
        logout: vi.fn(),
        isAdmin: () => false,
        currentUser: () => null,
      },
    },
    { provide: Router, useValue: { navigateByUrl: vi.fn(), navigate: vi.fn() } },
    {
      provide: ActivatedRoute,
      useValue: {
        snapshot: {
          paramMap: convertToParamMap({ workspaceId: 'ws1' }),
          queryParamMap: convertToParamMap({}),
        },
      },
    },
    { provide: ToastService, useValue: toast },
    { provide: ConfirmDialogService, useValue: confirm },
  ];

  beforeEach(() => {
    vi.clearAllMocks();
    confirm.confirm.mockReturnValue(of(true));
  });

  it('affiche les habitudes chargées puis permet de créer une habitude', async () => {
    const user = userEvent.setup();
    const list = vi.fn().mockReturnValue(of(pageOf(initialHabits)));
    const create = vi.fn().mockReturnValue(
      of({
        id: 'h2',
        workspaceId: 'ws1',
        title: 'Read 10 pages',
        createdAt: '2026-09-09T11:00:00Z',
        streak: 0,
        lastCompletedOn: null,
      }),
    );

    list
      .mockReturnValueOnce(of(pageOf(initialHabits)))
      .mockReturnValueOnce(
        of(
          pageOf([
            {
              id: 'h2',
              workspaceId: 'ws1',
              title: 'Read 10 pages',
              createdAt: '2026-09-09T11:00:00Z',
              streak: 0,
              lastCompletedOn: null,
            },
            ...initialHabits,
          ]),
        ),
      );

    await render(HabitsPage, {
      providers: providers({
        list,
        create,
        complete: vi.fn(),
        delete: vi.fn(),
      }),
    });

    expect(await screen.findByText('Drink water')).toBeTruthy();

    await user.type(screen.getByPlaceholderText('Nouvelle habitude…'), 'Read 10 pages');
    await user.click(screen.getByRole('button', { name: 'Ajouter' }));

    expect(create).toHaveBeenCalledWith({ workspaceId: 'ws1', title: 'Read 10 pages' });
    expect(toast.success).toHaveBeenCalledWith('Habitude ajoutée');
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
      providers: providers({
        list: vi.fn().mockReturnValue(of(pageOf(initialHabits))),
        create: vi.fn(),
        complete,
        delete: vi.fn(),
      }),
    });

    await screen.findByText('Drink water');
    await user.click(screen.getByRole('button', { name: 'Compléter' }));

    expect(complete).toHaveBeenCalledWith('h1', undefined);
    expect(toast.success).toHaveBeenCalledWith('Complétée — streak 1');
    expect(await screen.findByText('streak 1')).toBeTruthy();
  });

  it('demande confirmation avant suppression optimiste', async () => {
    const user = userEvent.setup();
    const remove = vi.fn().mockReturnValue(of(void 0));
    const list = vi.fn().mockReturnValue(of(pageOf(initialHabits)));

    await render(HabitsPage, {
      providers: providers({
        list,
        create: vi.fn(),
        complete: vi.fn(),
        delete: remove,
      }),
    });

    await screen.findByText('Drink water');
    await user.click(screen.getByRole('button', { name: 'Supprimer' }));

    expect(confirm.confirm).toHaveBeenCalled();
    expect(remove).toHaveBeenCalledWith('h1');
    expect(toast.success).toHaveBeenCalledWith('Habitude supprimée');
    expect(await screen.findByText(/Aucune habitude/)).toBeTruthy();
  });
});
