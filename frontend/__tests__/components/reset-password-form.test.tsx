import { render, screen, fireEvent } from "@testing-library/react";
import { useRouter } from "next/navigation";
import ResetPasswordForm from "../../app/(app)/auth/reset-password/components/reset-password-form";
import { handleResetPassword, handleVerifyResetCode } from "../../lib/cognito-actions";
import { Route } from "../../enums/Route";
import { useActionState } from "react";

// Mock Next.js router
jest.mock("next/navigation", () => ({
  useRouter: jest.fn(),
  redirect: jest.fn(),
}));

// Mock cognito actions
jest.mock("../../lib/cognito-actions", () => ({
  handleResetPassword: jest.fn(),
  handleVerifyResetCode: jest.fn(),
}));

// Mock useActionState with better implementation
const mockUseActionState = jest.fn();
jest.mock("react", () => ({
  ...jest.requireActual("react"),
  useActionState: (...args: any[]) => mockUseActionState(...args),
}));

const mockRouter = {
  push: jest.fn(),
  replace: jest.fn(),
  back: jest.fn(),
  forward: jest.fn(),
  refresh: jest.fn(),
  prefetch: jest.fn(),
};

const mockHandleResetPassword = handleResetPassword as jest.MockedFunction<
  typeof handleResetPassword
>;

const mockHandleVerifyResetCode = handleVerifyResetCode as jest.MockedFunction<
  typeof handleVerifyResetCode
>;

// Helper to setup mocks for code verification step
const setupCodeVerificationStep = (error?: string, isPending = false) => {
  let callCount = 0;
  mockUseActionState.mockImplementation(() => {
    callCount++;
    if (callCount === 1) {
      // First call - verifyAction
      return [error, jest.fn(), isPending];
    } else {
      // Second call - resetAction
      return [undefined, jest.fn(), false];
    }
  });
};

// Helper to setup mocks for password reset step  
const setupPasswordResetStep = (error?: string, isPending = false) => {
  let callCount = 0;
  mockUseActionState.mockImplementation(() => {
    callCount++;
    if (callCount === 1) {
      // First call - verifyAction (already verified)
      return [{ success: true, codeVerified: true }, jest.fn(), false];
    } else {
      // Second call - resetAction
      return [error, jest.fn(), isPending];
    }
  });
};

