"use client";

import { useEffect, useMemo, useState } from "react";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Badge } from "@/components/ui/badge";
import { Calendar, X } from "lucide-react";
import { projectService, TaskResponse, UpdateTaskRequest } from "@/services/project-service";
import { tagService } from "@/services/tag-service";
import { useCurrentUser } from "@/contexts/user-context";
import { TaskCollaboratorManagement } from "@/components/task-collaborator-management";
import { userManagementService } from "@/services/user-management-service";
import type { UserResponseDto } from "@/types/user";
import { useUpdateTask } from "@/hooks/use-task-mutations";
import { RecurrenceSelector, RecurrenceData } from "./recurrence-selector";

interface TaskUpdateDialogProps {
  task: TaskResponse;
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onTaskUpdated: (updatedTask: TaskResponse) => void;
}

const STATUS_OPTIONS = [
  { value: 'TODO', label: 'To Do' },
  { value: 'IN_PROGRESS', label: 'In Progress' },
  { value: 'BLOCKED', label: 'Blocked' },
  { value: 'COMPLETED', label: 'Done' },
];

const TASK_TYPE_OPTIONS = [
  { value: 'BUG', label: 'Bug' },
  { value: 'FEATURE', label: 'Feature' },
  { value: 'CHORE', label: 'Chore' },
  { value: 'RESEARCH', label: 'Research' },
];

