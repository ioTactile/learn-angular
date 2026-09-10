import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { UserRole } from '../../core/auth/auth.models';

export interface AdminUser {
  id: string;
  email: string;
  role: UserRole;
  createdAt: string;
}

@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly http = inject(HttpClient);

  listUsers(): Observable<AdminUser[]> {
    return this.http.get<AdminUser[]>('/api/admin/users');
  }
}
