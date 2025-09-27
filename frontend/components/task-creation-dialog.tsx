"use client";

import { useState } from "react";
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
import { fileService } from "@/services/file-service";

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
  projectId?: number; // If provided, create a project task; otherwise, create a personal task
  onTaskCreated?: (task: TaskResponse) => void;
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
  onTaskCreated 
}: TaskCreationDialogProps) {
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

  const isPersonalTask = !projectId;

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

      // For now, we'll use a dummy user ID. In a real app, this would come from auth context
      const userId = 1;
      const updatedProjectId = isPersonalTask ? 0 : projectId!;

      const taskData: CreateTaskRequest = {
        ...formData,
        ownerId: userId,
        title: formData.title!,
        taskType: formData.taskType!,
        projectId: updatedProjectId,
      };

      console.log('Creating task with data:', taskData);
      const createdTask = await projectService.createTask(taskData);
      console.log('Task created successfully:', createdTask);

      // Debug file upload logic
      console.log('Checking file upload conditions:');
      console.log('selectedFiles:', selectedFiles);
      console.log('selectedFiles?.length:', selectedFiles?.length);
      console.log('createdTask.id:', createdTask.id);
      console.log('createdTask.projectId:', createdTask.projectId);
      console.log('projectId:', updatedProjectId);

      // Upload files if any
      if (selectedFiles && createdTask.id) {
        console.log('Starting file upload...');
        try {
          await uploadFiles({
            files: selectedFiles,
            taskId: createdTask.id,
            projectId: createdTask.projectId ?? updatedProjectId,
          });
          console.log('File upload completed successfully');
        } catch (uploadError) {
          console.error('File upload failed:', uploadError);
          // Don't fail the entire task creation if file upload fails
          setError('Task created successfully, but file upload failed. Please try uploading files again.');
        }
      } else {
        console.log('Skipping file upload - conditions not met');
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
            Create New {isPersonalTask ? 'Personal' : 'Project'} Task
          </DialogTitle>
          <DialogDescription>
            {isPersonalTask 
              ? "Create a personal task that's not associated with any project."
              : "Create a new task for this project."
            }
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
