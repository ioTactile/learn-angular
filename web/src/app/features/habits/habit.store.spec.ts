import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { ToastService } from '../../core/ui/toast.service';
import { Habit } from './habit.models';
import { HabitService } from './habit.service';
import { HabitStore, projectOptimisticComplete } from './habit.store';

describe('projectOptimisticComplete', () => {
  const base: Habit = {
    id: 'h1',
    workspaceId: 'ws1',
    title: 'Drink water',
    createdAt: '2026-09-09T10:00:00Z',
    streak: 2,
    lastCompletedOn: '2026-09-08',
  };

  it('incrémente le streak si le jour précédent', () => {
    expect(projectOptimisticComplete(base, '2026-09-09')).toEqual({
      ...base,
      streak: 3,
      lastCompletedOn: '2026-09-09',
    });
  });

  it('reste idempotent si déjà complété aujourd’hui', () => {
    const today: Habit = { ...base, lastCompletedOn: '2026-09-09', streak: 3 };
    expect(projectOptimisticComplete(today, '2026-09-09')).toEqual(today);
  });

  it('repart à 1 après un trou', () => {
    expect(projectOptimisticComplete(base, '2026-09-10').streak).toBe(1);
  });
});

describe('HabitStore', () => {
  const habit: Habit = {
    id: 'h1',
    workspaceId: 'ws1',
    title: 'Drink water',
    createdAt: '2026-09-09T10:00:00Z',
    streak: 0,
    lastCompletedOn: null,
  };

  const pageOf = (content: Habit[]) => ({
    content,
    page: 0,
    size: 10,
    totalElements: content.length,
    totalPages: 1,
  });

  let api: {
    list: ReturnType<typeof vi.fn>;
    create: ReturnType<typeof vi.fn>;
    complete: ReturnType<typeof vi.fn>;
    delete: ReturnType<typeof vi.fn>;
  };
  let toast: { success: ReturnType<typeof vi.fn>; error: ReturnType<typeof vi.fn> };
  let store: InstanceType<typeof HabitStore>;

  beforeEach(() => {
    api = {
      list: vi.fn().mockReturnValue(of(pageOf([habit]))),
      create: vi.fn(),
      complete: vi.fn(),
      delete: vi.fn(),
    };
    toast = { success: vi.fn(), error: vi.fn() };

    TestBed.configureTestingModule({
      providers: [
        HabitStore,
        { provide: HabitService, useValue: api },
        { provide: ToastService, useValue: toast },
      ],
    });

    store = TestBed.inject(HabitStore);
    store.initFromRoute('ws1', '', 0, 10);
  });

  it('initFromRoute charge les habits du workspace', () => {
    expect(api.list).toHaveBeenCalledWith({
      workspaceId: 'ws1',
      page: 0,
      size: 10,
      q: '',
    });
    expect(store.habits()).toEqual([habit]);
    expect(store.loading()).toBe(false);
  });

  it('complete applique l’UI optimiste puis confirme avec la réponse serveur', () => {
    const server = { ...habit, streak: 1, lastCompletedOn: '2026-09-10' };
    api.complete.mockReturnValue(of(server));

    store.complete(habit);

    expect(store.habits()[0].streak).toBe(1);
    expect(api.complete).toHaveBeenCalledWith('h1', undefined);
    expect(toast.success).toHaveBeenCalledWith('Complétée — streak 1');
    expect(store.habits()[0]).toEqual(server);
  });

  it('complete rollback si l’API échoue', () => {
    api.complete.mockReturnValue(throwError(() => new Error('boom')));

    store.complete(habit);

    expect(store.habits()[0]).toEqual(habit);
    expect(toast.error).toHaveBeenCalledWith('Completion impossible.');
  });

  it('remove retire tout de suite et restaure en cas d’erreur', () => {
    api.delete.mockReturnValue(throwError(() => new Error('boom')));

    store.remove(habit);

    expect(store.habits()).toEqual([habit]);
    expect(store.totalElements()).toBe(1);
    expect(toast.error).toHaveBeenCalledWith('Suppression impossible.');
  });

  it('remove confirme la suppression sans reload', () => {
    api.delete.mockReturnValue(of(void 0));

    store.remove(habit);

    expect(store.habits()).toEqual([]);
    expect(store.totalElements()).toBe(0);
    expect(toast.success).toHaveBeenCalledWith('Habitude supprimée');
    expect(api.list).toHaveBeenCalledTimes(1);
  });
});
