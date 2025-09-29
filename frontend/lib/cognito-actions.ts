import {
  confirmSignUp,
  signIn,
  signOut,
  resendSignUpCode,
  confirmSignIn,
  resetPassword,
  confirmResetPassword,
} from "aws-amplify/auth";
import { Route } from "@/enums/Route";
import { redirect } from "next/navigation";

export async function handleSignIn(
  prevState: string | undefined,
  formData: FormData,
): Promise<{ success: true; redirectTo: string } | string> {
  let redirectLink = "";

  try {
    const username = String(formData.get("email"));
    console.log("Attempting sign in with username:", username);

    // Store email for potential resend operations
    sessionStorage.setItem("signin-email", username);

    const signInResult = await signIn({
      username: username,
      password: String(formData.get("password")),
    });

    const { nextStep } = signInResult;

    if (nextStep.signInStep === "CONFIRM_SIGN_UP") {
      await resendSignUpCode({ username });
      redirectLink = Route.ConfirmSignUp;
    } else if (
      nextStep.signInStep === "CONFIRM_SIGN_IN_WITH_NEW_PASSWORD_REQUIRED"
    ) {
      redirectLink = Route.NewPassword;
    } else if (nextStep.signInStep === "CONFIRM_SIGN_IN_WITH_SMS_CODE") {
      console.log(
        "SMS code sent to:",
        nextStep.codeDeliveryDetails?.destination,
      );
      redirectLink = Route.ConfirmSMSCode;
    } else if (nextStep.signInStep === "DONE") {
      redirectLink = Route.Dashboard;
    } else {
      console.error(`Unexpected NextSignInStep: ${nextStep.signInStep}`);
      throw new Error(`Unexpected next sign in step: ${nextStep.signInStep}`);
    }

    return { success: true, redirectTo: redirectLink };
  } catch (error) {
    console.error("Sign in error:", error);
    return getErrorMessage(error);
  }
}

export async function handleSignOut() {
  try {
    await signOut();
  } catch (error) {
    console.log(getErrorMessage(error));
  }
  console.debug("User logged out");
  redirect(Route.SignIn);
}

export async function handleEmailCodeConfirmation(
  prevState: string | undefined,
  formData: FormData,
): Promise<{ success: true; redirectTo: string } | string> {
  try {
    const code = String(formData.get("code"));
    console.log("Confirming email code verification");

    const { nextStep } = await confirmSignIn({
      challengeResponse: code,
    });

    if (nextStep.signInStep === "DONE") {
      return { success: true, redirectTo: Route.Dashboard };
    } else {
      throw new Error("Unexpected next step after email verification");
    }
  } catch (error) {
    return getErrorMessage(error);
  }
}

export async function handleSMSCodeConfirmation(
  prevState: string | undefined,
  formData: FormData,
): Promise<{ success: true; redirectTo: string } | string> {
  try {
    const code = String(formData.get("code"));
    console.log("Confirming SMS code verification");

    const { nextStep } = await confirmSignIn({
      challengeResponse: code,
    });

    if (nextStep.signInStep === "DONE") {
      return { success: true, redirectTo: Route.Dashboard };
    } else {
      throw new Error("Unexpected next step after SMS verification");
    }
  } catch (error) {
    return getErrorMessage(error);
  }
}

export async function handleForgotPassword(
  prevState: string | { success: true; redirectTo: string } | undefined,
  formData: FormData,
): Promise<{ success: true; redirectTo: string } | string> {
  try {
    const username = String(formData.get("email"));
    console.log("Requesting password reset for username:", username);

    // Store email for the reset password step
    sessionStorage.setItem("reset-email", username);

    const output = await resetPassword({ username });
    
    if (output.nextStep.resetPasswordStep === "CONFIRM_RESET_PASSWORD_WITH_CODE") {
      console.log(
        "Reset code sent to:",
        output.nextStep.codeDeliveryDetails?.destination
      );
      return { success: true, redirectTo: Route.ResetPassword };
    } else {
      throw new Error(`Unexpected reset password step: ${output.nextStep.resetPasswordStep}`);
    }
  } catch (error) {
    console.error("Forgot password error:", error);
    return getErrorMessage(error);
  }
}

export async function handleVerifyResetCode(
  prevState: string | { success: true; codeVerified: boolean } | undefined,
  formData: FormData,
): Promise<{ success: true; codeVerified: boolean } | string> {
  try {
    const username = sessionStorage.getItem("reset-email");
    const confirmationCode = String(formData.get("code"));

    if (!username) {
      throw new Error("Session expired. Please restart the password reset process.");
    }

    // Basic client-side validation for code format
    if (!/^\d{6}$/.test(confirmationCode)) {
      throw new Error("Please enter a valid 6-digit verification code.");
    }

    console.log("Code format validated for username:", username);

    // Store the code for the actual password reset
    sessionStorage.setItem("verified-reset-code", confirmationCode);

    console.log("Reset code format verified successfully");
    return { success: true, codeVerified: true };
  } catch (error) {
    console.error("Code verification error:", error);
    return getErrorMessage(error);
  }
}

export async function handleResetPassword(
  prevState: string | { success: true; redirectTo: string } | undefined,
  formData: FormData,
): Promise<{ success: true; redirectTo: string } | string> {
  try {
    const username = sessionStorage.getItem("reset-email");
    const confirmationCode = sessionStorage.getItem("verified-reset-code");
    const newPassword = String(formData.get("password"));

    if (!username) {
      throw new Error("Session expired. Please restart the password reset process.");
    }

    if (!confirmationCode) {
      throw new Error("Code verification required. Please verify your code first.");
    }

    console.log("Confirming password reset for username:", username);

    await confirmResetPassword({
      username,
      confirmationCode,
      newPassword,
    });

    // Clear the stored email and code
    sessionStorage.removeItem("reset-email");
    sessionStorage.removeItem("verified-reset-code");

    console.log("Password reset successful");
    return { success: true, redirectTo: Route.SignIn };
  } catch (error) {
    console.error("Reset password error:", error);
    // If there's an error, it might be due to an invalid/expired code
    // Clear the verified code so user has to reverify
    sessionStorage.removeItem("verified-reset-code");
    return getErrorMessage(error);
  }
}

export function getErrorMessage(error: unknown): string {
  if (error instanceof Error) {
    return error.message;
  }
  if (error && typeof error === "object" && "message" in error) {
    return String(error.message);
  }
  if (typeof error === "string") {
    return error;
  }
  return "An error occurred";
}
export { confirmSignUp };
