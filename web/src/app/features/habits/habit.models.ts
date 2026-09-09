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
