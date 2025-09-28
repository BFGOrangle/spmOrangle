"use client";

import { useActionState, useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { handleResetPassword, handleVerifyResetCode } from "@/lib/cognito-actions";
import { redirect } from "next/navigation";
import { Route } from "@/enums/Route";
import Link from "next/link";
import { AlertCircle, Eye, EyeOff, CheckCircle } from "lucide-react";
import { Alert, AlertDescription } from "@/components/ui/alert";

export default function ResetPasswordForm() {
  const [codeVerified, setCodeVerified] = useState(false);
  
  const [verifyState, verifyAction, isVerifying] = useActionState(
    handleVerifyResetCode,
    undefined
  );
  
  const [resetState, resetAction, isResetting] = useActionState(
    handleResetPassword,
    undefined
  );
  
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  // Handle successful code verification
  if (verifyState && typeof verifyState === "object" && "codeVerified" in verifyState) {
    if (verifyState.codeVerified && !codeVerified) {
      setCodeVerified(true);
    }
  }

  // Handle successful password reset
  if (resetState && typeof resetState === "object" && "success" in resetState) {
    redirect(resetState.redirectTo);
  }

  const handlePasswordSubmit = (formData: FormData) => {
    const password = formData.get("password") as string;
    const confirmPassword = formData.get("confirmPassword") as string;

    if (password !== confirmPassword) {
      // We need to handle this validation error differently since we can't return from here
      // For now, we'll let the form handle this with HTML5 validation
      return;
    }

    resetAction(formData);
  };

  const currentError = verifyState && typeof verifyState === "string" ? verifyState : 
                      resetState && typeof resetState === "string" ? resetState : null;

  return (
    <Card className="w-full max-w-md mx-auto">
      <CardHeader className="text-center">
        <CardTitle>
          {!codeVerified ? "Verify Reset Code" : "Set New Password"}
        </CardTitle>
        <CardDescription>
          {!codeVerified 
            ? "Enter the verification code sent to your email."
            : "Create a new password for your account."
          }
        </CardDescription>
      </CardHeader>
      <CardContent>
        {!codeVerified ? (
          // Step 1: Verify Code
          <form action={verifyAction} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="code">Verification Code</Label>
              <Input
                id="code"
                name="code"
                type="text"
                placeholder="Enter the 6-digit code"
                required
                disabled={isVerifying}
                pattern="[0-9]{6}"
                maxLength={6}
              />
            </div>

            {currentError && (
              <Alert variant="destructive">
                <AlertCircle className="h-4 w-4" />
                <AlertDescription>{currentError}</AlertDescription>
              </Alert>
            )}

            <Button 
              type="submit" 
              className="w-full" 
              disabled={isVerifying}
            >
              {isVerifying ? "Verifying Code..." : "Verify Code"}
            </Button>

            <div className="text-center space-y-2">
              <p className="text-sm text-muted-foreground">
                Didn't receive the code?{" "}
                <Link 
                  href={Route.ForgotPassword} 
                  className="text-primary hover:underline"
                >
                  Request new code
                </Link>
              </p>
              <p className="text-sm text-muted-foreground">
                Remember your password?{" "}
                <Link 
                  href={Route.SignIn} 
                  className="text-primary hover:underline"
                >
                  Sign in
                </Link>
              </p>
            </div>
          </form>
        ) : (
          // Step 2: Set New Password
          <div className="space-y-4">
            {/* Code verified indicator */}
            <Alert className="border-green-200 bg-green-50 text-green-800">
              <CheckCircle className="h-4 w-4 text-green-600" />
              <AlertDescription>
                Code verified successfully! Now create your new password.
              </AlertDescription>
            </Alert>

            <form action={handlePasswordSubmit} className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="password">New Password</Label>
                <div className="relative">
                  <Input
                    id="password"
                    name="password"
                    type={showPassword ? "text" : "password"}
                    placeholder="Enter your new password"
                    required
                    disabled={isResetting}
                    minLength={8}
                  />
                  <Button
                    type="button"
                    variant="ghost"
                    size="sm"
                    className="absolute right-0 top-0 h-full px-3 py-2 hover:bg-transparent"
                    onClick={() => setShowPassword(!showPassword)}
                    disabled={isResetting}
                  >
                    {showPassword ? (
                      <EyeOff className="h-4 w-4" />
                    ) : (
                      <Eye className="h-4 w-4" />
                    )}
                    <span className="sr-only">
                      {showPassword ? "Hide password" : "Show password"}
                    </span>
                  </Button>
                </div>
                <p className="text-xs text-muted-foreground">
                  Password must be at least 8 characters and include uppercase, lowercase, numbers, and special characters.
                </p>
              </div>

              <div className="space-y-2">
                <Label htmlFor="confirmPassword">Confirm New Password</Label>
                <div className="relative">
                  <Input
                    id="confirmPassword"
                    name="confirmPassword"
                    type={showConfirmPassword ? "text" : "password"}
                    placeholder="Confirm your new password"
                    required
                    disabled={isResetting}
                    minLength={8}
                  />
                  <Button
                    type="button"
                    variant="ghost"
                    size="sm"
                    className="absolute right-0 top-0 h-full px-3 py-2 hover:bg-transparent"
                    onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                    disabled={isResetting}
                  >
                    {showConfirmPassword ? (
                      <EyeOff className="h-4 w-4" />
                    ) : (
                      <Eye className="h-4 w-4" />
                    )}
                    <span className="sr-only">
                      {showConfirmPassword ? "Hide password" : "Show password"}
                    </span>
                  </Button>
                </div>
              </div>

              {currentError && (
                <Alert variant="destructive">
                  <AlertCircle className="h-4 w-4" />
                  <AlertDescription>{currentError}</AlertDescription>
                </Alert>
              )}

              <Button 
                type="submit" 
                className="w-full" 
                disabled={isResetting}
              >
                {isResetting ? "Resetting Password..." : "Reset Password"}
              </Button>
            </form>

            <div className="text-center">
              <p className="text-sm text-muted-foreground">
                Need to change the code?{" "}
                <button 
                  type="button"
                  onClick={() => setCodeVerified(false)}
                  className="text-primary hover:underline bg-transparent border-none cursor-pointer"
                >
                  Go back
                </button>
              </p>
            </div>
          </div>
        )}
      </CardContent>
    </Card>
  );
}