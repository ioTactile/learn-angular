export interface Habit {
  id: string;
  workspaceId: string;
  title: string;
  createdAt: string;
  streak: number;
  lastCompletedOn: string | null;
}

export interface HabitCompletion {
  id: string;
  habitId: string;
  completedOn: string;
  note: string | null;
  createdAt: string;
}

export interface CreateHabitPayload {
  workspaceId: string;
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
  workspaceId: string;
  page?: number;
  size?: number;
  q?: string;
}

export interface ListCompletionsParams {
  from?: string;
  to?: string;
}
