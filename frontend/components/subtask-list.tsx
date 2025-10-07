"use client";

import React, { useState } from "react";
import { Badge } from "@/components/ui/badge";
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
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
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
import { Plus, CheckCircle2, Circle, Clock, AlertCircle, Eye, Edit, Trash2 } from "lucide-react";
import { SubtaskResponse, CreateSubtaskRequest, projectService } from "@/services/project-service";
import { CommentSection } from "./comment-section";
import { SubtaskUpdateDialog } from "./subtask-update-dialog";
import { useToast } from "@/hooks/use-toast";

interface SubtaskListProps {
  taskId: number;
  projectId: number;
  subtasks: SubtaskResponse[];
  onSubtaskCreated: (subtask: SubtaskResponse) => void;
  onSubtaskUpdated: (subtask: SubtaskResponse) => void;
  onSubtaskDeleted?: (subtaskId: number) => void;
}

type SubtaskStatus = 'TODO' | 'IN_PROGRESS' | 'COMPLETED' | 'BLOCKED';
type TaskType = 'BUG' | 'FEATURE' | 'CHORE' | 'RESEARCH';

const statusIcons: Record<SubtaskStatus, React.ComponentType<{ className?: string }>> = {
  'TODO': Circle,
  'IN_PROGRESS': Clock,
  'COMPLETED': CheckCircle2,
  'BLOCKED': AlertCircle,
};

const statusColors: Record<SubtaskStatus, string> = {
  'TODO': "text-slate-500",
  'IN_PROGRESS': "text-blue-500", 
  'COMPLETED': "text-green-500",
  'BLOCKED': "text-amber-500",
};

const statusLabels: Record<SubtaskStatus, string> = {
  'TODO': "Todo",
  'IN_PROGRESS': "In Progress", 
  'COMPLETED': "Done",
  'BLOCKED': "Blocked",
};