export function TaskUpdateDialog({
  task,
  open,
  onOpenChange,
  onTaskUpdated,
}: TaskUpdateDialogProps) {
  const updateTaskMutation = useUpdateTask();
  const [title, setTitle] = useState(task.title);
  const [description, setDescription] = useState(task.description || '');
  const [status, setStatus] = useState(task.status);
  const [taskType, setTaskType] = useState(task.taskType);
  const [tags, setTags] = useState<string[]>(task.tags || []);
  const [tagInput, setTagInput] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [availableTags, setAvailableTags] = useState<string[]>([]);
  const [loadingTags, setLoadingTags] = useState(false);
  const [tagsError, setTagsError] = useState<string | null>(null);

  // Due date state - convert UTC to local datetime-local format
  const [dueDate, setDueDate] = useState<string>(() => {
    if (task.dueDateTime) {
      // Convert UTC datetime to local datetime-local format (YYYY-MM-DDTHH:mm)
      const date = new Date(task.dueDateTime);
      const year = date.getFullYear();
      const month = (date.getMonth() + 1).toString().padStart(2, '0');
      const day = date.getDate().toString().padStart(2, '0');
      const hours = date.getHours().toString().padStart(2, '0');
      const minutes = date.getMinutes().toString().padStart(2, '0');
      return `${year}-${month}-${day}T${hours}:${minutes}`;
    }
    return '';
  });

  // Task recurrence data - managed by RecurrenceSelector
  const [recurrenceData, setRecurrenceData] = useState<RecurrenceData>({
    isRecurring: task.isRecurring ?? false,
    recurrenceRuleStr: task.recurrenceRuleStr ?? null,
    startDate: task.startDate ?? null,
    endDate: task.endDate ?? null,
  })

  // Reset form fields when task changes
  useEffect(() => {
    setTitle(task.title);
    setDescription(task.description || '');
    setStatus(task.status);
    setTaskType(task.taskType);
    setTags(task.tags || []);
    setCollaboratorIds(task.assignedUserIds ?? []);
    
    // Reset due date
    if (task.dueDateTime) {
      const date = new Date(task.dueDateTime);
      const year = date.getFullYear();
      const month = (date.getMonth() + 1).toString().padStart(2, '0');
      const day = date.getDate().toString().padStart(2, '0');
      const hours = date.getHours().toString().padStart(2, '0');
      const minutes = date.getMinutes().toString().padStart(2, '0');
      setDueDate(`${year}-${month}-${day}T${hours}:${minutes}`);
    } else {
      setDueDate('');
    }

    setRecurrenceData({
      isRecurring: task.isRecurring ?? false,
      recurrenceRuleStr: task.recurrenceRuleStr ?? null,
      startDate: task.startDate ?? null,
      endDate: task.endDate ?? null
    })
  }, [task]);

  const [collaboratorIds, setCollaboratorIds] = useState<number[]>(
    task.assignedUserIds ?? [],
  );
  const [availableCollaborators, setAvailableCollaborators] = useState<UserResponseDto[]>([]);
  const [loadingCollaborators, setLoadingCollaborators] = useState(false);
  const [collaboratorsError, setCollaboratorsError] = useState<string | null>(null);

  const { currentUser } = useCurrentUser();
  const isManager = currentUser?.role === 'MANAGER';
  const derivedCurrentUserId =
    currentUser?.backendStaffId ??
    (currentUser?.id ? Number.parseInt(currentUser.id, 10) : undefined);
  const currentUserId =
    typeof derivedCurrentUserId === 'number' && !Number.isNaN(derivedCurrentUserId)
      ? derivedCurrentUserId
      : undefined;

  const normalizeTag = (value: string) => value.trim().toLowerCase();

  const tagSuggestions = useMemo(() => {
    if (!availableTags.length) {
      return [] as string[];
    }

    const selectedTags = new Set(tags.map((tag) => normalizeTag(tag)));

    return availableTags
      .filter((tag) => !selectedTags.has(normalizeTag(tag)))
      .slice(0, 10);
  }, [availableTags, tags]);

  const selectedCollaborators = useMemo(() => {
    if (!collaboratorIds.length || !availableCollaborators.length) {
      return [] as UserResponseDto[];
    }

    const collaboratorSet = new Set(collaboratorIds);
    return availableCollaborators.filter((collaborator) =>
      collaboratorSet.has(collaborator.id),
    );
  }, [availableCollaborators, collaboratorIds]);

  const missingCollaboratorIds = useMemo(() => {
    if (!collaboratorIds.length) {
      return [] as number[];
    }

    const knownIds = new Set(availableCollaborators.map((collaborator) => collaborator.id));
    return collaboratorIds.filter((id) => !knownIds.has(id));
  }, [availableCollaborators, collaboratorIds]);

  useEffect(() => {
    if (!open) {
      return;
    }

    let isActive = true;

    const loadTags = async () => {
      try {
        setLoadingTags(true);
        setTagsError(null);
        const tagsResponse = await tagService.getTags();
        if (isActive) {
          setAvailableTags(tagsResponse.map((tag) => tag.tagName));
        }
      } catch (err) {
        console.error('Error loading tags:', err);
        if (isActive) {
          setTagsError('Failed to load tag suggestions.');
        }
      } finally {
        if (isActive) {
          setLoadingTags(false);
        }
      }
    };

    loadTags();

    return () => {
      isActive = false;
    };
  }, [open]);

  useEffect(() => {
    setCollaboratorIds(task.assignedUserIds ?? []);
  }, [task.assignedUserIds, task.id]);

  useEffect(() => {
    if (!open) {
      return;
    }

    let isActive = true;

    const loadCollaborators = async () => {
      try {
        setLoadingCollaborators(true);
        setCollaboratorsError(null);
        const collaborators = await userManagementService.getCollaborators();
        if (isActive) {
          setAvailableCollaborators(collaborators);
        }
      } catch (err) {
        console.error('Error loading collaborators:', err);
        if (isActive) {
          setCollaboratorsError(
            'Failed to load collaborators. You can still manage them, but collaborator names may be unavailable.',
          );
        }
      } finally {
        if (isActive) {
          setLoadingCollaborators(false);
        }
      }
    };

    loadCollaborators();

    return () => {
      isActive = false;
    };
  }, [open]);

  const addTag = (value: string) => {
    const trimmedTag = value.trim();
    if (!trimmedTag) {
      return false;
    }

    let tagWasAdded = false;

    setTags((prev) => {
      if (prev.some((existing) => normalizeTag(existing) === normalizeTag(trimmedTag))) {
        return prev;
      }

      tagWasAdded = true;
      return [...prev, trimmedTag];
    });

    return tagWasAdded;
  };

  const handleAddTag = () => {
    if (addTag(tagInput)) {
      setTagInput('');
    }
  };

  const handleRemoveTag = (tagToRemove: string) => {
    setTags(tags.filter(tag => tag !== tagToRemove));
  };

  const handleCollaboratorsChange = (updatedIds: number[]) => {
    setCollaboratorIds(updatedIds);
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      handleAddTag();
    }
  };

  const ensureManagedTagsExist = async (tagNames: string[]) => {
    if (!isManager || !tagNames.length) {
      return;
    }

    setTagsError(null);

    const existing = new Set(availableTags.map((tag) => normalizeTag(tag)));
    let encounteredError = false;

    for (const tagName of tagNames) {
      const trimmedName = tagName.trim();
      if (!trimmedName) {
        continue;
      }

      const normalized = normalizeTag(trimmedName);
      if (existing.has(normalized)) {
        continue;
      }

      try {
        const createdTag = await tagService.createTag({ tagName: trimmedName });
        existing.add(normalizeTag(createdTag.tagName));
        setAvailableTags((prev) => {
          const alreadyExists = prev.some((existingTag) => normalizeTag(existingTag) === normalizeTag(createdTag.tagName));
          if (alreadyExists) {
            return prev;
          }

          return [...prev, createdTag.tagName].sort((a, b) => a.localeCompare(b));
        });
      } catch (err) {
        encounteredError = true;
        console.error('Error creating tag:', err);
      }
    }

    if (encounteredError) {
      setTagsError('Some tags could not be saved globally. They will still be added to the task.');
    }
  };

