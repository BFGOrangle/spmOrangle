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
import { ScrollArea } from "@/components/ui/scroll-area";
import { projectService, CreateTaskRequest, TaskResponse } from "@/services/project-service";
import { userManagementService } from "@/services/user-management-service";
import { fileService } from "@/services/file-service";
import { useCurrentUser } from "@/contexts/user-context";
import { UserResponseDto } from "@/types/user";
import { User, Crown, Loader2, X, Calendar } from "lucide-react";
import { tagService } from "@/services/tag-service";
import { useCreateTask } from "@/hooks/use-task-mutations";
import { RecurrenceData, RecurrenceSelector } from "./recurrence-selector";

// Helper for file upload
async function uploadFiles({ files, taskId, projectId }: { files: FileList | File[], taskId: number, projectId: number }) {
  console.log('uploadFiles called with:', { taskId, projectId, filesCount: files.length });

  const uploads = Array.from(files).map(async (file, index) => {
    console.log(`Uploading file ${index + 1}/${files.length}:`, file.name);
    try {
      // Use correct argument object for uploadFile
      const result = await fileService.uploadFile({ file, taskId, projectId });
      console.log(`File ${file.name} uploaded successfully:`, result);
      return result;
    } catch (error) {
      console.error(`Error uploading file ${file.name}:`, error);
      throw error;
    }
  });

  return Promise.all(uploads);
}

interface TaskCreationDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  projectId?: number; // If provided, defaults to this project; if null/undefined, allows selection
  onTaskCreated?: (task: TaskResponse) => void;
  availableProjects?: Array<{ id: number; name: string }>; // Projects manager can assign tasks to
}

const TASK_TYPES = [
  { value: 'BUG', label: 'Bug' },
  { value: 'FEATURE', label: 'Feature' },
  { value: 'CHORE', label: 'Chore' },
  { value: 'RESEARCH', label: 'Research' },
] as const;

const TASK_STATUSES = [
  { value: 'TODO', label: 'To Do' },
  { value: 'IN_PROGRESS', label: 'In Progress' },
  { value: 'BLOCKED', label: 'Blocked' },
  { value: 'COMPLETED', label: 'Completed' },
] as const;

const PRIORITY_OPTIONS = [
  { value: 10, label: '10 - Highest' },
  { value: 9, label: '9' },
  { value: 8, label: '8' },
  { value: 7, label: '7' },
  { value: 6, label: '6' },
  { value: 5, label: '5 - Medium' },
  { value: 4, label: '4' },
  { value: 3, label: '3' },
  { value: 2, label: '2' },
  { value: 1, label: '1 - Lowest' },
] as const;

