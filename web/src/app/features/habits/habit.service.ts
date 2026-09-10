import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { CreateHabitPayload, Habit, HabitPage, ListHabitsParams } from './habit.models';

@Injectable({ providedIn: 'root' })
export class HabitService {
  private readonly http = inject(HttpClient);

  list(params: ListHabitsParams = {}): Observable<HabitPage> {
    let httpParams = new HttpParams()
      .set('page', String(params.page ?? 0))
      .set('size', String(params.size ?? 10));

    if (params.q?.trim()) {
      httpParams = httpParams.set('q', params.q.trim());
    }

    return this.http.get<HabitPage>('/api/habits', { params: httpParams });
  }

  create(payload: CreateHabitPayload): Observable<Habit> {
    return this.http.post<Habit>('/api/habits', payload);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`/api/habits/${id}`);
  }

  complete(id: string): Observable<Habit> {
    return this.http.post<Habit>(`/api/habits/${id}/complete`, {});
  }
}
