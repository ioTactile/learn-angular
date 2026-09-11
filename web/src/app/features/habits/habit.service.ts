import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  CreateHabitPayload,
  Habit,
  HabitCompletion,
  HabitPage,
  ListCompletionsParams,
  ListHabitsParams,
} from './habit.models';

@Injectable({ providedIn: 'root' })
export class HabitService {
  private readonly http = inject(HttpClient);

  list(params: ListHabitsParams): Observable<HabitPage> {
    let httpParams = new HttpParams()
      .set('workspaceId', params.workspaceId)
      .set('page', String(params.page ?? 0))
      .set('size', String(params.size ?? 10));

    if (params.q?.trim()) {
      httpParams = httpParams.set('q', params.q.trim());
    }

    return this.http.get<HabitPage>('/api/habits', { params: httpParams });
  }

  get(id: string): Observable<Habit> {
    return this.http.get<Habit>(`/api/habits/${id}`);
  }

  listCompletions(id: string, params: ListCompletionsParams = {}): Observable<HabitCompletion[]> {
    let httpParams = new HttpParams();
    if (params.from) {
      httpParams = httpParams.set('from', params.from);
    }
    if (params.to) {
      httpParams = httpParams.set('to', params.to);
    }
    return this.http.get<HabitCompletion[]>(`/api/habits/${id}/completions`, { params: httpParams });
  }

  create(payload: CreateHabitPayload): Observable<Habit> {
    return this.http.post<Habit>('/api/habits', payload);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`/api/habits/${id}`);
  }

  complete(id: string, note?: string | null): Observable<Habit> {
    return this.http.post<Habit>(`/api/habits/${id}/complete`, { note: note ?? null });
  }
}