describe("ResetPasswordForm", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    (useRouter as jest.Mock).mockReturnValue(mockRouter);
  });

  it("renders code verification form initially", () => {
    setupCodeVerificationStep();
    render(<ResetPasswordForm />);
    
    expect(screen.getByText("Verify Reset Code")).toBeInTheDocument();
    expect(screen.getByText("Enter the verification code sent to your email.")).toBeInTheDocument();
    expect(screen.getByLabelText("Verification Code")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Verify Code" })).toBeInTheDocument();
    
    // Password fields should not be visible initially
    expect(screen.queryByLabelText("New Password")).not.toBeInTheDocument();
    expect(screen.queryByLabelText("Confirm New Password")).not.toBeInTheDocument();
  });

  it("enforces verification code validation", () => {
    setupCodeVerificationStep();
    render(<ResetPasswordForm />);
    
    const codeInput = screen.getByLabelText("Verification Code");
    
    expect(codeInput).toHaveAttribute("pattern", "[0-9]{6}");
    expect(codeInput).toHaveAttribute("maxLength", "6");
    expect(codeInput).toBeRequired();
  });

  it("shows password form after successful code verification", () => {
    setupPasswordResetStep();
    render(<ResetPasswordForm />);
    
    expect(screen.getByText("Set New Password")).toBeInTheDocument();
    expect(screen.getByText("Create a new password for your account.")).toBeInTheDocument();
    expect(screen.getByText(/Code verified successfully! Now create your new password/)).toBeInTheDocument();
    expect(screen.getByLabelText("New Password")).toBeInTheDocument();
    expect(screen.getByLabelText("Confirm New Password")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Reset Password" })).toBeInTheDocument();
  });

  it("enforces password requirements after code verification", () => {
    setupPasswordResetStep();
    render(<ResetPasswordForm />);
    
    const passwordInput = screen.getByLabelText("New Password");
    const confirmPasswordInput = screen.getByLabelText("Confirm New Password");
    
    expect(passwordInput).toHaveAttribute("minLength", "8");
    expect(passwordInput).toBeRequired();
    expect(confirmPasswordInput).toHaveAttribute("minLength", "8");
    expect(confirmPasswordInput).toBeRequired();
    
    expect(screen.getByText(/Password must be at least 8 characters and include uppercase, lowercase, numbers, and special characters/)).toBeInTheDocument();
  });

  it("toggles password visibility after code verification", () => {
    setupPasswordResetStep();
    render(<ResetPasswordForm />);
    
    const passwordInput = screen.getByLabelText("New Password");
    const toggleButton = screen.getAllByRole("button", { name: /show password/i })[0];
    
    expect(passwordInput).toHaveAttribute("type", "password");
    
    fireEvent.click(toggleButton);
    expect(passwordInput).toHaveAttribute("type", "text");
    
    fireEvent.click(toggleButton);
    expect(passwordInput).toHaveAttribute("type", "password");
  });

  it("toggles confirm password visibility independently after code verification", () => {
    setupPasswordResetStep();
    render(<ResetPasswordForm />);
    
    const confirmPasswordInput = screen.getByLabelText("Confirm New Password");
    const toggleButtons = screen.getAllByRole("button", { name: /show password/i });
    const confirmToggleButton = toggleButtons[1];
    
    expect(confirmPasswordInput).toHaveAttribute("type", "password");
    
    fireEvent.click(confirmToggleButton);
    expect(confirmPasswordInput).toHaveAttribute("type", "text");
  });

  it("displays error message for invalid/expired code during verification", () => {
    setupCodeVerificationStep("Invalid verification code provided, please try again.");
    render(<ResetPasswordForm />);
    
    expect(screen.getByText(/Invalid verification code provided, please try again/)).toBeInTheDocument();
  });

  it("displays error message for password policy violations during reset", () => {
    setupPasswordResetStep("Password does not conform to policy: Password must have uppercase characters");
    render(<ResetPasswordForm />);
    
    expect(screen.getByText(/Password does not conform to policy: Password must have uppercase characters/)).toBeInTheDocument();
  });

  it("shows loading state when verifying code", () => {
    setupCodeVerificationStep(undefined, true);
    render(<ResetPasswordForm />);
    
    expect(screen.getByText("Verifying Code...")).toBeInTheDocument();
    expect(screen.getByLabelText("Verification Code")).toBeDisabled();
  });

  it("shows loading state when resetting password", () => {
    setupPasswordResetStep(undefined, true);
    render(<ResetPasswordForm />);
    
    expect(screen.getByText("Resetting Password...")).toBeInTheDocument();
    expect(screen.getByLabelText("New Password")).toBeDisabled();
    expect(screen.getByLabelText("Confirm New Password")).toBeDisabled();
  });

  it("handles successful code verification flow", async () => {
    const mockVerifyAction = jest.fn();
    
    mockUseActionState
      .mockReturnValueOnce([undefined, mockVerifyAction, false])
      .mockReturnValueOnce([undefined, jest.fn(), false]);
    
    render(<ResetPasswordForm />);
    
    const codeInput = screen.getByLabelText("Verification Code");
    const verifyButton = screen.getByRole("button", { name: "Verify Code" });
    
    fireEvent.change(codeInput, { target: { value: "123456" } });
    fireEvent.click(verifyButton);
    
    expect(mockVerifyAction).toHaveBeenCalled();
  });

  it("handles successful password reset after verification", async () => {
    const mockResetAction = jest.fn();
    
    // Set up for password reset step with no loading state
    let callCount = 0;
    mockUseActionState.mockImplementation(() => {
      callCount++;
      if (callCount === 1) {
        // First call - verifyAction (already verified)
        return [{ success: true, codeVerified: true }, jest.fn(), false];
      } else {
        // Second call - resetAction (not loading, no error)
        return [undefined, mockResetAction, false];
      }
    });
    
    render(<ResetPasswordForm />);
    
    const passwordInput = screen.getByLabelText("New Password");
    const confirmPasswordInput = screen.getByLabelText("Confirm New Password");
    const resetButton = screen.getByRole("button", { name: "Reset Password" });
    
    fireEvent.change(passwordInput, { target: { value: "NewPass123!" } });
    fireEvent.change(confirmPasswordInput, { target: { value: "NewPass123!" } });
    fireEvent.click(resetButton);
    
    expect(mockResetAction).toHaveBeenCalled();
  });

  it("has correct navigation links in code verification step", () => {
    setupCodeVerificationStep();
    render(<ResetPasswordForm />);
    
    const requestNewCodeLink = screen.getByText("Request new code");
    expect(requestNewCodeLink).toBeInTheDocument();
    expect(requestNewCodeLink.closest("a")).toHaveAttribute("href", Route.ForgotPassword);
    
    const signInLink = screen.getByText("Sign in");
    expect(signInLink).toBeInTheDocument();
    expect(signInLink.closest("a")).toHaveAttribute("href", Route.SignIn);
  });

  it("allows going back to code verification from password step", () => {
    setupPasswordResetStep();
    render(<ResetPasswordForm />);
    
    const goBackButton = screen.getByText("Go back");
    expect(goBackButton).toBeInTheDocument();
    
    fireEvent.click(goBackButton);
    
    // After clicking go back, we should be back to code verification
    // The component uses local state so this works immediately
    expect(screen.queryByText("Set New Password")).not.toBeInTheDocument();
  });

  it("enforces single-use token policy through error messages", () => {
    setupCodeVerificationStep("Invalid verification code provided, please try again.");
    render(<ResetPasswordForm />);
    
    // This error message indicates the token has been used or is invalid
    expect(screen.getByText(/Invalid verification code provided, please try again/)).toBeInTheDocument();
    
    // User should be able to request a new code
    const requestNewCodeLink = screen.getByText("Request new code");
    expect(requestNewCodeLink).toBeInTheDocument();
  });

  it("handles session expiration gracefully during code verification", () => {
    setupCodeVerificationStep("Session expired. Please restart the password reset process.");
    render(<ResetPasswordForm />);
    
    expect(screen.getByText(/Session expired. Please restart the password reset process/)).toBeInTheDocument();
  });

  it("handles session expiration gracefully during password reset", () => {
    // Set up for password reset step but with error in reset action
    let callCount = 0;
    mockUseActionState.mockImplementation(() => {
      callCount++;
      if (callCount === 1) {
        // First call - verifyAction (already verified)
        return [{ success: true, codeVerified: true }, jest.fn(), false];
      } else {
        // Second call - resetAction with error
        return ["Session expired. Please restart the password reset process.", jest.fn(), false];
      }
    });
    render(<ResetPasswordForm />);
    
    expect(screen.getByText(/Session expired. Please restart the password reset process/)).toBeInTheDocument();
  });

  it("maintains form state during code verification submission", () => {
    setupCodeVerificationStep(undefined, true);
    render(<ResetPasswordForm />);
    
    const codeInput = screen.getByLabelText("Verification Code");
    expect(codeInput).toBeDisabled();
    
    const submitButton = screen.getByRole("button");
    expect(submitButton).toBeDisabled();
    expect(submitButton).toHaveTextContent("Verifying Code...");
  });

  it("maintains form state during password reset submission", () => {
    setupPasswordResetStep(undefined, true);
    render(<ResetPasswordForm />);
    
    const inputs = [
      screen.getByLabelText("New Password"), 
      screen.getByLabelText("Confirm New Password")
    ];
    
    inputs.forEach(input => {
      expect(input).toBeDisabled();
    });
    
    const submitButton = screen.getByRole("button", { name: "Resetting Password..." });
    expect(submitButton).toBeDisabled();
    expect(submitButton).toHaveTextContent("Resetting Password...");
  });

  it("validates code format requirements", () => {
    setupCodeVerificationStep();
    render(<ResetPasswordForm />);
    
    const codeInput = screen.getByLabelText("Verification Code");
    
    expect(codeInput).toHaveAttribute("pattern", "[0-9]{6}");
    expect(codeInput).toHaveAttribute("maxLength", "6");
    expect(codeInput).toHaveAttribute("placeholder", "Enter the 6-digit code");
  });
});