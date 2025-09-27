"use client";

import { useState } from "react";
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
import { UserPlus, UserMinus, Users as UsersIcon, Loader2 } from "lucide-react";
import { createAuthenticatedRequestConfig } from "@/lib/auth-utils";

interface TaskCollaboratorManagementProps {
  taskId: number;
  currentCollaboratorIds: number[];
  currentUserId: number;
  onCollaboratorsChange?: (collaboratorIds: number[]) => void;
}

export function TaskCollaboratorManagement({
  taskId,
  currentCollaboratorIds,
  currentUserId,
  onCollaboratorsChange,
}: TaskCollaboratorManagementProps) {
  const [isOpen, setIsOpen] = useState(false);
  const [selectedUserId, setSelectedUserId] = useState<string>("");
  const [isLoading, setIsLoading] = useState(false);

  // Simple list of user IDs for testing (1-20)
  const availableUserIds = Array.from({ length: 20 }, (_, i) => i + 1);

  const handleAddCollaborator = async () => {
    if (!selectedUserId) return;

    const userId = parseInt(selectedUserId);
    const payload = { taskId, collaboratorId: userId, assignedById: currentUserId };

    setIsLoading(true);
    console.log("Adding collaborator with payload:", payload);

    try {
      const apiBaseUrl = process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080";
      const endpoint = `${apiBaseUrl}/api/tasks/collaborator`;

      console.log("Making authenticated POST request to:", endpoint);

      // Get authenticated request config
      const requestConfig = await createAuthenticatedRequestConfig("POST", payload);
      console.log("Request headers:", requestConfig.headers);

      const response = await fetch(endpoint, requestConfig);

      console.log("Response status:", response.status);
      console.log("Response headers:", Object.fromEntries(response.headers.entries()));

      if (!response.ok) {
        const errorText = await response.text();
        console.error("Error response:", errorText);
        throw new Error(`HTTP ${response.status}: ${errorText}`);
      }

      const result = await response.json();
      console.log("Success response:", result);

      toast({
        title: "Collaborator added",
        description: `User ID ${userId} has been successfully added to the task.`,
      });

      if (onCollaboratorsChange) {
        onCollaboratorsChange([...currentCollaboratorIds, userId]);
      }
      setSelectedUserId("");

    } catch (error) {
      console.error("Add collaborator error:", error);
      toast({
        title: "Error adding collaborator",
        description: error instanceof Error ? error.message : "Failed to add collaborator to the task.",
        variant: "destructive",
      });
    } finally {
      setIsLoading(false);
    }
  };

  const handleRemoveCollaborator = async (collaboratorId: number) => {
    const payload = { taskId, collaboratorId, assignedById: currentUserId };

    setIsLoading(true);
    console.log("Removing collaborator with payload:", payload);

    try {
      const apiBaseUrl = process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080";
      const endpoint = `${apiBaseUrl}/api/tasks/collaborator`;

      console.log("Making authenticated DELETE request to:", endpoint);

      // Get authenticated request config
      const requestConfig = await createAuthenticatedRequestConfig("DELETE", payload);
      console.log("Request headers:", requestConfig.headers);

      const response = await fetch(endpoint, requestConfig);

      console.log("Response status:", response.status);

      if (!response.ok) {
        const errorText = await response.text();
        console.error("Error response:", errorText);
        throw new Error(`HTTP ${response.status}: ${errorText}`);
      }

      console.log("Remove collaborator success");

      toast({
        title: "Collaborator removed",
        description: `User ID ${collaboratorId} has been successfully removed from the task.`,
      });

      if (onCollaboratorsChange) {
        onCollaboratorsChange(currentCollaboratorIds.filter(id => id !== collaboratorId));
      }

    } catch (error) {
      console.error("Remove collaborator error:", error);
      toast({
        title: "Error removing collaborator",
        description: error instanceof Error ? error.message : "Failed to remove collaborator from the task.",
        variant: "destructive",
      });
    } finally {
      setIsLoading(false);
    }
  };

  const getAvailableUserIds = () => {
    return availableUserIds.filter(userId =>
      !currentCollaboratorIds.includes(userId) &&
      userId !== currentUserId
    );
  };

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
            Add or remove collaborators for this task.
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-4">
          {/* Current Collaborators */}
          <div>
            <h4 className="text-sm font-medium mb-3">Current Collaborators</h4>
            {currentCollaboratorIds.length > 0 ? (
              <div className="space-y-2">
                {currentCollaboratorIds.map((collaboratorId) => (
                  <div key={collaboratorId} className="flex items-center justify-between py-2 px-3 border rounded-lg">
                    <div className="flex items-center gap-2">
                      <div className="flex h-8 w-8 items-center justify-center rounded-full bg-secondary text-sm font-semibold text-secondary-foreground">
                        {collaboratorId}
                      </div>
                      <span className="text-sm font-medium">User ID: {collaboratorId}</span>
                    </div>
                    <Button
                      variant="ghost"
                      size="sm"
                      className="h-8 w-8 p-0 text-muted-foreground hover:text-destructive"
                      onClick={() => handleRemoveCollaborator(collaboratorId)}
                      disabled={isLoading}
                    >
                      <UserMinus className="h-4 w-4" />
                    </Button>
                  </div>
                ))}
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
                <SelectTrigger>
                  <SelectValue placeholder="Select a user ID to add" />
                </SelectTrigger>
                <SelectContent>
                  {getAvailableUserIds().map((userId) => (
                    <SelectItem key={userId} value={userId.toString()}>
                      User ID: {userId}
                    </SelectItem>
                  ))}
                  {getAvailableUserIds().length === 0 && (
                    <SelectItem value="no-users" disabled>
                      No users available to add
                    </SelectItem>
                  )}
                </SelectContent>
              </Select>

              <Button
                onClick={handleAddCollaborator}
                disabled={!selectedUserId || selectedUserId === "no-users" || isLoading}
                className="w-full"
              >
                {isLoading ? (
                  <>
                    <Loader2 className="h-4 w-4 animate-spin mr-2" />
                    Adding...
                  </>
                ) : (
                  <>
                    <UserPlus className="h-4 w-4 mr-2" />
                    Add Collaborator
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