export interface Habit {
  id: string;
  title: string;
  createdAt: string;
  streak: number;
  lastCompletedOn: string | null;
}

export interface CreateHabitPayload {
  title: string;
}

export interface HabitPage {
  content: Habit[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface ListHabitsParams {
  page?: number;
  size?: number;
  q?: string;
}
