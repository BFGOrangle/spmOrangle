"use client";

import { useEffect, useMemo, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Separator } from "@/components/ui/separator";
import { toast } from "@/hooks/use-toast";
import { useTaskCollaboratorMutations } from "@/hooks/use-task-collaborators";
import {
  CollaboratorApiError,
  CollaboratorValidationError,
} from "@/services/task-collaborator-api";
import { userManagementService } from "@/services/user-management-service";
import type { UserResponseDto } from "@/types/user";
import { Loader2, UserMinus, UserPlus, Users as UsersIcon } from "lucide-react";

function getCollaboratorErrorMessage(error: unknown): string {
  if (!error) {
    return "An unexpected error occurred.";
  }

  if (error instanceof CollaboratorValidationError) {
    return (
      error.validationErrors
        ?.map(({ message }) => message)
        .filter(Boolean)
        .join(", ") || "Validation error"
    );
  }

  if (error instanceof CollaboratorApiError) {
    if (error.errors?.length) {
      return error.errors
        .map(({ message }) => message)
        .filter(Boolean)
        .join(", ");
    }
    return error.message;
  }

  if (error instanceof Error) {
    return error.message;
  }

  return "An unexpected error occurred.";
}

interface TaskCollaboratorManagementProps {
  taskId: number;
  currentCollaboratorIds: number[];
  currentUserId: number;
  onCollaboratorsChange?: (collaboratorIds: number[]) => void;
}

const AVAILABLE_COLLABORATORS_QUERY_KEY = [
  "tasks",
  "collaborators",
  "available",
] as const;

