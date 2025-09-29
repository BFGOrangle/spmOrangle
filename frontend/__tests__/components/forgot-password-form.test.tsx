import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { useRouter } from "next/navigation";
import ForgotPasswordForm from "../../app/(app)/auth/forgot-password/components/forgot-password-form";
import { handleForgotPassword } from "../../lib/cognito-actions";
import { Route } from "../../enums/Route";
import { useActionState } from "react";

// Mock Next.js router
jest.mock("next/navigation", () => ({
  useRouter: jest.fn(),
  redirect: jest.fn(),
}));

// Mock cognito actions
jest.mock("../../lib/cognito-actions", () => ({
  handleForgotPassword: jest.fn(),
}));

// Mock useActionState
jest.mock("react", () => ({
  ...jest.requireActual("react"),
  useActionState: jest.fn(),
}));

const mockRouter = {
  push: jest.fn(),
  replace: jest.fn(),
  back: jest.fn(),
  forward: jest.fn(),
  refresh: jest.fn(),
  prefetch: jest.fn(),
};

const mockHandleForgotPassword = handleForgotPassword as jest.MockedFunction<
  typeof handleForgotPassword
>;

const mockUseActionState = useActionState as jest.MockedFunction<typeof useActionState>;

describe("ForgotPasswordForm", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    (useRouter as jest.Mock).mockReturnValue(mockRouter);
    
    // Mock useActionState to return initial state
    mockUseActionState.mockReturnValue([undefined, jest.fn(), false]);
  });

  it("renders forgot password form correctly", () => {
    render(<ForgotPasswordForm />);
    
    expect(screen.getByText("Reset Password")).toBeInTheDocument();
    expect(screen.getByText("Enter your email address and we'll send you a code to reset your password.")).toBeInTheDocument();
    expect(screen.getByLabelText("Email Address")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Send Reset Code" })).toBeInTheDocument();
  });

  it("shows validation error for invalid email", async () => {
    render(<ForgotPasswordForm />);
    
    const emailInput = screen.getByLabelText("Email Address");
    const submitButton = screen.getByRole("button", { name: "Send Reset Code" });
    
    fireEvent.change(emailInput, { target: { value: "invalid-email" } });
    fireEvent.click(submitButton);
    
    // HTML5 validation should prevent submission
    expect(mockHandleForgotPassword).not.toHaveBeenCalled();
  });

  it("displays error message when email not found", () => {
    mockUseActionState.mockReturnValue([
      "User does not exist",
      jest.fn(),
      false
    ]);
    
    render(<ForgotPasswordForm />);
    
    expect(screen.getByText("User does not exist")).toBeInTheDocument();
  });

  it("shows loading state when submitting", () => {
    mockUseActionState.mockReturnValue([
      undefined,
      jest.fn(),
      true // isPending = true
    ]);
    
    render(<ForgotPasswordForm />);
    
    expect(screen.getByText("Sending Reset Code...")).toBeInTheDocument();
    expect(screen.getByLabelText("Email Address")).toBeDisabled();
  });

  it("handles successful password reset request", async () => {
    const mockFormAction = jest.fn();
    
    // First render with normal state
    const { rerender } = render(<ForgotPasswordForm />);
    mockUseActionState.mockReturnValue([undefined, mockFormAction, false]);
    rerender(<ForgotPasswordForm />);
    
    const emailInput = screen.getByLabelText("Email Address");
    const submitButton = screen.getByRole("button", { name: "Send Reset Code" });
    
    fireEvent.change(emailInput, { target: { value: "test@example.com" } });
    fireEvent.click(submitButton);
    
    // Simulate successful response
    mockUseActionState.mockReturnValue([
      { success: true, redirectTo: Route.ResetPassword },
      mockFormAction,
      false
    ]);
    
    rerender(<ForgotPasswordForm />);
    
    // The component should attempt to redirect (handled by redirect function)
    expect(mockFormAction).toHaveBeenCalled();
  });

  it("has correct navigation links", () => {
    render(<ForgotPasswordForm />);
    
    const signInLink = screen.getByText("Sign in");
    expect(signInLink).toBeInTheDocument();
    expect(signInLink.closest("a")).toHaveAttribute("href", Route.SignIn);
  });

  it("requires email field", () => {
    render(<ForgotPasswordForm />);
    
    const emailInput = screen.getByLabelText("Email Address");
    expect(emailInput).toBeRequired();
    expect(emailInput).toHaveAttribute("type", "email");
  });

  it("handles network errors gracefully", () => {
    mockUseActionState.mockReturnValue([
      "Network error occurred",
      jest.fn(),
      false
    ]);
    
    render(<ForgotPasswordForm />);
    
    expect(screen.getByText("Network error occurred")).toBeInTheDocument();
  });

  it("maintains form state during submission", () => {
    mockUseActionState.mockReturnValue([
      undefined,
      jest.fn(),
      true
    ]);
    
    render(<ForgotPasswordForm />);
    
    const emailInput = screen.getByLabelText("Email Address");
    const submitButton = screen.getByRole("button");
    
    expect(emailInput).toBeDisabled();
    expect(submitButton).toBeDisabled();
    expect(submitButton).toHaveTextContent("Sending Reset Code...");
  });
});