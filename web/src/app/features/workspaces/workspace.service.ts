import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { CreateWorkspacePayload, Workspace } from './workspace.models';

@Injectable({ providedIn: 'root' })
export class WorkspaceService {
  private readonly http = inject(HttpClient);

  list(): Observable<Workspace[]> {
    return this.http.get<Workspace[]>('/api/workspaces');
  }

  create(payload: CreateWorkspacePayload): Observable<Workspace> {
    return this.http.post<Workspace>('/api/workspaces', payload);
  }
}
