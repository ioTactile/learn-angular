import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { HabitService } from './habit.service';
import { Habit } from './habit.models';

describe('HabitService', () => {
  let service: HabitService;
  let http: HttpTestingController;

  const sample: Habit = {
    id: 'h1',
    title: 'Drink water',
    createdAt: '2026-09-09T10:00:00Z',
    streak: 0,
    lastCompletedOn: null,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(HabitService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('list appelle GET /api/habits', () => {
    let habits: Habit[] = [];
    service.list().subscribe((res) => (habits = res));

    const req = http.expectOne('/api/habits');
    expect(req.request.method).toBe('GET');
    req.flush([sample]);

    expect(habits).toEqual([sample]);
  });

  it('create envoie le titre', () => {
    service.create({ title: 'Run' }).subscribe();

    const req = http.expectOne('/api/habits');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ title: 'Run' });
    req.flush({ ...sample, title: 'Run' });
  });

  it('delete appelle DELETE /api/habits/:id', () => {
    service.delete('h1').subscribe();

    const req = http.expectOne('/api/habits/h1');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('complete appelle POST /api/habits/:id/complete', () => {
    service.complete('h1').subscribe();

    const req = http.expectOne('/api/habits/h1/complete');
    expect(req.request.method).toBe('POST');
    req.flush({ ...sample, streak: 1, lastCompletedOn: '2026-09-09' });
  });
});
