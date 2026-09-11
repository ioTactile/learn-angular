export interface Workspace {
  id: string;
  name: string;
  createdAt: string;
}

export interface CreateWorkspacePayload {
  name: string;
}