export function SubtaskList({ taskId, projectId, subtasks, onSubtaskCreated, onSubtaskUpdated, onSubtaskDeleted }: SubtaskListProps) {
  const { toast } = useToast();
  const [showCreateDialog, setShowCreateDialog] = useState(false);
  const [showDetailDialog, setShowDetailDialog] = useState(false);
  const [showUpdateDialog, setShowUpdateDialog] = useState(false);
  const [showDeleteDialog, setShowDeleteDialog] = useState(false);
  const [selectedSubtask, setSelectedSubtask] = useState<SubtaskResponse | null>(null);
  const [subtaskToUpdate, setSubtaskToUpdate] = useState<SubtaskResponse | null>(null);
  const [subtaskToDelete, setSubtaskToDelete] = useState<number | null>(null);
  const [isCreating, setIsCreating] = useState(false);
  const [newSubtask, setNewSubtask] = useState<Partial<CreateSubtaskRequest>>({
    taskId,
    projectId,
    title: "",
    details: "",
    status: "TODO",
    taskType: "FEATURE",
  });

  const handleCreateSubtask = async () => {
    if (!newSubtask.title?.trim()) return;

    setIsCreating(true);
    try {
      const subtaskData: CreateSubtaskRequest = {
        taskId,
        projectId,
        title: newSubtask.title,
        details: newSubtask.details,
        status: newSubtask.status || "TODO",
        taskType: newSubtask.taskType || "FEATURE",
      };

      // For now, use a dummy user ID. In a real app, this would come from auth context
      const createdSubtask = await projectService.createSubtask(subtaskData);
      
      onSubtaskCreated(createdSubtask);
      setShowCreateDialog(false);
      setNewSubtask({
        taskId,
        projectId,
        title: "",
        details: "",
        status: "TODO",
        taskType: "FEATURE",
      });
    } catch (error) {
      console.error('Error creating subtask:', error);
    } finally {
      setIsCreating(false);
    }
  };

  const handleStatusChange = async (subtaskId: number, newStatus: string) => {
    try {
      const updatedSubtask = await projectService.updateSubtask(
        subtaskId,
        { status: newStatus as SubtaskStatus },
      );
      onSubtaskUpdated(updatedSubtask);
    } catch (error) {
      console.error('Error updating subtask:', error);
    }
  };

  const handleViewDetails = (subtask: SubtaskResponse) => {
    setSelectedSubtask(subtask);
    setShowDetailDialog(true);
  };

  const handleEditSubtask = (subtask: SubtaskResponse) => {
    setSubtaskToUpdate(subtask);
    setShowUpdateDialog(true);
  };

  const handleSubtaskUpdatedFromDialog = (updatedSubtask: SubtaskResponse) => {
    onSubtaskUpdated(updatedSubtask);
    setShowUpdateDialog(false);
    setSubtaskToUpdate(null);

    // Also update the selected subtask if it's the same one
    if (selectedSubtask?.id === updatedSubtask.id) {
      setSelectedSubtask(updatedSubtask);
    }
  };

  const handleDeleteClick = (subtaskId: number) => {
    setSubtaskToDelete(subtaskId);
    setShowDeleteDialog(true);
  };

  const handleDeleteConfirm = async () => {
    if (!subtaskToDelete) return;

    try {
      await projectService.deleteSubtask(subtaskToDelete);
      
      toast({
        title: "Subtask deleted",
        description: "The subtask has been successfully deleted.",
      });

      setShowDeleteDialog(false);
      setSubtaskToDelete(null);

      // Notify parent to remove from state
      if (onSubtaskDeleted) {
        onSubtaskDeleted(subtaskToDelete);
      }
    } catch (error) {
      console.error('Error deleting subtask:', error);
      toast({
        title: "Failed to delete subtask",
        description: error instanceof Error ? error.message : "An error occurred while deleting the subtask.",
        variant: "destructive",
      });
      setShowDeleteDialog(false);
      setSubtaskToDelete(null);
    }
  };

  if (subtasks.length === 0 && !showCreateDialog) {
    return (
      <div className="space-y-2">
        <div className="flex items-center justify-between">
          <h4 className="text-sm font-medium">Subtasks</h4>
          <Dialog open={showCreateDialog} onOpenChange={setShowCreateDialog}>
            <DialogTrigger asChild>
              <Button variant="outline" size="sm">
                <Plus className="h-4 w-4 mr-1" />
                Add Subtask
              </Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>Create Subtask</DialogTitle>
                <DialogDescription>
                  Add a new subtask to break down this task into smaller pieces.
                </DialogDescription>
              </DialogHeader>
              <CreateSubtaskForm 
                newSubtask={newSubtask}
                setNewSubtask={setNewSubtask}
                onSubmit={handleCreateSubtask}
                isCreating={isCreating}
              />
            </DialogContent>
          </Dialog>
        </div>
        <p className="text-sm text-muted-foreground">No subtasks yet</p>
      </div>
    );
  }

  const completedCount = subtasks.filter(s => s.status === 'COMPLETED').length;
  const progress = subtasks.length > 0 ? Math.round((completedCount / subtasks.length) * 100) : 0;

  return (
    <div className="space-y-3">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <h4 className="text-sm font-medium">Subtasks</h4>
          <Badge variant="outline" className="text-xs">
            {completedCount}/{subtasks.length}
          </Badge>
        </div>
        <Dialog open={showCreateDialog} onOpenChange={setShowCreateDialog}>
          <DialogTrigger asChild>
            <Button variant="outline" size="sm">
              <Plus className="h-4 w-4 mr-1" />
              Add
            </Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Create Subtask</DialogTitle>
              <DialogDescription>
                Add a new subtask to break down this task into smaller pieces.
              </DialogDescription>
            </DialogHeader>
            <CreateSubtaskForm 
              newSubtask={newSubtask}
              setNewSubtask={setNewSubtask}
              onSubmit={handleCreateSubtask}
              isCreating={isCreating}
            />
          </DialogContent>
        </Dialog>
      </div>

      {/* Progress bar */}
      <div className="space-y-1">
        <div className="flex justify-between text-xs text-muted-foreground">
          <span>{progress}% complete</span>
          <span>{completedCount} of {subtasks.length} done</span>
        </div>
        <div className="h-2 w-full rounded-full bg-muted">
          <div
            className="h-2 rounded-full bg-primary transition-all"
            style={{ width: `${progress}%` }}
          />
        </div>
      </div>

      {/* Subtask list */}
      <div className="space-y-2">
        {subtasks.map((subtask) => {
          const StatusIcon = statusIcons[subtask.status as SubtaskStatus];
          return (
            <div
              key={subtask.id}
              className="flex items-center gap-3 p-2 rounded-lg border bg-card hover:bg-accent/50 transition-colors"
            >
              <button
                onClick={() => {
                  const newStatus = subtask.status === 'COMPLETED' ? 'TODO' : 'COMPLETED';
                  handleStatusChange(subtask.id, newStatus);
                }}
                className={`${statusColors[subtask.status as SubtaskStatus]} hover:scale-110 transition-transform`}
              >
                <StatusIcon className="h-4 w-4" />
              </button>
              
              <div className="flex-1 min-w-0">
                <p className={`text-sm font-medium ${subtask.status === 'COMPLETED' ? 'line-through text-muted-foreground' : ''}`}>
                  {subtask.title}
                </p>
                {subtask.details && (
                  <p className="text-xs text-muted-foreground line-clamp-1">
                    {subtask.details}
                  </p>
                )}
              </div>

              <div className="flex items-center gap-1">
                {subtask.userHasEditAccess && (
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={(e) => {
                      e.stopPropagation();
                      handleEditSubtask(subtask);
                    }}
                    className="h-8 w-8 p-0 hover:bg-accent"
                    title="Edit subtask"
                  >
                    <Edit className="h-3.5 w-3.5" />
                  </Button>
                )}
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={(e) => {
                    e.stopPropagation();
                    handleViewDetails(subtask);
                  }}
                  className="h-8 w-8 p-0 hover:bg-accent"
                  title="View details and comments"
                >
                  <Eye className="h-4 w-4" />
                </Button>
                {subtask.userHasDeleteAccess && (
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={(e) => {
                      e.stopPropagation();
                      handleDeleteClick(subtask.id);
                    }}
                    className="h-8 w-8 p-0 hover:bg-destructive hover:text-destructive-foreground"
                    title="Delete subtask"
                  >
                    <Trash2 className="h-3.5 w-3.5" />
                  </Button>
                )}
                <Badge variant="outline" className="text-xs ml-1">
                  {statusLabels[subtask.status as SubtaskStatus]}
                </Badge>
              </div>
            </div>
          );
        })}
      </div>

      {/* Subtask Detail Dialog */}
      {selectedSubtask && (
        <Dialog open={showDetailDialog} onOpenChange={setShowDetailDialog}>
          <DialogContent className="max-w-5xl max-h-[90vh] overflow-hidden flex flex-col">
            <DialogHeader>
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <div className={statusColors[selectedSubtask.status as SubtaskStatus]}>
                    {React.createElement(statusIcons[selectedSubtask.status as SubtaskStatus], { className: "h-5 w-5" })}
                  </div>
                  <DialogTitle className="mb-0">{selectedSubtask.title}</DialogTitle>
                </div>
                <div className="flex gap-2">
                  {selectedSubtask.userHasEditAccess && (
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => {
                        handleEditSubtask(selectedSubtask);
                        setShowDetailDialog(false);
                      }}
                    >
                      <Edit className="h-4 w-4 mr-1" />
                      Edit
                    </Button>
                  )}
                  {selectedSubtask.userHasDeleteAccess && (
                    <Button
                      variant="destructive"
                      size="sm"
                      onClick={() => {
                        handleDeleteClick(selectedSubtask.id);
                        setShowDetailDialog(false);
                      }}
                    >
                      <Trash2 className="h-4 w-4 mr-1" />
                      Delete
                    </Button>
                  )}
                </div>
              </div>
              <DialogDescription>
                Subtask Details and Comments
              </DialogDescription>
            </DialogHeader>

            <div className="flex-1 overflow-y-auto space-y-6 pr-2">
              {/* Subtask Information */}
              <div className="space-y-4">
                <div className="flex items-center gap-4">
                  <Badge variant="outline">
                    {statusLabels[selectedSubtask.status as SubtaskStatus]}
                  </Badge>
                  <Badge variant="secondary">
                    {selectedSubtask.taskType}
                  </Badge>
                </div>

                {selectedSubtask.details && (
                  <div>
                    <h4 className="text-sm font-medium mb-2">Description</h4>
                    <p className="text-sm text-muted-foreground whitespace-pre-wrap">
                      {selectedSubtask.details}
                    </p>
                  </div>
                )}

                <div className="text-xs text-muted-foreground">
                  Created: {new Date(selectedSubtask.createdAt).toLocaleString()}
                  {selectedSubtask.updatedAt && selectedSubtask.updatedAt !== selectedSubtask.createdAt && (
                    <> • Updated: {new Date(selectedSubtask.updatedAt).toLocaleString()}</>
                  )}
                </div>
              </div>

              {/* Comments Section */}
              <div className="border-t pt-4">
                <CommentSection
                  subtaskId={selectedSubtask.id}
                  projectId={projectId}
                  title="Subtask Comments"
                  showTitle={true}
                  compact={false}
                />
              </div>
            </div>
          </DialogContent>
        </Dialog>
      )}

      {/* Subtask Update Dialog */}
      {subtaskToUpdate && (
        <SubtaskUpdateDialog
          subtask={subtaskToUpdate}
          open={showUpdateDialog}
          onOpenChange={setShowUpdateDialog}
          onSubtaskUpdated={handleSubtaskUpdatedFromDialog}
        />
      )}

      {/* Delete Confirmation Dialog */}
      <AlertDialog open={showDeleteDialog} onOpenChange={setShowDeleteDialog}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Delete Subtask</AlertDialogTitle>
            <AlertDialogDescription>
              Are you sure you want to delete this subtask? This action cannot be undone.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction
              onClick={handleDeleteConfirm}
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
            >
              Delete
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}

