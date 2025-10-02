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
import { projectService, SubtaskResponse, UpdateSubtaskRequest } from "@/services/project-service";

interface SubtaskUpdateDialogProps {
  subtask: SubtaskResponse;
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSubtaskUpdated: (updatedSubtask: SubtaskResponse) => void;
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

export function SubtaskUpdateDialog({
  subtask,
  open,
  onOpenChange,
  onSubtaskUpdated,
}: SubtaskUpdateDialogProps) {
  const [title, setTitle] = useState(subtask.title);
  const [details, setDetails] = useState(subtask.details || '');
  const [status, setStatus] = useState(subtask.status);
  const [taskType, setTaskType] = useState(subtask.taskType);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setIsSubmitting(true);

    try {
      // Build update request with only changed fields
      const updateRequest: UpdateSubtaskRequest = {};

      if (title !== subtask.title) {
        updateRequest.title = title;
      }

      if (details !== (subtask.details || '')) {
        updateRequest.details = details;
      }

      if (status !== subtask.status) {
        updateRequest.status = status;
      }

      if (taskType !== subtask.taskType) {
        updateRequest.taskType = taskType;
      }

      // Only make API call if something changed
      if (Object.keys(updateRequest).length === 0) {
        onOpenChange(false);
        return;
      }

      const updatedSubtask = await projectService.updateSubtask(
        subtask.id,
        updateRequest,
      );
      onSubtaskUpdated(updatedSubtask);
      onOpenChange(false);
    } catch (err) {
      console.error('Error updating subtask:', err);
      setError(err instanceof Error ? err.message : 'Failed to update subtask');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Update Subtask</DialogTitle>
          <DialogDescription>
            Modify subtask details. Only changed fields will be updated.
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="title">Title *</Label>
            <Input
              id="title"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="Subtask title"
              required
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="details">Details</Label>
            <Textarea
              id="details"
              value={details}
              onChange={(e) => setDetails(e.target.value)}
              placeholder="Add additional details..."
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
              {isSubmitting ? 'Updating...' : 'Update Subtask'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}