export function TaskCreationDialog({ 
  open, 
  onOpenChange, 
  projectId, 
  onTaskCreated,
  availableProjects = []
}: TaskCreationDialogProps) {
  const { currentUser } = useCurrentUser();
  const createTaskMutation = useCreateTask();
  const [formData, setFormData] = useState<Partial<CreateTaskRequest>>({
    title: '',
    description: '',
    taskType: 'FEATURE',
    status: 'TODO',
    tags: [],
    assignedUserIds: [],
    projectId,
    priority: 5, // Default to medium priority
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedFiles, setSelectedFiles] = useState<FileList | null>(null);
  const [availableTags, setAvailableTags] = useState<string[]>([]);
  const [tagsError, setTagsError] = useState<string | null>(null);
  const [loadingTags, setLoadingTags] = useState(false);
  
  // New state for task assignment and project selection
  const [projectMembers, setProjectMembers] = useState<UserResponseDto[]>([]);
  const [loadingMembers, setLoadingMembers] = useState(false);
  const [selectedAssignee, setSelectedAssignee] = useState<number | null>(null);
  const [selectedProjectId, setSelectedProjectId] = useState<number | null>(projectId || null);

  // Collaborator management state
  const [availableCollaborators, setAvailableCollaborators] = useState<UserResponseDto[]>([]);
  const [loadingCollaborators, setLoadingCollaborators] = useState(false);
  const [collaboratorsError, setCollaboratorsError] = useState<string | null>(null);
  const [isCollaboratorDialogOpen, setIsCollaboratorDialogOpen] = useState(false);
  const [draftCollaboratorIds, setDraftCollaboratorIds] = useState<number[]>([]);

  // Due date state
  const [dueDate, setDueDate] = useState<string>('');

  // Get current datetime in local format for min attribute
  const getCurrentLocalDateTime = () => {
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    const hours = String(now.getHours()).padStart(2, '0');
    const minutes = String(now.getMinutes()).padStart(2, '0');
    return `${year}-${month}-${day}T${hours}:${minutes}`;
  };

  const isManager = currentUser?.role === 'MANAGER';
  const isPersonalTask = selectedProjectId === null || selectedProjectId === 0;
  const canAssignToOthers = isManager && !isPersonalTask; // Only managers can assign project tasks to others
  const canSelectProject = isManager && !projectId; // Only managers can select project when not called from specific project

  // Get the current user's ID consistently
  const currentUserId = currentUser?.backendStaffId || parseInt(currentUser?.id || '1');

  const selectedCollaboratorIds = formData.assignedUserIds ?? [];

  const selectedCollaborators = useMemo(() => {
    if (!selectedCollaboratorIds.length || !availableCollaborators.length) {
      return [] as UserResponseDto[];
    }

    const selectedSet = new Set(selectedCollaboratorIds);
    return availableCollaborators.filter((collaborator) =>
      selectedSet.has(collaborator.id),
    );
  }, [availableCollaborators, selectedCollaboratorIds]);

  const missingCollaboratorIds = useMemo(() => {
    if (!selectedCollaboratorIds.length) {
      return [] as number[];
    }

    const knownIds = new Set(availableCollaborators.map((collaborator) => collaborator.id));
    return selectedCollaboratorIds.filter((id) => !knownIds.has(id));
  }, [availableCollaborators, selectedCollaboratorIds]);

  const normalizeTag = (tag: string) => tag.trim().toLowerCase();

  const tagSuggestions = useMemo(() => {
    if (!availableTags.length) {
      return [] as string[];
    }

    const selectedTags = new Set((formData.tags ?? []).map((tag) => normalizeTag(tag)));

    return availableTags
      .filter((tag) => !selectedTags.has(normalizeTag(tag)))
      .slice(0, 10);
  }, [availableTags, formData.tags]);

  const [recurrenceData, setRecurrenceData] = useState<RecurrenceData>({
    isRecurring: false,
    recurrenceRuleStr: null,
    startDate: null,
    endDate: null
  })

  // Reset form when dialog opens
  useEffect(() => {
    if (open) {
      setFormData({
        title: '',
        description: '',
        status: 'TODO',
        taskType: 'FEATURE',
        tags: [],
        assignedUserIds: [],
        priority: 5, // Reset to medium priority
      });
      setDueDate('');
      setSelectedProjectId(projectId || null);
      setSelectedAssignee(null);
      setSelectedFiles(null);
      setDraftCollaboratorIds([]);
    }
  }, [open, projectId]);

  useEffect(() => {
    if (!open) {
      return;
    }

    let isActive = true;

    const loadTags = async () => {
      try {
        setLoadingTags(true);
        setTagsError(null);
        const tags = await tagService.getTags();
        if (isActive) {
          const tagNames = tags.map((tag) => tag.tagName);
          setAvailableTags(tagNames);
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
          setCollaboratorsError('Failed to load collaborators. You can still create the task and add them later.');
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

  // Load project members when dialog opens and projectId is available
  useEffect(() => {
    if (open && selectedProjectId && canAssignToOthers) {
      loadProjectMembers();
    }
  }, [open, selectedProjectId, canAssignToOthers]);

  useEffect(() => {
    if (!open) {
      setIsCollaboratorDialogOpen(false);
    }
  }, [open]);

  useEffect(() => {
    if (isCollaboratorDialogOpen) {
      setDraftCollaboratorIds(selectedCollaboratorIds);
    }
  }, [isCollaboratorDialogOpen, selectedCollaboratorIds]);

  const loadProjectMembers = async () => {
    if (!selectedProjectId) return;
    
    try {
      setLoadingMembers(true);
      const members = await userManagementService.getProjectMembers(selectedProjectId);
      setProjectMembers(members);
    } catch (err) {
      console.error('Error loading project members:', err);
      setError('Failed to load project members. You can still create the task without assignment.');
    } finally {
      setLoadingMembers(false);
    }
  };

  const toggleDraftCollaborator = (collaboratorId: number) => {
    setDraftCollaboratorIds((prev) => {
      if (prev.includes(collaboratorId)) {
        return prev.filter((id) => id !== collaboratorId);
      }
      // Check if we would exceed the 5 assignee limit
      if (prev.length >= 5) {
        setError('Maximum 5 assignees allowed per task');
        return prev;
      }
      setError(null);
      return [...prev, collaboratorId];
    });
  };

  const handleCollaboratorSelectionSave = () => {
    const uniqueIds = Array.from(new Set(draftCollaboratorIds));
    setFormData((prev) => ({
      ...prev,
      assignedUserIds: uniqueIds,
    }));
    setIsCollaboratorDialogOpen(false);
  };

  const handleCollaboratorSelectionClear = () => {
    setDraftCollaboratorIds([]);
    setFormData((prev) => ({
      ...prev,
      assignedUserIds: [],
    }));
  };

  const handleRemoveSelectedCollaborator = (collaboratorId: number) => {
    setFormData((prev) => ({
      ...prev,
      assignedUserIds: (prev.assignedUserIds ?? []).filter((id) => id !== collaboratorId),
    }));
  };

  const addTag = (tag: string) => {
    const trimmedTag = tag.trim();
    if (!trimmedTag) {
      return;
    }

    setFormData((prev) => {
      const currentTags = prev.tags ?? [];
      if (currentTags.some((existing) => normalizeTag(existing) === normalizeTag(trimmedTag))) {
        return prev;
      }

      return {
        ...prev,
        tags: [...currentTags, trimmedTag],
      };
    });
  };

  const ensureManagedTagsExist = async (tagNames: string[]) => {
    if (!isManager || !tagNames.length) {
      return;
    }

    setTagsError(null);

    const existing = new Set(availableTags.map((tag) => normalizeTag(tag)));
    let encounteredError = false;

    for (const tagName of tagNames) {
      const trimmedTagName = tagName.trim();
      if (!trimmedTagName) {
        continue;
      }

      const normalizedTag = normalizeTag(trimmedTagName);
      if (existing.has(normalizedTag)) {
        continue;
      }

      try {
        const createdTag = await tagService.createTag({ tagName: trimmedTagName });
        existing.add(normalizeTag(createdTag.tagName));
        setAvailableTags((prev) => {
          const alreadyHasTag = prev.some((existingTag) => normalizeTag(existingTag) === normalizeTag(createdTag.tagName));
          if (alreadyHasTag) {
            return prev;
          }

          return [...prev, createdTag.tagName].sort((a, b) => a.localeCompare(b));
        });
      } catch (creationError) {
        encounteredError = true;
        console.error('Error creating tag:', creationError);
      }
    }

    if (encounteredError) {
      setTagsError('Some tags could not be saved globally. They will still be added to the task.');
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!formData.title?.trim()) {
      setError('Title is required');
      return;
    }

    if (!formData.taskType) {
      setError('Task type is required');
      return;
    }

    // Validate due date is not in the past
    if (dueDate && new Date(dueDate) < new Date()) {
      setError('Due date cannot be in the past');
      return;
    }

    try {
      setLoading(true);
      setError(null);

      // Determine the owner ID and which endpoint to use
      const assigneeId = selectedAssignee || currentUserId;
      const shouldUseManagerEndpoint = canAssignToOthers && selectedAssignee && selectedAssignee !== currentUserId;

      const uniqueCollaboratorIds = Array.from(new Set(selectedCollaboratorIds));
      // Convert local datetime to ISO string with timezone offset if dueDate is provided
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

      const taskData: CreateTaskRequest = {
        ...formData,
        assignedUserIds:
          uniqueCollaboratorIds.length > 0 ? uniqueCollaboratorIds : undefined,
        ownerId: assigneeId,
        title: formData.title!,
        taskType: formData.taskType!,
        projectId: isPersonalTask ? 0 : selectedProjectId!,
        dueDateTime: formatDueDateTime(dueDate),
        isRecurring: recurrenceData.isRecurring,
        recurrenceRuleStr: recurrenceData.recurrenceRuleStr ?? undefined,
        startDate: recurrenceData.startDate ?? undefined,
        endDate: recurrenceData.endDate ?? undefined
      };

      await ensureManagedTagsExist(taskData.tags ?? []);

      console.log('Creating task with data:', taskData);
      console.log('Using manager endpoint:', shouldUseManagerEndpoint);

      // Use the mutation hook for task creation with automatic cache invalidation
      const createdTask = await createTaskMutation.mutateAsync({
        taskData,
        useManagerEndpoint: !!shouldUseManagerEndpoint
      });
      
      console.log('Task created successfully:', createdTask);

      // Upload files if any
      if (selectedFiles && createdTask.id) {
        console.log('Starting file upload...');
        try {
          await uploadFiles({
            files: selectedFiles,
            taskId: createdTask.id,
            projectId: createdTask.projectId ?? (isPersonalTask ? 0 : selectedProjectId!),
          });
          console.log('File upload completed successfully');
        } catch (uploadError) {
          console.error('File upload failed:', uploadError);
          setError('Task created successfully, but file upload failed. Please try uploading files again.');
        }
      }

      onTaskCreated?.(createdTask);
      onOpenChange(false);

      // Reset form and file input
      setFormData({
        title: '',
        description: '',
        taskType: 'FEATURE',
        status: 'TODO',
        tags: [],
        assignedUserIds: [],
        projectId,
        priority: 5, // Reset to medium priority
      });
      setSelectedFiles(null);
      setSelectedAssignee(null);
      setSelectedProjectId(projectId || null);
      setDueDate('');
      setError(null);
    } catch (err) {
      console.error('Error creating task:', err);
      setError('Failed to create task. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleTagsChange = (value: string) => {
    const rawTags = value
      .split(',')
      .map((tag) => tag.trim())
      .filter(Boolean);

    const dedupedTags = Array.from(
      new Map(rawTags.map((tag) => [normalizeTag(tag), tag])).values(),
    );

    setFormData((prev) => ({ ...prev, tags: dedupedTags }));
  };

  return (
    <>
      <Dialog open={open} onOpenChange={onOpenChange}>
        <DialogContent
          className="sm:max-w-[525px] max-h-[90vh] flex flex-col"
          onInteractOutside={(event) => {
            if (isCollaboratorDialogOpen) {
              event.preventDefault();
            }
          }}
          onEscapeKeyDown={(event) => {
            if (isCollaboratorDialogOpen) {
              event.preventDefault();
            }
          }}
        >
        <DialogHeader>
          <DialogTitle>
            Create New Task
          </DialogTitle>
          <DialogDescription>
            {canSelectProject ? (
              <>
                As a manager, you can create a task for any project or create a personal task.
                {canAssignToOthers && (
                  <span className="block mt-1 text-sm text-blue-600">
                    <Crown className="inline h-3 w-3 mr-1" />
                    You can also assign project tasks to team members.
                  </span>
                )}
              </>
            ) : (
              <>
                {isPersonalTask 
                  ? "Create a personal task that's not associated with any project."
                  : "Create a new task for this project."
                }
                {canAssignToOthers && (
                  <span className="block mt-1 text-sm text-blue-600">
                    <Crown className="inline h-3 w-3 mr-1" />
                    As a manager, you can assign this task to team members.
                  </span>
                )}
              </>
            )}
          </DialogDescription>
        </DialogHeader>
        
        <ScrollArea className="flex-1 pr-6">
          <form onSubmit={handleSubmit} className="space-y-4 pb-4">
          <div className="space-y-2">
            <Label htmlFor="title">Title *</Label>
            <Input
              id="title"
              placeholder="Enter task title"
              value={formData.title}
              onChange={(e) => setFormData(prev => ({ ...prev, title: e.target.value }))}
              required
            />
          </div>

          {/* Project Selection - Only show for managers when not called from specific project */}
          {canSelectProject && (
            <div className="space-y-2">
              <Label htmlFor="project">Project</Label>
              <Select
                value={selectedProjectId?.toString() || '0'}
                onValueChange={(value) => {
                  const projectId = value === "0" ? null : parseInt(value);
                  setSelectedProjectId(projectId);
                  setSelectedAssignee(null); // Reset assignee when project changes
                  setProjectMembers([]); // Clear project members
                }}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Select project or create personal task" />
                </SelectTrigger>
                <SelectContent>
                  {availableProjects.map((project) => (
                    <SelectItem key={project.id} value={project.id.toString()}>
                      <div className="flex items-center gap-2">
                        <span>📁</span>
                        <span>{project.name}</span>
                      </div>
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              <p className="text-xs text-muted-foreground">
                {isPersonalTask 
                  ? "This will be a personal task assigned to you only."
                  : "This task will be created for the selected project."
                }
              </p>
            </div>
          )}

          <div className="space-y-2">
            <Label htmlFor="description">Description</Label>
            <Textarea
              id="description"
              placeholder="Enter task description (optional)"
              value={formData.description}
              onChange={(e) => setFormData(prev => ({ ...prev, description: e.target.value }))}
              rows={3}
            />
          </div>

          {/* Task Assignment Section */}
          {isPersonalTask ? (
            <div className="space-y-2">
              <Label>Task Owner</Label>
              <div className="p-3 bg-muted rounded-md">
                <div className="flex items-center gap-2 text-sm">
                  <User className="h-4 w-4" />
                  <span>This personal task will be assigned to you: <strong>{currentUser?.fullName || currentUser?.email}</strong></span>
                </div>
              </div>
            </div>
          ) : canAssignToOthers ? (
            <div className="space-y-2">
              <Label htmlFor="assignee">Assign to *</Label>
              <Select
                value={selectedAssignee?.toString() || ''}
                onValueChange={(value) => setSelectedAssignee(value ? parseInt(value) : null)}
                disabled={loadingMembers}
              >
                <SelectTrigger>
                  <SelectValue placeholder={loadingMembers ? "Loading team members..." : "Select team member"} />
                </SelectTrigger>
                <SelectContent>
                  {/* Option to assign to self */}
                                    {/* Option to assign to self */}
                  <SelectItem value={currentUserId.toString()}>
                    <div className="flex items-center gap-2">
                      <User className="h-4 w-4" />
                      <span>Myself ({currentUser?.fullName || currentUser?.email})</span>
                    </div>
                  </SelectItem>
                  
                  {/* Project team members */}
                  {projectMembers.map((member) => {
                    const isCurrentUser = member.id === currentUserId;
                    if (isCurrentUser) return null; // Don't duplicate the current user
                    
                    return (
                      <SelectItem key={member.id} value={member.id.toString()}>
                        <div className="flex items-center gap-2">
                          <User className="h-4 w-4" />
                          <span>{member.username}</span>
                          <span className="text-xs">{member.email}</span>
                        </div>
                      </SelectItem>
                    );
                  })}
                </SelectContent>
              </Select>
              {loadingMembers && (
                <p className="text-xs text-muted-foreground">
                  Loading project team members...
                </p>
              )}
              {!loadingMembers && projectMembers.length === 0 && selectedProjectId && (
                <p className="text-xs text-muted-foreground">
                  No other team members found for this project. Task will be assigned to you.
                </p>
              )}
            </div>
          ) : !isPersonalTask ? (
            <div className="space-y-2">
              <Label>Task Owner</Label>
              <div className="p-3 bg-muted rounded-md">
                <div className="flex items-center gap-2 text-sm">
                  <User className="h-4 w-4" />
                  <span>This project task will be assigned to you: <strong>{currentUser?.fullName || currentUser?.email}</strong></span>
                </div>
              </div>
            </div>
          ) : null}

          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="taskType">Task Type *</Label>
              <Select
                value={formData.taskType}
                onValueChange={(value: any) => setFormData(prev => ({ ...prev, taskType: value }))}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Select task type" />
                </SelectTrigger>
                <SelectContent>
                  {TASK_TYPES.map((type) => (
                    <SelectItem key={type.value} value={type.value}>
                      {type.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="status">Initial Status</Label>
              <Select
                value={formData.status}
                onValueChange={(value: any) => setFormData(prev => ({ ...prev, status: value }))}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Select status" />
                </SelectTrigger>
                <SelectContent>
                  {TASK_STATUSES.map((status) => (
                    <SelectItem key={status.value} value={status.value}>
                      {status.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </div>

          <div className="space-y-2">
            <Label htmlFor="priority">Priority</Label>
            <Select
              value={formData.priority?.toString() || '5'}
              onValueChange={(value) => setFormData(prev => ({ ...prev, priority: parseInt(value) }))}
            >
              <SelectTrigger>
                <SelectValue placeholder="Select priority" />
              </SelectTrigger>
              <SelectContent>
                {PRIORITY_OPTIONS.map((priority) => (
                  <SelectItem key={priority.value} value={priority.value.toString()}>
                    {priority.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            <p className="text-xs text-muted-foreground">
              1 = Lowest priority, 10 = Highest priority (Default: 5)
            </p>
          </div>

          <div className="space-y-2">
            <Label htmlFor="tags">Tags</Label>
            <Input
              id="tags"
              placeholder="Enter tags separated by commas (optional)"
              value={formData.tags?.join(', ') || ''}
              onChange={(e) => handleTagsChange(e.target.value)}
            />
            <p className="text-xs text-muted-foreground">
              Separate multiple tags with commas
            </p>
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
        </div>

        <div className="space-y-2">
          <Label htmlFor="dueDate">Due Date & Time</Label>
          
          <div className="relative">
            <Input
              id="dueDate"
              type="datetime-local"
              value={dueDate}
              onChange={(e) => {
                const selectedDate = e.target.value;
                // Validate that selected date is not in the past
                if (selectedDate && new Date(selectedDate) < new Date()) {
                  setError('Due date cannot be in the past');
                  return;
                }
                setError(null);
                setDueDate(selectedDate);
              }}
              min={getCurrentLocalDateTime()}
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
            Due date must be in the future
          </p>
        </div>

        {/* Task Recurrence input */}
        <div className="space-y-2">
          <RecurrenceSelector
            onChange={setRecurrenceData}
            initialValue={{
              isRecurring: false,
              recurrenceRuleStr: null,
              startDate: null,
              endDate: null,
            }}
          />
        </div>

        <div className="space-y-2">
          <Label>Collaborators</Label>
          {selectedCollaborators.length > 0 ? (
            <div className="flex flex-wrap gap-2">
              {selectedCollaborators.map((collaborator) => {
                const label =
                  collaborator.username ||
                  collaborator.email ||
                  `User ID: ${collaborator.id}`;

                return (
                  <Badge
                    key={collaborator.id}
                    variant="secondary"
                    className="flex items-center gap-2 px-2 py-1 text-xs"
                  >
                    <span className="font-medium">{label}</span>
                    <button
                      type="button"
                      className="p-0 leading-none text-muted-foreground hover:text-destructive"
                      onClick={() => handleRemoveSelectedCollaborator(collaborator.id)}
                      aria-label={`Remove collaborator ${label}`}
                    >
                      <X className="h-3 w-3" />
                    </button>
                  </Badge>
                );
              })}
              {missingCollaboratorIds.map((id) => (
                <Badge
                  key={`missing-${id}`}
                  variant="secondary"
                  className="flex items-center gap-2 px-2 py-1 text-xs"
                >
                  <span className="font-medium">User ID: {id}</span>
                  <button
                    type="button"
                    className="p-0 leading-none text-muted-foreground hover:text-destructive"
                    onClick={() => handleRemoveSelectedCollaborator(id)}
                    aria-label={`Remove collaborator with ID ${id}`}
                  >
                    <X className="h-3 w-3" />
                  </button>
                </Badge>
              ))}
            </div>
          ) : (
            <p className="text-sm text-muted-foreground">
              No collaborators selected. You can add collaborators now or after this task is created.
            </p>
          )}
          <div className="flex flex-wrap items-center gap-2">
            <Button
              type="button"
              variant="outline"
              onClick={() => setIsCollaboratorDialogOpen(true)}
              disabled={loadingCollaborators}
            >
              {loadingCollaborators ? (
                <>
                  <Loader2 className="h-4 w-4 animate-spin mr-2" />
                  Loading...
                </>
              ) : (
                "Select Collaborators"
              )}
            </Button>
            {selectedCollaboratorIds.length > 0 && (
              <Button
                type="button"
                variant="ghost"
                className="text-muted-foreground hover:text-destructive"
                onClick={handleCollaboratorSelectionClear}
              >
                Clear selection
              </Button>
            )}
          </div>
          {collaboratorsError && (
            <p className="text-xs text-destructive">{collaboratorsError}</p>
          )}
        </div>

        <div className="space-y-2">
          <Label htmlFor="attachments">Attachments</Label>
          <Input
            id="attachments"
            type="file"
              multiple
              onChange={e => setSelectedFiles(e.target.files)}
            />
            <p className="text-xs text-muted-foreground">
              You can select one or more files to upload as attachments.
            </p>
            {selectedFiles && selectedFiles.length > 0 && (
              <ul className="text-xs mt-1">
                {Array.from(selectedFiles).map((file, idx) => (
                  <li key={idx}>{file.name}</li>
                ))}
              </ul>
            )}
          </div>

          {error && (
            <div className="text-sm text-destructive">
              {error}
            </div>
          )}
          </form>
        </ScrollArea>

        <DialogFooter className="flex-shrink-0 pt-4 border-t">
          <Button
            type="button"
            variant="outline"
            onClick={() => onOpenChange(false)}
            disabled={loading}
          >
            Cancel
          </Button>
          <Button 
            type="submit" 
            disabled={loading}
            onClick={handleSubmit}
          >
            {loading ? 'Creating...' : 'Create Task'}
          </Button>
        </DialogFooter>
      </DialogContent>
      </Dialog>

      <Dialog
        modal={false}
        open={isCollaboratorDialogOpen}
        onOpenChange={setIsCollaboratorDialogOpen}
      >
        <DialogContent className="sm:max-w-[500px]">
          <DialogHeader>
            <DialogTitle>Select Collaborators</DialogTitle>
            <DialogDescription>
              Choose team members to collaborate on this task. Maximum 5 assignees allowed.
              {draftCollaboratorIds.length > 0 && (
                <span className="block mt-1 text-sm font-medium">
                  Selected: {draftCollaboratorIds.length} / 5
                </span>
              )}
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            {loadingCollaborators ? (
              <div className="flex items-center gap-2 text-sm text-muted-foreground">
                <Loader2 className="h-4 w-4 animate-spin" />
                Loading collaborators...
              </div>
            ) : collaboratorsError ? (
              <p className="text-sm text-destructive">{collaboratorsError}</p>
            ) : availableCollaborators.length > 0 ? (
              <ScrollArea className="max-h-[320px] pr-1">
                <div className="space-y-2">
                  {availableCollaborators.map((collaborator) => {
                    const isSelected = draftCollaboratorIds.includes(collaborator.id);
                    const isLimitReached = draftCollaboratorIds.length >= 5 && !isSelected;

                    return (
                      <Button
                        key={collaborator.id}
                        type="button"
                        variant={isSelected ? "default" : "outline"}
                        className="w-full justify-start gap-3"
                        onClick={() => toggleDraftCollaborator(collaborator.id)}
                        disabled={isLimitReached}
                      >
                        <span className="flex flex-col items-start">
                          <span className="text-sm font-medium">{collaborator.username}</span>
                          <span className="text-xs text-muted-foreground">ID: {collaborator.id} · {collaborator.email}</span>
                        </span>
                      </Button>
                    );
                  })}
                </div>
              </ScrollArea>
            ) : (
              <p className="text-sm text-muted-foreground">
                No collaborators available yet. You can still create the task and manage collaborators later.
              </p>
            )}
          </div>
          <DialogFooter>
            <Button
              type="button"
              variant="outline"
              onClick={() => setIsCollaboratorDialogOpen(false)}
            >
              Cancel
            </Button>
            <div className="flex items-center gap-2">
              {draftCollaboratorIds.length > 0 && (
                <Button
                  type="button"
                  variant="ghost"
                  className="text-muted-foreground hover:text-destructive"
                  onClick={handleCollaboratorSelectionClear}
                >
                  Clear All
                </Button>
              )}
              <Button type="button" onClick={handleCollaboratorSelectionSave}>
                Save selection
              </Button>
            </div>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
}
