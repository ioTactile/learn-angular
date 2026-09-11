import { inject } from '@angular/core';
import { tapResponse } from '@ngrx/operators';
import { patchState, signalStore, withMethods, withState } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { EMPTY, pipe, switchMap, tap } from 'rxjs';
import { ToastService } from '../../core/ui/toast.service';
import { Habit } from './habit.models';
import { HabitService } from './habit.service';

export type HabitStoreState = {
  workspaceId: string | null;
  habits: Habit[];
  loading: boolean;
  creating: boolean;
  filter: string;
  pageIndex: number;
  pageSize: number;
  totalElements: number;
};

const initialState: HabitStoreState = {
  workspaceId: null,
  habits: [],
  loading: true,
  creating: false,
  filter: '',
  pageIndex: 0,
  pageSize: 10,
  totalElements: 0,
};

/** Miroir client des règles domaine streak (idempotent / +1 / reset à 1). */
export function projectOptimisticComplete(
  habit: Habit,
  today = new Date().toISOString().slice(0, 10),
): Habit {
  if (habit.lastCompletedOn === today) {
    return habit;
  }

  const nextStreak =
    habit.lastCompletedOn !== null && isNextCalendarDay(habit.lastCompletedOn, today)
      ? habit.streak + 1
      : 1;

  return { ...habit, streak: nextStreak, lastCompletedOn: today };
}

function isNextCalendarDay(previous: string, today: string): boolean {
  const prev = Date.parse(`${previous}T00:00:00.000Z`);
  const curr = Date.parse(`${today}T00:00:00.000Z`);
  return curr - prev === 86_400_000;
}

export const HabitStore = signalStore(
  withState(initialState),
  withMethods((store, api = inject(HabitService), toast = inject(ToastService)) => {
    const load = rxMethod<void>(
      pipe(
        tap(() => patchState(store, { loading: true })),
        switchMap(() => {
          const workspaceId = store.workspaceId();
          if (!workspaceId) {
            patchState(store, { loading: false, habits: [], totalElements: 0 });
            return EMPTY;
          }
          return api
            .list({
              workspaceId,
              page: store.pageIndex(),
              size: store.pageSize(),
              q: store.filter(),
            })
            .pipe(
              tapResponse({
                next: (page) =>
                  patchState(store, {
                    habits: page.content,
                    totalElements: page.totalElements,
                    loading: false,
                  }),
                error: () => {
                  patchState(store, { loading: false });
                  toast.error('Impossible de charger les habitudes.');
                },
              }),
            );
        }),
      ),
    );

    return {
      load,

      /** Hydrate depuis les route/query params (équivalent Next searchParams). */
      initFromRoute(workspaceId: string, q: string, pageIndex: number, pageSize: number): void {
        patchState(store, {
          workspaceId,
          filter: q,
          pageIndex,
          pageSize: pageSize > 0 ? pageSize : 10,
        });
        load();
      },

      setFilter(filter: string): void {
        patchState(store, { filter, pageIndex: 0 });
        load();
      },

      setPage(pageIndex: number, pageSize: number): void {
        patchState(store, { pageIndex, pageSize });
        load();
      },

      create(title: string, onSuccess?: () => void): void {
        const workspaceId = store.workspaceId();
        if (!workspaceId) {
          return;
        }
        patchState(store, { creating: true });
        api.create({ workspaceId, title }).subscribe({
          next: () => {
            patchState(store, { creating: false, pageIndex: 0 });
            onSuccess?.();
            toast.success('Habitude ajoutée');
            load();
          },
          error: () => {
            patchState(store, { creating: false });
            toast.error('Création impossible.');
          },
        });
      },

      complete(habit: Habit, note?: string | null): void {
        const previous = habit;
        const optimistic = projectOptimisticComplete(habit);
        patchState(store, {
          habits: store.habits().map((h) => (h.id === habit.id ? optimistic : h)),
        });

        api.complete(habit.id, note).subscribe({
          next: (updated) => {
            patchState(store, {
              habits: store.habits().map((h) => (h.id === updated.id ? updated : h)),
            });
            toast.success(`Complétée — streak ${updated.streak}`);
          },
          error: () => {
            patchState(store, {
              habits: store.habits().map((h) => (h.id === previous.id ? previous : h)),
            });
            toast.error('Completion impossible.');
          },
        });
      },

      remove(habit: Habit): void {
        const previousHabits = store.habits();
        const previousTotal = store.totalElements();

        patchState(store, {
          habits: previousHabits.filter((h) => h.id !== habit.id),
          totalElements: Math.max(0, previousTotal - 1),
        });

        api.delete(habit.id).subscribe({
          next: () => toast.success('Habitude supprimée'),
          error: () => {
            patchState(store, {
              habits: previousHabits,
              totalElements: previousTotal,
            });
            toast.error('Suppression impossible.');
          },
        });
      },
    };
  }),
);