export function TaskCollaboratorManagement({
  taskId,
  currentCollaboratorIds,
  currentUserId,
  onCollaboratorsChange,
}: TaskCollaboratorManagementProps) {
  const [isOpen, setIsOpen] = useState(false);
  const [selectedUserId, setSelectedUserId] = useState<string>("");

  const {
    data: collaborators = [],
    isLoading: isCollaboratorsLoading,
    error: collaboratorsError,
  } = useQuery<UserResponseDto[]>({
    queryKey: AVAILABLE_COLLABORATORS_QUERY_KEY,
    queryFn: () => userManagementService.getCollaborators(),
    staleTime: 5 * 60 * 1000,
  });

  useEffect(() => {
    if (collaboratorsError) {
      toast({
        title: "Unable to load collaborators",
        description: getCollaboratorErrorMessage(collaboratorsError),
        variant: "destructive",
      });
    }
  }, [collaboratorsError]);

  const collaboratorMap = useMemo(() => {
    return new Map(collaborators.map((collaborator) => [collaborator.id, collaborator]));
  }, [collaborators]);

  const availableCollaborators = useMemo(() => {
    return collaborators
      .filter(
        (collaborator) =>
          !currentCollaboratorIds.includes(collaborator.id) &&
          collaborator.id !== currentUserId,
      )
      .sort((a, b) => {
        const nameA = (a.username || a.email || `User ${a.id}`).toLowerCase();
        const nameB = (b.username || b.email || `User ${b.id}`).toLowerCase();
        return nameA.localeCompare(nameB);
      });
  }, [collaborators, currentCollaboratorIds, currentUserId]);

  useEffect(() => {
    if (
      selectedUserId &&
      availableCollaborators.every(
        (collaborator) => collaborator.id.toString() !== selectedUserId,
      )
    ) {
      setSelectedUserId("");
    }
  }, [availableCollaborators, selectedUserId]);

  const { addCollaboratorMutation, removeCollaboratorMutation } =
    useTaskCollaboratorMutations();

  const isMutationPending =
    addCollaboratorMutation.isPending || removeCollaboratorMutation.isPending;

  const handleAddCollaborator = async () => {
    if (!selectedUserId || selectedUserId === "no-users") {
      return;
    }

    // Check if adding would exceed the 5 assignee limit
    if (currentCollaboratorIds.length >= 5) {
      toast({
        title: "Cannot add collaborator",
        description: "Maximum 5 assignees allowed per task",
        variant: "destructive",
      });
      return;
    }

    const collaboratorId = Number.parseInt(selectedUserId, 10);

    try {
      const response = await addCollaboratorMutation.mutateAsync({
        taskId,
        collaboratorId,
      });

      const collaborator = collaboratorMap.get(response.collaboratorId);

      toast({
        title: "Collaborator added",
        description:
          collaborator?.username
            ? `${collaborator.username} has been added to the task.`
            : `User ID ${response.collaboratorId} has been added to the task.`,
      });

      if (onCollaboratorsChange) {
        const updatedCollaborators = Array.from(
          new Set([...currentCollaboratorIds, response.collaboratorId]),
        );
        onCollaboratorsChange(updatedCollaborators);
      }

      setSelectedUserId("");
    } catch (error) {
      toast({
        title: "Error adding collaborator",
        description: getCollaboratorErrorMessage(error),
        variant: "destructive",
      });
    }
  };

  const handleRemoveCollaborator = async (collaboratorId: number) => {
    try {
      await removeCollaboratorMutation.mutateAsync({
        taskId,
        collaboratorId,
      });

      if (onCollaboratorsChange) {
        onCollaboratorsChange(
          currentCollaboratorIds.filter((id) => id !== collaboratorId),
        );
      }

      const collaborator = collaboratorMap.get(collaboratorId);

      toast({
        title: "Collaborator removed",
        description:
          collaborator?.username
            ? `${collaborator.username} has been removed from the task.`
            : `User ID ${collaboratorId} has been removed from the task.`,
      });
    } catch (error) {
      toast({
        title: "Error removing collaborator",
        description: getCollaboratorErrorMessage(error),
        variant: "destructive",
      });
    }
  };

  const isAddDisabled =
    !selectedUserId ||
    selectedUserId === "no-users" ||
    isMutationPending ||
    isCollaboratorsLoading ||
    availableCollaborators.length === 0 ||
    currentCollaboratorIds.length >= 5;

  const addButtonLabel = isMutationPending ? "Processing..." : "Add Collaborator";

  const selectPlaceholder = isCollaboratorsLoading
    ? "Loading collaborators..."
    : "Select a collaborator to add";

  return (
    <Dialog open={isOpen} onOpenChange={setIsOpen}>
      <DialogTrigger asChild>
        <Button variant="outline" size="sm" className="h-8 px-2">
          <UsersIcon className="h-4 w-4 mr-1" />
          Manage Collaborators
        </Button>
      </DialogTrigger>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>Manage Task Collaborators</DialogTitle>
          <DialogDescription>
            Add or remove collaborators for this task. Maximum 5 assignees allowed.
            {currentCollaboratorIds.length > 0 && (
              <span className="block mt-1 text-sm font-medium">
                Current assignees: {currentCollaboratorIds.length} / 5
              </span>
            )}
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-4">
          {/* Current Collaborators */}
          <div>
            <h4 className="text-sm font-medium mb-3">Current Collaborators</h4>
            {currentCollaboratorIds.length > 0 ? (
              <div className="space-y-2">
                {currentCollaboratorIds.map((collaboratorId) => {
                  const collaborator = collaboratorMap.get(collaboratorId);
                  const collaboratorLabel =
                    collaborator?.username ||
                    collaborator?.email ||
                    `User ID: ${collaboratorId}`;
                  const collaboratorInitials = collaboratorLabel
                    .split(" ")
                    .map((part) => part.trim()[0])
                    .filter(Boolean)
                    .join("")
                    .slice(0, 2)
                    .toUpperCase();

                  return (
                    <div
                      key={collaboratorId}
                      className="flex items-center justify-between py-2 px-3 border rounded-lg"
                    >
                      <div className="flex items-center gap-2">
                        <div className="flex h-8 w-8 items-center justify-center rounded-full bg-secondary text-sm font-semibold text-secondary-foreground">
                          {collaboratorInitials || collaboratorId}
                        </div>
                        <div className="flex flex-col">
                          <span className="text-sm font-medium">{collaboratorLabel}</span>
                          {collaborator?.email && collaborator?.username && (
                            <span className="text-xs text-muted-foreground">
                              {collaborator.email}
                            </span>
                          )}
                        </div>
                      </div>
                      <Button
                        variant="ghost"
                        size="sm"
                        className="h-8 w-8 p-0 text-muted-foreground hover:text-destructive"
                        onClick={() => handleRemoveCollaborator(collaboratorId)}
                        disabled={isMutationPending}
                      >
                        <UserMinus className="h-4 w-4" />
                      </Button>
                    </div>
                  );
                })}
              </div>
            ) : (
              <p className="text-sm text-muted-foreground py-4 text-center">
                No collaborators assigned
              </p>
            )}
          </div>

          <Separator />

          {/* Add New Collaborator */}
          <div>
            <h4 className="text-sm font-medium mb-3">Add Collaborator</h4>
            <div className="space-y-3">
              <Select value={selectedUserId} onValueChange={setSelectedUserId}>
                <SelectTrigger disabled={isCollaboratorsLoading || isMutationPending}>
                  <SelectValue placeholder={selectPlaceholder} />
                </SelectTrigger>
                <SelectContent>
                  {availableCollaborators.map((collaborator) => (
                    <SelectItem
                      key={collaborator.id}
                      value={collaborator.id.toString()}
                    >
                      <div className="flex flex-col">
                        <span className="text-sm font-medium">
                          {collaborator.username || collaborator.email || `User ${collaborator.id}`}
                        </span>
                        <span className="text-xs text-muted-foreground">
                          ID: {collaborator.id}
                          {collaborator.email ? ` · ${collaborator.email}` : ""}
                        </span>
                      </div>
                    </SelectItem>
                  ))}
                  {availableCollaborators.length === 0 && !isCollaboratorsLoading && (
                    <SelectItem value="no-users" disabled>
                      No collaborators available to add
                    </SelectItem>
                  )}
                </SelectContent>
              </Select>

              <Button
                onClick={handleAddCollaborator}
                disabled={isAddDisabled}
                className="w-full"
              >
                {isMutationPending ? (
                  <>
                    <Loader2 className="h-4 w-4 animate-spin mr-2" />
                    {addButtonLabel}
                  </>
                ) : (
                  <>
                    <UserPlus className="h-4 w-4 mr-2" />
                    {addButtonLabel}
                  </>
                )}
              </Button>
            </div>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
}
