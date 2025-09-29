"use client";

import { useState, useEffect } from "react";
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
import { projectService, CreateTaskRequest, TaskResponse } from "@/services/project-service";
import { userManagementService } from "@/services/user-management-service";
import { fileService } from "@/services/file-service";
import { useCurrentUser } from "@/contexts/user-context";
import { UserResponseDto } from "@/types/user";
import { User, Crown } from "lucide-react";

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

export function TaskCreationDialog({ 
  open, 
  onOpenChange, 
  projectId, 
  onTaskCreated,
  availableProjects = []
}: TaskCreationDialogProps) {
  const { currentUser } = useCurrentUser();
  const [formData, setFormData] = useState<Partial<CreateTaskRequest>>({
    title: '',
    description: '',
    taskType: 'FEATURE',
    status: 'TODO',
    tags: [],
    assignedUserIds: [],
    projectId,
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedFiles, setSelectedFiles] = useState<FileList | null>(null);
  
  // New state for task assignment and project selection
  const [projectMembers, setProjectMembers] = useState<UserResponseDto[]>([]);
  const [loadingMembers, setLoadingMembers] = useState(false);
  const [selectedAssignee, setSelectedAssignee] = useState<number | null>(null);
  const [selectedProjectId, setSelectedProjectId] = useState<number | null>(projectId || null);

  const isManager = currentUser?.role === 'MANAGER';
  const isPersonalTask = selectedProjectId === null || selectedProjectId === 0;
  const canAssignToOthers = isManager && !isPersonalTask; // Only managers can assign project tasks to others
  const canSelectProject = isManager && !projectId; // Only managers can select project when not called from specific project

  // Get the current user's ID consistently
  const currentUserId = currentUser?.backendStaffId || parseInt(currentUser?.id || '1');

  // Load project members when dialog opens and projectId is available
  useEffect(() => {
    if (open && selectedProjectId && canAssignToOthers) {
      loadProjectMembers();
    }
  }, [open, selectedProjectId, canAssignToOthers]);

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

    try {
      setLoading(true);
      setError(null);

      // Determine the owner ID and which endpoint to use
      const assigneeId = selectedAssignee || currentUserId;
      const shouldUseManagerEndpoint = canAssignToOthers && selectedAssignee && selectedAssignee !== currentUserId;

      const taskData: CreateTaskRequest = {
        ...formData,
        ownerId: assigneeId,
        title: formData.title!,
        taskType: formData.taskType!,
        projectId: isPersonalTask ? undefined : selectedProjectId!,
      };

      console.log('Creating task with data:', taskData);
      console.log('Using manager endpoint:', shouldUseManagerEndpoint);

      let createdTask: TaskResponse;
      
      if (shouldUseManagerEndpoint) {
        // Use the manager endpoint that allows specifying owner
        createdTask = await projectService.createTaskWithSpecifiedOwner(taskData);
      } else {
        // Use the regular endpoint where the current user becomes the owner
        createdTask = await projectService.createTask(taskData);
      }
      
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
      });
      setSelectedFiles(null);
      setSelectedAssignee(null);
      setSelectedProjectId(projectId || null);
      setError(null);
    } catch (err) {
      console.error('Error creating task:', err);
      setError('Failed to create task. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleTagsChange = (value: string) => {
    const tags = value.split(',').map(tag => tag.trim()).filter(Boolean);
    setFormData(prev => ({ ...prev, tags }));
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[525px]">
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
        
        <form onSubmit={handleSubmit} className="space-y-4">
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
                  <SelectItem value="0">
                    <div className="flex items-center gap-2">
                      <User className="h-4 w-4" />
                      <span>Personal Task (No Project)</span>
                    </div>
                  </SelectItem>
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
                          <span>{member.fullName}</span>
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

          <DialogFooter>
            <Button
              type="button"
              variant="outline"
              onClick={() => onOpenChange(false)}
              disabled={loading}
            >
              Cancel
            </Button>
            <Button type="submit" disabled={loading}>
              {loading ? 'Creating...' : 'Create Task'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}