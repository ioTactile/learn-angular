import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { CreateHabitPayload, Habit } from './habit.models';

@Injectable({ providedIn: 'root' })
export class HabitService {
  private readonly http = inject(HttpClient);

  list(): Observable<Habit[]> {
    return this.http.get<Habit[]>('/api/habits');
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
