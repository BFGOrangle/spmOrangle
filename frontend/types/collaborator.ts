// Types that mirror the backend DTOs for task collaborator management

export interface AddCollaboratorRequestDto {
  taskId: number;
  collaboratorId: number;
  assignedById: number;
}

export interface RemoveCollaboratorRequestDto {
  taskId: number;
  collaboratorId: number;
  assignedById: number;
}

export interface AddCollaboratorResponseDto {
  taskId: number;
  collaboratorId: number;
  assignedById: number;
  assignedAt: string; // ISO 8601 timestamp
}

export type CollaboratorAssignmentDto = AddCollaboratorResponseDto;

export interface CollaboratorApiErrorDetail {
  message: string;
  timestamp: string;
  field?: string;
  rejectedValue?: unknown;
}