const formatDueDateTime = (localDateTime: string): string | undefined => {
  if (!localDateTime) return undefined;
  
  // JavaScript automatically handles the conversion
  const date = new Date(localDateTime);
  
  // Send as ISO string - backend handles it perfectly
  return date.toISOString();
  // Input: "2025-10-06T14:30" (local)
  // Output: "2025-10-06T06:30:00.000Z" (UTC)
  // Backend receives and stores correctly ✅
};

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setIsSubmitting(true);

    try {
      // Build update request with only changed fields
      const updateRequest: UpdateTaskRequest = {
        taskId: task.id,
      };

      if (title !== task.title) {
        updateRequest.title = title;
      }

      if (description !== (task.description || '')) {
        updateRequest.description = description;
      }

      if (status !== task.status) {
        updateRequest.status = status;
      }

      if (taskType !== task.taskType) {
        updateRequest.taskType = taskType;
      }

      // Always send tags if they've been modified
      const originalTags = task.tags || [];
      const tagsChanged = tags.length !== originalTags.length ||
        tags.some((tag, index) => tag !== originalTags[index]);

      if (tagsChanged) {
        updateRequest.tags = tags;
        await ensureManagedTagsExist(tags);
      }

    // Check if due date has changed
    const originalDueDate = task.dueDateTime ? (() => {
      const date = new Date(task.dueDateTime);
      const year = date.getFullYear();
      const month = (date.getMonth() + 1).toString().padStart(2, '0');
      const day = date.getDate().toString().padStart(2, '0');
      const hours = date.getHours().toString().padStart(2, '0');
      const minutes = date.getMinutes().toString().padStart(2, '0');
      return `${year}-${month}-${day}T${hours}:${minutes}`;
    })() : '';

    if (dueDate !== originalDueDate) {
      if (dueDate == '') {
        updateRequest.dueDateTime = undefined // Clear due date
      } else {
        updateRequest.dueDateTime = dueDate ? formatDueDateTime(dueDate) : undefined;
      }
    }

      // Check if recurrence data has changed
      const recurrenceChanged =
        recurrenceData.isRecurring !== (task.isRecurring ?? false) ||
        recurrenceData.recurrenceRuleStr !== (task.recurrenceRuleStr ?? null) ||
        recurrenceData.startDate !== (task.startDate ?? null) ||
        recurrenceData.endDate !== (task.endDate ?? null);

      if (recurrenceChanged) {
        updateRequest.isRecurring = recurrenceData.isRecurring;
        updateRequest.recurrenceRuleStr = recurrenceData.recurrenceRuleStr ?? undefined;
        updateRequest.startDate = recurrenceData.startDate ?? undefined;
        updateRequest.endDate = recurrenceData.endDate ?? undefined;
      }

      // Use the mutation hook for task update with automatic cache invalidation
      const updatedTask = await updateTaskMutation.mutateAsync(updateRequest);
      onTaskUpdated(updatedTask);
      onOpenChange(false);
    } catch (err) {
      console.error('Error updating task:', err);
      setError(err instanceof Error ? err.message : 'Failed to update task');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Update Task</DialogTitle>
          <DialogDescription>
            Modify task details. Only changed fields will be updated.
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="title">Title *</Label>
            <Input
              id="title"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="Task title"
              required
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="description">Description</Label>
            <Textarea
              id="description"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Describe the task..."
              rows={4}
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="status">Status</Label>
              <Select
                value={status}
                onValueChange={(value) => setStatus(value as typeof status)}
              >
                <SelectTrigger id="status">
                  <SelectValue placeholder="Select status" />
                </SelectTrigger>
                <SelectContent>
                  {STATUS_OPTIONS.map((option) => (
                    <SelectItem key={option.value} value={option.value}>
                      {option.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="taskType">Task Type</Label>
              <Select
                value={taskType}
                onValueChange={(value) => setTaskType(value as typeof taskType)}
              >
                <SelectTrigger id="taskType">
                  <SelectValue placeholder="Select task type" />
                </SelectTrigger>
                <SelectContent>
                  {TASK_TYPE_OPTIONS.map((option) => (
                    <SelectItem key={option.value} value={option.value}>
                      {option.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
          </div>
        </div>

        <div className="space-y-2">
          <div className="flex items-center justify-between gap-2">
            <Label>Collaborators</Label>
            {currentUserId ? (
              <TaskCollaboratorManagement
                taskId={task.id}
                currentCollaboratorIds={collaboratorIds}
                currentUserId={currentUserId}
                onCollaboratorsChange={handleCollaboratorsChange}
              />
            ) : (
              <Button type="button" variant="outline" size="sm" disabled>
                Manage Collaborators
              </Button>
            )}
          </div>
          {loadingCollaborators ? (
            <p className="text-sm text-muted-foreground">Loading collaborators…</p>
          ) : collaboratorIds.length > 0 ? (
            <div className="flex flex-wrap gap-2">
              {selectedCollaborators.map((collaborator) => (
                <Badge
                  key={collaborator.id}
                  variant="secondary"
                  className="flex flex-col gap-0 px-2 py-1 text-xs leading-snug"
                >
                  <span className="font-medium">{collaborator.fullName}</span>
                  <span className="text-[10px] text-muted-foreground">{collaborator.email}</span>
                </Badge>
              ))}
              {missingCollaboratorIds.map((id) => (
                <Badge
                  key={`collaborator-${id}`}
                  variant="secondary"
                  className="px-2 py-1 text-xs"
                >
                  User ID: {id}
                </Badge>
              ))}
            </div>
          ) : (
            <p className="text-sm text-muted-foreground">
              No collaborators assigned yet. Use “Manage Collaborators” to add team members.
            </p>
          )}
          {collaboratorsError && (
            <p className="text-xs text-destructive">{collaboratorsError}</p>
          )}
        </div>

        <div className="space-y-2">
          <Label htmlFor="tags">Tags</Label>
          <div className="flex gap-2">
            <Input
                id="tags"
                value={tagInput}
                onChange={(e) => setTagInput(e.target.value)}
                onKeyDown={handleKeyDown}
                placeholder="Add a tag and press Enter"
              />
              <Button
                type="button"
                variant="secondary"
                onClick={handleAddTag}
              >
                Add
              </Button>
            </div>
            {loadingTags ? (
              <p className="text-xs text-muted-foreground">Loading tag suggestions…</p>
            ) : (
              tagSuggestions.length > 0 && (
                <div className="flex flex-wrap gap-2">
                  {tagSuggestions.map((tag) => (
                    <button
                      key={tag}
                      type="button"
                      onClick={() => addTag(tag)}
                      className="focus:outline-none"
                      aria-label={`Add tag ${tag}`}
                    >
                      <Badge variant="outline" className="cursor-pointer px-2 py-1 text-xs">
                        + {tag}
                      </Badge>
                    </button>
                  ))}
                </div>
              )
            )}
            {tagsError && (
              <p className="text-xs text-destructive">{tagsError}</p>
            )}
            {tags.length > 0 && (
              <div className="flex flex-wrap gap-2 mt-2">
                {tags.map((tag) => (
                  <Badge key={tag} variant="secondary" className="gap-1">
                    {tag}
                    <button
                      type="button"
                      onClick={() => handleRemoveTag(tag)}
                      className="ml-1 hover:text-destructive"
                    >
                      <X className="h-3 w-3" />
                    </button>
                  </Badge>
                ))}
              </div>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="dueDate">Due Date & Time</Label>
            
            <div className="relative">
              <Input
                id="dueDate"
                type="datetime-local"
                value={dueDate}
                onChange={(e) => setDueDate(e.target.value)}
                min={new Date().toISOString().slice(0, 16)}
                placeholder="Select due date and time (optional)"
                className={dueDate ? 'pr-10' : ''}
              />
              
              {dueDate && (
                <button
                  type="button"
                  onClick={() => setDueDate('')}
                  className="absolute right-2 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors"
                  aria-label="Clear due date"
                >
                  <X className="h-4 w-4" />
                </button>
              )}
            </div>
            
            {dueDate && (
              <div className="flex items-center gap-2 text-xs">
                <Calendar className="h-3 w-3 text-muted-foreground" />
                <span className="text-muted-foreground">
                  Due: {new Date(dueDate).toLocaleString('en-SG', {
                    dateStyle: 'medium',
                    timeStyle: 'short'
                  })}
                </span>
              </div>
            )}
            
            <p className="text-xs text-muted-foreground">
              Set a deadline for this task (optional)
            </p>
          </div>

          {/* Task Recurrence Settings */}
          <div className="space-y-2">
            <RecurrenceSelector
              onChange={setRecurrenceData}
              initialValue={{
                isRecurring: task.isRecurring ?? false,
                recurrenceRuleStr: task.recurrenceRuleStr ?? null,
                startDate: task.startDate ?? null,
                endDate: task.endDate ?? null,
              }}
            />
          </div>

          {error && (
            <div className="text-sm text-destructive bg-destructive/10 border border-destructive/20 rounded-md p-3">
              {error}
            </div>
          )}

          <DialogFooter>
            <Button
              type="button"
              variant="outline"
              onClick={() => onOpenChange(false)}
              disabled={isSubmitting}
            >
              Cancel
            </Button>
            <Button type="submit" disabled={isSubmitting}>
              {isSubmitting ? 'Updating...' : 'Update Task'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