interface CreateSubtaskFormProps {
  newSubtask: Partial<CreateSubtaskRequest>;
  setNewSubtask: (subtask: Partial<CreateSubtaskRequest>) => void;
  onSubmit: () => void;
  isCreating: boolean;
}

function CreateSubtaskForm({ newSubtask, setNewSubtask, onSubmit, isCreating }: CreateSubtaskFormProps) {
  return (
    <div className="space-y-4">
      <div className="space-y-2">
        <Label htmlFor="title">Title</Label>
        <Input
          id="title"
          placeholder="Enter subtask title..."
          value={newSubtask.title || ""}
          onChange={(e) => setNewSubtask({ ...newSubtask, title: e.target.value })}
        />
      </div>

      <div className="space-y-2">
        <Label htmlFor="details">Details (optional)</Label>
        <Textarea
          id="details"
          placeholder="Add any additional details..."
          value={newSubtask.details || ""}
          onChange={(e) => setNewSubtask({ ...newSubtask, details: e.target.value })}
          rows={3}
        />
      </div>

      <div className="grid grid-cols-2 gap-4">
        <div className="space-y-2">
          <Label htmlFor="status">Status</Label>
          <Select
            value={newSubtask.status}
            onValueChange={(value: string) => setNewSubtask({ ...newSubtask, status: value as SubtaskStatus })}
          >
            <SelectTrigger>
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="TODO">Todo</SelectItem>
              <SelectItem value="IN_PROGRESS">In Progress</SelectItem>
              <SelectItem value="COMPLETED">Done</SelectItem>
              <SelectItem value="BLOCKED">Blocked</SelectItem>
            </SelectContent>
          </Select>
        </div>

        <div className="space-y-2">
          <Label htmlFor="taskType">Type</Label>
          <Select
            value={newSubtask.taskType}
            onValueChange={(value: string) => setNewSubtask({ ...newSubtask, taskType: value as TaskType })}
          >
            <SelectTrigger>
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="FEATURE">Feature</SelectItem>
              <SelectItem value="BUG">Bug</SelectItem>
              <SelectItem value="CHORE">Chore</SelectItem>
              <SelectItem value="RESEARCH">Research</SelectItem>
            </SelectContent>
          </Select>
        </div>
      </div>

      <div className="flex justify-end gap-2">
        <Button variant="outline" onClick={() => setNewSubtask({ taskId: newSubtask.taskId, projectId: newSubtask.projectId, title: "", details: "", status: "TODO", taskType: "FEATURE" })}>
          Cancel
        </Button>
        <Button onClick={onSubmit} disabled={isCreating || !newSubtask.title?.trim()}>
          {isCreating ? "Creating..." : "Create Subtask"}
        </Button>
      </div>
    </div>
  );
}