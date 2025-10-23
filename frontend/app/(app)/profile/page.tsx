"use client";

import React, { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Separator } from "@/components/ui/separator";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { toast } from "sonner";
import { Loader2, Trash2, User, Mail, Shield } from "lucide-react";
import { useCurrentUser } from "@/contexts/user-context";
import { userManagementService } from "@/services/user-management-service";
import { UserResponseDto } from "@/types/user";
import {
  BaseApiError,
  BaseValidationError,
} from "@/services/authenticated-api-client";
import { Route } from "@/enums/Route";
import FullPageSpinnerLoader from "@/components/full-page-spinner-loader";

export default function ProfilePage() {
  const [profileData, setProfileData] = useState<UserResponseDto | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [deleting, setDeleting] = useState<boolean>(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState<boolean>(false);
  const [error, setError] = useState<string>("");

  const { currentUser, signOut } = useCurrentUser();
  const router = useRouter();

  // Fetch user profile data from backend
  useEffect(() => {
    const fetchProfile = async () => {
      if (!currentUser?.cognitoSub) {
        setError("No user session found");
        setLoading(false);
        return;
      }

      try {
        setLoading(true);
        setError("");

        const userData = await userManagementService.getUserByCognitoSub(
          currentUser.cognitoSub,
        );
        setProfileData(userData);
      } catch (err) {
        console.error("Error fetching profile:", err);

        if (err instanceof BaseApiError) {
          const errorMessage =
            err.errors.length > 0
              ? err.errors[0].message
              : "Failed to load profile data";
          setError(errorMessage);
        } else {
          setError("An unexpected error occurred while loading your profile.");
        }
      } finally {
        setLoading(false);
      }
    };

    fetchProfile();
  }, [currentUser]);

  const handleDeleteAccount = async () => {
    if (!profileData) return;

    try {
      setDeleting(true);

      await userManagementService.deleteUser(profileData.id);

      toast("Account Deleted", {
        description: "Your account has been successfully deleted.",
      });

      // Sign out and redirect to sign in page
      await signOut();
      router.push(Route.SignIn);
    } catch (err) {
      console.error("Error deleting account:", err);

      if (err instanceof BaseValidationError) {
        const validationMessages = err.validationErrors
          .map((error) => error.message)
          .join("; ");
        toast("Deletion Failed", {
          description: validationMessages,
        });
      } else if (err instanceof BaseApiError) {
        const errorMessage =
          err.errors.length > 0
            ? err.errors[0].message
            : "Failed to delete account";
        toast("Deletion Failed", {
          description: errorMessage,
        });
      } else {
        toast("Deletion Failed", {
          description: "An unexpected error occurred. Please try again.",
        });
      }
    } finally {
      setDeleting(false);
      setDeleteDialogOpen(false);
    }
  };

  if (loading) {
    return <FullPageSpinnerLoader loadingMessage="Loading Profile..." />;
  }

  if (error) {
    return (
      <div className="max-w-2xl mx-auto p-6">
        <Card>
          <CardContent className="p-6">
            <div className="text-center">
              <h2 className="text-2xl font-bold text-red-600 mb-2">Error</h2>
              <p className="text-gray-600">{error}</p>
              <Button onClick={() => window.location.reload()} className="mt-4">
                Retry
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    );
  }

  if (!profileData) {
    return (
      <div className="max-w-2xl mx-auto p-6">
        <Card>
          <CardContent className="p-6">
            <div className="text-center">
              <h2 className="text-2xl font-bold mb-2">Profile Not Found</h2>
              <p className="text-gray-600">Unable to load your profile data.</p>
            </div>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="max-w-2xl mx-auto p-6 space-y-6">
      {/* Profile Header */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center space-x-2">
            <User className="h-6 w-6" />
            <span>My Profile</span>
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <h3 className="text-2xl font-bold">{profileData.username}</h3>
              <p className="text-gray-600 flex items-center space-x-1">
                <Mail className="h-4 w-4" />
                <span>{profileData.email}</span>
              </p>
            </div>
            <Badge variant="secondary" className="flex items-center space-x-1">
              <Shield className="h-3 w-3" />
              <span>{profileData.roleType}</span>
            </Badge>
          </div>
        </CardContent>
      </Card>

      {/* Profile Details */}
      <Card>
        <CardHeader>
          <CardTitle>Account Information</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="text-sm font-medium text-gray-500">
                User ID
              </label>
              <p className="text-sm">{profileData.id}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-gray-500">Role</label>
              <p className="text-sm">{profileData.roleType}</p>
            </div>
          </div>

          <Separator />

          <div>
            <label className="text-sm font-medium text-gray-500">
              Cognito Sub
            </label>
            <p className="text-xs font-mono">{profileData.cognitoSub}</p>
          </div>
        </CardContent>
      </Card>

      {/* Account Actions */}
      <Card>
        <CardContent>
          <div className="space-y-4">
            <div>
              <h4 className="font-medium">Delete Account</h4>
              <p className="text-sm text-gray-600 mb-4">
                Permanently delete your account and all associated data. This
                action cannot be undone.
              </p>
              <Button
                variant="destructive"
                onClick={() => setDeleteDialogOpen(true)}
                className="flex items-center space-x-2"
              >
                <Trash2 className="h-4 w-4" />
                <span>Delete My Account</span>
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Delete Confirmation Dialog */}
      <Dialog open={deleteDialogOpen} onOpenChange={setDeleteDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle className="flex items-center space-x-2 text-red-600">
              <Trash2 className="h-5 w-5" />
              <span>Delete Account</span>
            </DialogTitle>
            <DialogDescription>
              Are you sure you want to delete your account? This action will:
              <ul className="list-disc list-inside mt-2 space-y-1">
                <li>Permanently delete your profile data</li>
                <li>Remove your access to the system</li>
                <li>Sign you out immediately</li>
              </ul>
              <p className="mt-3 font-semibold">
                This action cannot be undone.
              </p>
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => setDeleteDialogOpen(false)}
              disabled={deleting}
            >
              Cancel
            </Button>
            <Button
              variant="destructive"
              onClick={handleDeleteAccount}
              disabled={deleting}
              className="flex items-center space-x-2"
            >
              {deleting ? (
                <>
                  <Loader2 className="h-4 w-4 animate-spin" />
                  <span>Deleting...</span>
                </>
              ) : (
                <>
                  <Trash2 className="h-4 w-4" />
                  <span>Delete Account</span>
                </>
              )}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
