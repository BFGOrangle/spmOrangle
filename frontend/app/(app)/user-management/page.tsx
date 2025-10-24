"use client";

import React, { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Separator } from "@/components/ui/separator";
import { SidebarInset, SidebarTrigger } from "@/components/ui/sidebar";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { toast } from "sonner";
import {
  Loader2,
  UserPlus,
  UserX,
  Shield,
  Search,
  RefreshCcw,
  Users,
} from "lucide-react";
import { useCurrentUser } from "@/contexts/user-context";
import { userManagementService } from "@/services/user-management-service";
import { UserResponseDto, CreateUserDto, UpdateUserRoleDto } from "@/types/user";
import {
  BaseApiError,
  BaseValidationError,
} from "@/services/authenticated-api-client";
import { Route } from "@/enums/Route";
import FullPageSpinnerLoader from "@/components/full-page-spinner-loader";

export default function UserManagementPage() {
  const [users, setUsers] = useState<UserResponseDto[]>([]);
  const [filteredUsers, setFilteredUsers] = useState<UserResponseDto[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [searchQuery, setSearchQuery] = useState<string>("");
  const [userTypes, setUserTypes] = useState<string[]>([]);

  // Create user dialog state
  const [createDialogOpen, setCreateDialogOpen] = useState<boolean>(false);
  const [creating, setCreating] = useState<boolean>(false);
  const [createForm, setCreateForm] = useState<CreateUserDto>({
    userName: "",
    email: "",
    password: "",
    roleType: "STAFF",
  });

  // Update role dialog state
  const [updateRoleDialogOpen, setUpdateRoleDialogOpen] = useState<boolean>(false);
  const [updating, setUpdating] = useState<boolean>(false);
  const [selectedUser, setSelectedUser] = useState<UserResponseDto | null>(null);
  const [newRole, setNewRole] = useState<string>("");

  // Deactivate user dialog state
  const [deactivateDialogOpen, setDeactivateDialogOpen] = useState<boolean>(false);
  const [deactivating, setDeactivating] = useState<boolean>(false);
  const [userToDeactivate, setUserToDeactivate] = useState<UserResponseDto | null>(null);

  // Reactivate user dialog state
  const [reactivateDialogOpen, setReactivateDialogOpen] = useState<boolean>(false);
  const [reactivating, setReactivating] = useState<boolean>(false);
  const [userToReactivate, setUserToReactivate] = useState<UserResponseDto | null>(null);

  const { currentUser, isAdmin } = useCurrentUser();
  const router = useRouter();

  // Check authorization
  useEffect(() => {
    if (!isAdmin) {
      toast.error("Unauthorized", {
        description: "You do not have permission to access this page.",
      });
      router.push(Route.Unauthorized);
    }
  }, [isAdmin, router]);

  // Fetch user types
  useEffect(() => {
    const fetchUserTypes = async () => {
      try {
        const types = await userManagementService.getUserTypes();
        setUserTypes(types);
      } catch (error) {
        console.error("Error fetching user types:", error);
      }
    };

    if (isAdmin) {
      fetchUserTypes();
    }
  }, [isAdmin]);

  // Fetch all users
  const fetchUsers = async () => {
    try {
      setLoading(true);
      const allUsers = await userManagementService.getAllUsers();
      setUsers(allUsers);
      setFilteredUsers(allUsers);
    } catch (error) {
      console.error("Error fetching users:", error);
      toast.error("Failed to fetch users", {
        description: error instanceof Error ? error.message : "Unknown error",
      });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (isAdmin) {
      fetchUsers();
    }
  }, [isAdmin]);

  // Filter users based on search query
  useEffect(() => {
    if (searchQuery.trim() === "") {
      setFilteredUsers(users);
    } else {
      const query = searchQuery.toLowerCase();
      setFilteredUsers(
        users.filter(
          (user) =>
            user.username.toLowerCase().includes(query) ||
            user.email.toLowerCase().includes(query) ||
            user.roleType.toLowerCase().includes(query)
        )
      );
    }
  }, [searchQuery, users]);

  // Handle create user
  const handleCreateUser = async () => {
    if (!createForm.userName || !createForm.email || !createForm.password || !createForm.roleType) {
      toast.error("All fields are required");
      return;
    }

    try {
      setCreating(true);
      await userManagementService.adminCreateUser(createForm);
      toast.success("User created successfully");
      setCreateDialogOpen(false);
      setCreateForm({
        userName: "",
        email: "",
        password: "",
        roleType: "STAFF",
      });
      fetchUsers();
    } catch (error) {
      console.error("Error creating user:", error);
      if (error instanceof BaseValidationError) {
        toast.error("Validation error", {
          description: error.errors.join(", "),
        });
      } else if (error instanceof BaseApiError) {
        toast.error("Failed to create user", {
          description: error.message,
        });
      } else {
        toast.error("Failed to create user", {
          description: error instanceof Error ? error.message : "Unknown error",
        });
      }
    } finally {
      setCreating(false);
    }
  };

  // Handle update role
  const handleUpdateRole = async () => {
    if (!selectedUser || !newRole) {
      toast.error("Please select a role");
      return;
    }

    try {
      setUpdating(true);
      const updateData: UpdateUserRoleDto = {
        userId: selectedUser.id,
        email: selectedUser.email,
        roleType: newRole,
      };
      await userManagementService.updateUserRole(updateData);
      toast.success("User role updated successfully");
      setUpdateRoleDialogOpen(false);
      setSelectedUser(null);
      setNewRole("");
      fetchUsers();
    } catch (error) {
      console.error("Error updating user role:", error);
      if (error instanceof BaseValidationError) {
        toast.error("Validation error", {
          description: error.errors.join(", "),
        });
      } else if (error instanceof BaseApiError) {
        toast.error("Failed to update role", {
          description: error.message,
        });
      } else {
        toast.error("Failed to update role", {
          description: error instanceof Error ? error.message : "Unknown error",
        });
      }
    } finally {
      setUpdating(false);
    }
  };

  // Handle deactivate user
  const handleDeactivateUser = async () => {
    if (!userToDeactivate) return;

    try {
      setDeactivating(true);
      await userManagementService.deactivateUser(userToDeactivate.id);
      toast.success("User deactivated successfully");
      setDeactivateDialogOpen(false);
      setUserToDeactivate(null);
      fetchUsers();
    } catch (error) {
      console.error("Error deactivating user:", error);
      if (error instanceof BaseApiError) {
        toast.error("Failed to deactivate user", {
          description: error.message,
        });
      } else {
        toast.error("Failed to deactivate user", {
          description: error instanceof Error ? error.message : "Unknown error",
        });
      }
    } finally {
      setDeactivating(false);
    }
  };

  // Handle reactivate user
  const handleReactivateUser = async () => {
    if (!userToReactivate) return;

    try {
      setReactivating(true);
      await userManagementService.reactivateUser(userToReactivate.id);
      toast.success("User reactivated successfully");
      setReactivateDialogOpen(false);
      setUserToReactivate(null);
      fetchUsers();
    } catch (error) {
      console.error("Error reactivating user:", error);
      if (error instanceof BaseApiError) {
        toast.error("Failed to reactivate user", {
          description: error.message,
        });
      } else {
        toast.error("Failed to reactivate user", {
          description: error instanceof Error ? error.message : "Unknown error",
        });
      }
    } finally {
      setReactivating(false);
    }
  };

  // Open update role dialog
  const openUpdateRoleDialog = (user: UserResponseDto) => {
    setSelectedUser(user);
    setNewRole(user.roleType);
    setUpdateRoleDialogOpen(true);
  };

  // Open deactivate dialog
  const openDeactivateDialog = (user: UserResponseDto) => {
    setUserToDeactivate(user);
    setDeactivateDialogOpen(true);
  };

  // Open reactivate dialog
  const openReactivateDialog = (user: UserResponseDto) => {
    setUserToReactivate(user);
    setReactivateDialogOpen(true);
  };

  if (!isAdmin) {
    return <FullPageSpinnerLoader />;
  }

  return (
    <SidebarInset>
      <header className="flex h-16 shrink-0 items-center gap-2 border-b px-4">
        <SidebarTrigger className="-ml-1" />
        <Separator orientation="vertical" className="mr-2 h-4" />
        <div className="flex items-center gap-2">
          <Users className="h-5 w-5" />
          <h1 className="text-lg font-semibold">User Management</h1>
        </div>
      </header>

      <div className="flex flex-1 flex-col gap-6 p-6 pb-12 lg:p-10">
        <div className="flex justify-between items-center">
          <div>
            <p className="text-muted-foreground">
              Manage users, roles, and permissions
            </p>
          </div>
          <Button onClick={() => setCreateDialogOpen(true)}>
            <UserPlus className="mr-2 h-4 w-4" />
            Create User
          </Button>
        </div>

        <Card>
          <CardHeader>
            <div className="flex justify-between items-center">
            <CardTitle>Users</CardTitle>
            <div className="flex gap-2">
              <div className="relative">
                <Search className="absolute left-2 top-2.5 h-4 w-4 text-muted-foreground" />
                <Input
                  placeholder="Search users..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="pl-8 w-[250px]"
                />
              </div>
              <Button variant="outline" size="icon" onClick={fetchUsers}>
                <RefreshCcw className="h-4 w-4" />
              </Button>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          {loading ? (
            <div className="flex justify-center py-8">
              <Loader2 className="h-8 w-8 animate-spin" />
            </div>
          ) : filteredUsers.length === 0 ? (
            <div className="text-center py-8 text-muted-foreground">
              <p>No users found</p>
              <p className="text-sm mt-2">
                Create a new user or adjust your search filters
              </p>
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Username</TableHead>
                  <TableHead>Email</TableHead>
                  <TableHead>Role</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {filteredUsers.map((user) => (
                  <TableRow key={user.id}>
                    <TableCell className="font-medium">{user.username}</TableCell>
                    <TableCell>{user.email}</TableCell>
                    <TableCell>
                      <Badge variant="secondary">{user.roleType}</Badge>
                    </TableCell>
                    <TableCell>
                      <Badge variant={user.isActive === false ? "destructive" : "default"}>
                        {user.isActive === false ? "Inactive" : "Active"}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-right">
                      <div className="flex justify-end gap-2">
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => openUpdateRoleDialog(user)}
                          disabled={user.isActive === false}
                        >
                          <Shield className="mr-1 h-3 w-3" />
                          Change Role
                        </Button>
                        {user.isActive === false ? (
                          <Button
                            variant="default"
                            size="sm"
                            onClick={() => openReactivateDialog(user)}
                          >
                            <UserPlus className="mr-1 h-3 w-3" />
                            Reactivate
                          </Button>
                        ) : (
                          <Button
                            variant="destructive"
                            size="sm"
                            onClick={() => openDeactivateDialog(user)}
                          >
                            <UserX className="mr-1 h-3 w-3" />
                            Deactivate
                          </Button>
                        )}
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>

      {/* Create User Dialog */}
      <Dialog open={createDialogOpen} onOpenChange={setCreateDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Create New User</DialogTitle>
            <DialogDescription>
              Add a new user to the system. They will receive credentials to log in.
            </DialogDescription>
          </DialogHeader>
          <div className="grid gap-4 py-4">
            <div className="grid gap-2">
              <Label htmlFor="userName">Full Name</Label>
              <Input
                id="userName"
                value={createForm.userName}
                onChange={(e) =>
                  setCreateForm({ ...createForm, userName: e.target.value })
                }
                placeholder="John Doe"
              />
            </div>
            <div className="grid gap-2">
              <Label htmlFor="email">Email</Label>
              <Input
                id="email"
                type="email"
                value={createForm.email}
                onChange={(e) =>
                  setCreateForm({ ...createForm, email: e.target.value })
                }
                placeholder="john.doe@example.com"
              />
            </div>
            <div className="grid gap-2">
              <Label htmlFor="password">Password</Label>
              <Input
                id="password"
                type="password"
                value={createForm.password}
                onChange={(e) =>
                  setCreateForm({ ...createForm, password: e.target.value })
                }
                placeholder="••••••••"
              />
            </div>
            <div className="grid gap-2">
              <Label htmlFor="roleType">Role</Label>
              <Select
                value={createForm.roleType}
                onValueChange={(value) =>
                  setCreateForm({ ...createForm, roleType: value })
                }
              >
                <SelectTrigger id="roleType">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {userTypes.length > 0 ? (
                    userTypes.map((type) => (
                      <SelectItem key={type} value={type}>
                        {type}
                      </SelectItem>
                    ))
                  ) : (
                    <>
                      <SelectItem value="STAFF">STAFF</SelectItem>
                      <SelectItem value="MANAGER">MANAGER</SelectItem>
                      <SelectItem value="HR">HR</SelectItem>
                      <SelectItem value="DIRECTOR">DIRECTOR</SelectItem>
                    </>
                  )}
                </SelectContent>
              </Select>
            </div>
          </div>
          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => setCreateDialogOpen(false)}
              disabled={creating}
            >
              Cancel
            </Button>
            <Button onClick={handleCreateUser} disabled={creating}>
              {creating && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
              Create User
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Update Role Dialog */}
      <Dialog open={updateRoleDialogOpen} onOpenChange={setUpdateRoleDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Update User Role</DialogTitle>
            <DialogDescription>
              Change the role for {selectedUser?.username}
            </DialogDescription>
          </DialogHeader>
          <div className="grid gap-4 py-4">
            <div className="grid gap-2">
              <Label htmlFor="newRole">New Role</Label>
              <Select value={newRole} onValueChange={setNewRole}>
                <SelectTrigger id="newRole">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {userTypes.length > 0 ? (
                    userTypes.map((type) => (
                      <SelectItem key={type} value={type}>
                        {type}
                      </SelectItem>
                    ))
                  ) : (
                    <>
                      <SelectItem value="STAFF">STAFF</SelectItem>
                      <SelectItem value="MANAGER">MANAGER</SelectItem>
                      <SelectItem value="HR">HR</SelectItem>
                      <SelectItem value="DIRECTOR">DIRECTOR</SelectItem>
                    </>
                  )}
                </SelectContent>
              </Select>
            </div>
          </div>
          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => setUpdateRoleDialogOpen(false)}
              disabled={updating}
            >
              Cancel
            </Button>
            <Button onClick={handleUpdateRole} disabled={updating}>
              {updating && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
              Update Role
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Deactivate User Dialog */}
      <Dialog open={deactivateDialogOpen} onOpenChange={setDeactivateDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Deactivate User</DialogTitle>
            <DialogDescription>
              Are you sure you want to deactivate {userToDeactivate?.username}? They will
              no longer be able to access the system.
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => setDeactivateDialogOpen(false)}
              disabled={deactivating}
            >
              Cancel
            </Button>
            <Button
              variant="destructive"
              onClick={handleDeactivateUser}
              disabled={deactivating}
            >
              {deactivating && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
              Deactivate
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Reactivate User Dialog */}
      <Dialog open={reactivateDialogOpen} onOpenChange={setReactivateDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Reactivate User</DialogTitle>
            <DialogDescription>
              Are you sure you want to reactivate {userToReactivate?.username}? They will
              regain access to the system.
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => setReactivateDialogOpen(false)}
              disabled={reactivating}
            >
              Cancel
            </Button>
            <Button
              onClick={handleReactivateUser}
              disabled={reactivating}
            >
              {reactivating && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
              Reactivate
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
      </div>
    </SidebarInset>
  );
}
