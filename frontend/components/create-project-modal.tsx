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
import { ScrollArea } from "@/components/ui/scroll-area";
import { Badge } from "@/components/ui/badge";
import { projectService, ProjectResponse } from "@/services/project-service";
import { userManagementService } from "@/services/user-management-service";
import { useCurrentUser } from "@/contexts/user-context";
import { UserResponseDto } from "@/types/user";
import { Loader2, X, Users } from "lucide-react";

interface CreateProjectModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onProjectCreated?: (project: ProjectResponse) => void;
}

export function CreateProjectModal({
  open,
  onOpenChange,
  onProjectCreated,
}: CreateProjectModalProps) {
  const { currentUser } = useCurrentUser();
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [selectedUserIds, setSelectedUserIds] = useState<number[]>([]);
  const [allUsers, setAllUsers] = useState<UserResponseDto[]>([]);
  const [loading, setLoading] = useState(false);
  const [loadingUsers, setLoadingUsers] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Load all users when modal opens
  useEffect(() => {
    if (open) {
      loadUsers();
    }
  }, [open]);

  const loadUsers = async () => {
    setLoadingUsers(true);
    try {
      const users = await userManagementService.getAllUsers();
      setAllUsers(users);
    } catch (err) {
      console.error("Error loading users:", err);
      setError("Failed to load users");
    } finally {
      setLoadingUsers(false);
    }
  };

  // Filter users to show only those from other departments
  const usersFromOtherDepartments = allUsers.filter(
    (user) =>
      user.isActive &&
      user.id !== currentUser?.backendStaffId &&
      user.department !== currentUser?.department
  );

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      const project = await projectService.createProject({
        name,
        description: description || undefined,
        additionalMemberIds: selectedUserIds.length > 0 ? selectedUserIds : undefined,
      });

      // Reset form
      setName("");
      setDescription("");
      setSelectedUserIds([]);
      setError(null);

      // Notify parent and close modal
      onProjectCreated?.(project);
      onOpenChange(false);
    } catch (err) {
      console.error("Error creating project:", err);
      setError(err instanceof Error ? err.message : "Failed to create project");
    } finally {
      setLoading(false);
    }
  };

  const toggleUserSelection = (userId: number) => {
    setSelectedUserIds((prev) =>
      prev.includes(userId)
        ? prev.filter((id) => id !== userId)
        : [...prev, userId]
    );
  };

  const removeSelectedUser = (userId: number) => {
    setSelectedUserIds((prev) => prev.filter((id) => id !== userId));
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Create New Project</DialogTitle>
          <DialogDescription>
            All active members from your department will be automatically added to this project.
            You can also add members from other departments below.
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-4">
          {/* Project Name */}
          <div className="space-y-2">
            <Label htmlFor="project-name">
              Project Name <span className="text-red-500">*</span>
            </Label>
            <Input
              id="project-name"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="Enter project name"
              required
              disabled={loading}
            />
          </div>

          {/* Description */}
          <div className="space-y-2">
            <Label htmlFor="project-description">Description</Label>
            <Textarea
              id="project-description"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Enter project description (optional)"
              rows={3}
              disabled={loading}
            />
          </div>

          {/* Current Department Info */}
          {currentUser?.department && (
            <div className="rounded-lg bg-blue-50 p-3 text-sm text-blue-800">
              <div className="flex items-center gap-2">
                <Users className="h-4 w-4" />
                <span>
                  All active members from <strong>{currentUser.department}</strong> will be added automatically
                </span>
              </div>
            </div>
          )}

          {/* Additional Members Section */}
          <div className="space-y-2">
            <Label>Additional Members (from other departments)</Label>

            {/* Selected Users */}
            {selectedUserIds.length > 0 && (
              <div className="flex flex-wrap gap-2 mb-2">
                {selectedUserIds.map((userId) => {
                  const user = allUsers.find((u) => u.id === userId);
                  return user ? (
                    <Badge key={userId} variant="secondary" className="flex items-center gap-1">
                      {user.username}
                      {user.department && (
                        <span className="text-xs text-muted-foreground">
                          ({user.department})
                        </span>
                      )}
                      <button
                        type="button"
                        onClick={() => removeSelectedUser(userId)}
                        className="ml-1 rounded-full hover:bg-gray-300"
                      >
                        <X className="h-3 w-3" />
                      </button>
                    </Badge>
                  ) : null;
                })}
              </div>
            )}

            {/* User List */}
            {loadingUsers ? (
              <div className="flex items-center justify-center py-8">
                <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
              </div>
            ) : usersFromOtherDepartments.length === 0 ? (
              <div className="text-sm text-muted-foreground py-4 text-center">
                No users from other departments available
              </div>
            ) : (
              <ScrollArea className="h-[200px] rounded-md border p-3">
                <div className="space-y-2">
                  {usersFromOtherDepartments.map((user) => (
                    <div
                      key={user.id}
                      className="flex items-center space-x-2 p-2 rounded hover:bg-gray-100 cursor-pointer"
                      onClick={() => toggleUserSelection(user.id)}
                    >
                      <input
                        type="checkbox"
                        checked={selectedUserIds.includes(user.id)}
                        onChange={() => toggleUserSelection(user.id)}
                        className="h-4 w-4 rounded border-gray-300"
                      />
                      <div className="flex-1">
                        <div className="text-sm font-medium">{user.username}</div>
                        <div className="text-xs text-muted-foreground">
                          {user.email}
                          {user.department && ` • ${user.department}`}
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </ScrollArea>
            )}
          </div>

          {/* Error Message */}
          {error && (
            <div className="rounded-lg bg-red-50 p-3 text-sm text-red-800">
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
            <Button type="submit" disabled={loading || !name.trim()}>
              {loading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
              Create Project
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
