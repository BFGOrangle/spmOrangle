"use client";

import { useMutation, UseMutationOptions, UseMutationResult } from "@tanstack/react-query";
import {
  taskCollaboratorApiService,
  TaskCollaboratorApiService,
  CollaboratorApiError,
  CollaboratorValidationError,
} from "@/services/task-collaborator-api";
import {
  AddCollaboratorRequestDto,
  AddCollaboratorResponseDto,
  RemoveCollaboratorRequestDto,
} from "@/types/collaborator";

export type CollaboratorMutationError =
  | CollaboratorApiError
  | CollaboratorValidationError;

export type AddCollaboratorMutationOptions = Omit<
  UseMutationOptions<
    AddCollaboratorResponseDto,
    CollaboratorMutationError,
    AddCollaboratorRequestDto
  >,
  "mutationFn" | "mutationKey"
>;

export type RemoveCollaboratorMutationOptions = Omit<
  UseMutationOptions<void, CollaboratorMutationError, RemoveCollaboratorRequestDto>,
  "mutationFn" | "mutationKey"
>;

const ADD_COLLABORATOR_MUTATION_KEY = [
  "tasks",
  "collaborators",
  "add",
] as const;
const REMOVE_COLLABORATOR_MUTATION_KEY = [
  "tasks",
  "collaborators",
  "remove",
] as const;

export function useAddCollaboratorMutation(
  options?: AddCollaboratorMutationOptions,
  apiService: TaskCollaboratorApiService = taskCollaboratorApiService,
): UseMutationResult<
  AddCollaboratorResponseDto,
  CollaboratorMutationError,
  AddCollaboratorRequestDto
> {
  return useMutation<
    AddCollaboratorResponseDto,
    CollaboratorMutationError,
    AddCollaboratorRequestDto
  >({
    mutationKey: ADD_COLLABORATOR_MUTATION_KEY,
    mutationFn: (payload) => apiService.addCollaborator(payload),
    ...options,
  });
}

export function useRemoveCollaboratorMutation(
  options?: RemoveCollaboratorMutationOptions,
  apiService: TaskCollaboratorApiService = taskCollaboratorApiService,
): UseMutationResult<
  void,
  CollaboratorMutationError,
  RemoveCollaboratorRequestDto
> {
  return useMutation<void, CollaboratorMutationError, RemoveCollaboratorRequestDto>({
    mutationKey: REMOVE_COLLABORATOR_MUTATION_KEY,
    mutationFn: (payload) => apiService.removeCollaborator(payload),
    ...options,
  });
}

export function useTaskCollaboratorMutations(
  config?: {
    addOptions?: AddCollaboratorMutationOptions;
    removeOptions?: RemoveCollaboratorMutationOptions;
    apiService?: TaskCollaboratorApiService;
  },
): {
  addCollaboratorMutation: UseMutationResult<
    AddCollaboratorResponseDto,
    CollaboratorMutationError,
    AddCollaboratorRequestDto
  >;
  removeCollaboratorMutation: UseMutationResult<
    void,
    CollaboratorMutationError,
    RemoveCollaboratorRequestDto
  >;
} {
  const service = config?.apiService ?? taskCollaboratorApiService;

  const addCollaboratorMutation = useAddCollaboratorMutation(
    config?.addOptions,
    service,
  );
  const removeCollaboratorMutation = useRemoveCollaboratorMutation(
    config?.removeOptions,
    service,
  );

  return {
    addCollaboratorMutation,
    removeCollaboratorMutation,
  };
}
