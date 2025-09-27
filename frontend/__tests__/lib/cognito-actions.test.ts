import {
  handleSignIn,
  handleSignOut,
  handleEmailCodeConfirmation,
  handleSMSCodeConfirmation,
  getErrorMessage,
} from "../../lib/cognito-actions";
import {
  signIn,
  signOut,
  resendSignUpCode,
  confirmSignIn,
} from "aws-amplify/auth";
import { redirect } from "next/navigation";
import { Route } from "@/enums/Route";

// Mock AWS Amplify Auth functions
jest.mock("aws-amplify/auth", () => ({
  signIn: jest.fn(),
  signOut: jest.fn(),
  resendSignUpCode: jest.fn(),
  confirmSignIn: jest.fn(),
}));

// Mock Next.js redirect
jest.mock("next/navigation", () => ({
  redirect: jest.fn(),
}));

const mockSignIn = signIn as jest.MockedFunction<typeof signIn>;
const mockSignOut = signOut as jest.MockedFunction<typeof signOut>;
const mockResendSignUpCode = resendSignUpCode as jest.MockedFunction<typeof resendSignUpCode>;
const mockConfirmSignIn = confirmSignIn as jest.MockedFunction<typeof confirmSignIn>;
const mockRedirect = redirect as jest.MockedFunction<typeof redirect>;

// Mock sessionStorage
const mockSessionStorage = {
  setItem: jest.fn(),
  getItem: jest.fn(),
  removeItem: jest.fn(),
  clear: jest.fn(),
};

Object.defineProperty(window, 'sessionStorage', {
  value: mockSessionStorage,
  writable: true,
});

describe("Cognito Actions", () => {
  beforeEach(() => {
    // Clear all mocks
    mockSignIn.mockClear();
    mockSignOut.mockClear();
    mockResendSignUpCode.mockClear();
    mockConfirmSignIn.mockClear();
    mockRedirect.mockClear();
    mockSessionStorage.setItem.mockClear();

    // Mock console methods
    jest.spyOn(console, "log").mockImplementation(() => {});
    jest.spyOn(console, "error").mockImplementation(() => {});
    jest.spyOn(console, "debug").mockImplementation(() => {});
  });

  afterEach(() => {
    jest.restoreAllMocks();
  });

  describe("handleSignIn", () => {
    const createFormData = (email: string, password: string) => {
      const formData = new FormData();
      formData.append("email", email);
      formData.append("password", password);
      return formData;
    };

    it("handles successful sign in (DONE)", async () => {
      const formData = createFormData("test@example.com", "password123");
      
      mockSignIn.mockResolvedValueOnce({
        nextStep: {
          signInStep: "DONE",
        },
      } as any);

      const result = await handleSignIn(undefined, formData);

      expect(mockSignIn).toHaveBeenCalledWith({
        username: "test@example.com",
        password: "password123",
      });
      expect(mockSessionStorage.setItem).toHaveBeenCalledWith("signin-email", "test@example.com");
      expect(result).toEqual({
        success: true,
        redirectTo: Route.Dashboard,
      });
    });

    it("handles CONFIRM_SIGN_UP step", async () => {
      const formData = createFormData("test@example.com", "password123");
      
      mockSignIn.mockResolvedValueOnce({
        nextStep: {
          signInStep: "CONFIRM_SIGN_UP",
        },
      } as any);

      const result = await handleSignIn(undefined, formData);

      expect(mockResendSignUpCode).toHaveBeenCalledWith({ username: "test@example.com" });
      expect(result).toEqual({
        success: true,
        redirectTo: Route.ConfirmSignUp,
      });
    });

    it("handles CONFIRM_SIGN_IN_WITH_NEW_PASSWORD_REQUIRED step", async () => {
      const formData = createFormData("test@example.com", "password123");
      
      mockSignIn.mockResolvedValueOnce({
        nextStep: {
          signInStep: "CONFIRM_SIGN_IN_WITH_NEW_PASSWORD_REQUIRED",
        },
      } as any);

      const result = await handleSignIn(undefined, formData);

      expect(result).toEqual({
        success: true,
        redirectTo: Route.NewPassword,
      });
    });

    it("handles CONFIRM_SIGN_IN_WITH_SMS_CODE step", async () => {
      const formData = createFormData("test@example.com", "password123");
      
      mockSignIn.mockResolvedValueOnce({
        nextStep: {
          signInStep: "CONFIRM_SIGN_IN_WITH_SMS_CODE",
          codeDeliveryDetails: {
            destination: "+1234567890",
          },
        },
      } as any);

      const result = await handleSignIn(undefined, formData);

      expect(result).toEqual({
        success: true,
        redirectTo: Route.ConfirmSMSCode,
      });
    });

    it("handles unexpected sign in step", async () => {
      const formData = createFormData("test@example.com", "password123");
      
      mockSignIn.mockResolvedValueOnce({
        nextStep: {
          signInStep: "UNEXPECTED_STEP" as any,
        },
      } as any);

      const result = await handleSignIn(undefined, formData);

      expect(typeof result).toBe("string");
      expect(result).toContain("Unexpected next sign in step");
    });

    it("handles sign in errors", async () => {
      const formData = createFormData("test@example.com", "wrongpassword");
      
      mockSignIn.mockRejectedValueOnce(new Error("Invalid credentials"));

      const result = await handleSignIn(undefined, formData);

      expect(typeof result).toBe("string");
      expect(result).toBe("Invalid credentials");
    });

    it("handles non-Error exceptions", async () => {
      const formData = createFormData("test@example.com", "password123");
      
      mockSignIn.mockRejectedValueOnce("String error");

      const result = await handleSignIn(undefined, formData);

      expect(typeof result).toBe("string");
      expect(result).toBe("String error");
    });

    it("logs console messages correctly", async () => {
      const formData = createFormData("test@example.com", "password123");
      const consoleSpy = jest.spyOn(console, "log");
      
      mockSignIn.mockResolvedValueOnce({
        nextStep: {
          signInStep: "CONFIRM_SIGN_IN_WITH_SMS_CODE",
          codeDeliveryDetails: {
            destination: "+1234567890",
          },
        },
      } as any);

      await handleSignIn(undefined, formData);

      expect(consoleSpy).toHaveBeenCalledWith("Attempting sign in with username:", "test@example.com");
    });
  });

  describe("handleSignOut", () => {
    it("handles successful sign out", async () => {
      mockSignOut.mockResolvedValueOnce(undefined);

      await handleSignOut();

      expect(mockSignOut).toHaveBeenCalled();
      expect(mockRedirect).toHaveBeenCalledWith(Route.SignIn);
    });

    it("handles sign out errors", async () => {
      const consoleSpy = jest.spyOn(console, "log");
      mockSignOut.mockRejectedValueOnce(new Error("Sign out failed"));

      await handleSignOut();

      expect(consoleSpy).toHaveBeenCalledWith("Sign out failed");
      expect(mockRedirect).toHaveBeenCalledWith(Route.SignIn);
    });
  });

  describe("handleEmailCodeConfirmation", () => {
    const createCodeFormData = (code: string) => {
      const formData = new FormData();
      formData.append("code", code);
      return formData;
    };

    it("handles successful email code confirmation", async () => {
      const formData = createCodeFormData("123456");
      
      mockConfirmSignIn.mockResolvedValueOnce({
        nextStep: {
          signInStep: "DONE",
        },
      } as any);

      const result = await handleEmailCodeConfirmation(undefined, formData);

      expect(mockConfirmSignIn).toHaveBeenCalledWith({
        challengeResponse: "123456",
      });
      expect(result).toEqual({
        success: true,
        redirectTo: Route.Dashboard,
      });
    });

    it("handles unexpected next step after email verification", async () => {
      const formData = createCodeFormData("123456");
      
      mockConfirmSignIn.mockResolvedValueOnce({
        nextStep: {
          signInStep: "UNEXPECTED_STEP" as any,
        },
      } as any);

      const result = await handleEmailCodeConfirmation(undefined, formData);

      expect(typeof result).toBe("string");
      expect(result).toContain("Unexpected next step after email verification");
    });

    it("handles email code confirmation errors", async () => {
      const formData = createCodeFormData("wrong-code");
      
      mockConfirmSignIn.mockRejectedValueOnce(new Error("Invalid code"));

      const result = await handleEmailCodeConfirmation(undefined, formData);

      expect(typeof result).toBe("string");
      expect(result).toBe("Invalid code");
    });
  });

  describe("handleSMSCodeConfirmation", () => {
    const createCodeFormData = (code: string) => {
      const formData = new FormData();
      formData.append("code", code);
      return formData;
    };

    it("handles successful SMS code confirmation", async () => {
      const formData = createCodeFormData("654321");
      
      mockConfirmSignIn.mockResolvedValueOnce({
        nextStep: {
          signInStep: "DONE",
        },
      } as any);

      const result = await handleSMSCodeConfirmation(undefined, formData);

      expect(mockConfirmSignIn).toHaveBeenCalledWith({
        challengeResponse: "654321",
      });
      expect(result).toEqual({
        success: true,
        redirectTo: Route.Dashboard,
      });
    });

    it("handles unexpected next step after SMS verification", async () => {
      const formData = createCodeFormData("654321");
      
      mockConfirmSignIn.mockResolvedValueOnce({
        nextStep: {
          signInStep: "UNEXPECTED_STEP" as any,
        },
      } as any);

      const result = await handleSMSCodeConfirmation(undefined, formData);

      expect(typeof result).toBe("string");
      expect(result).toContain("Unexpected next step after SMS verification");
    });

    it("handles SMS code confirmation errors", async () => {
      const formData = createCodeFormData("wrong-code");
      
      mockConfirmSignIn.mockRejectedValueOnce(new Error("Invalid SMS code"));

      const result = await handleSMSCodeConfirmation(undefined, formData);

      expect(typeof result).toBe("string");
      expect(result).toBe("Invalid SMS code");
    });
  });

  describe("getErrorMessage", () => {
    it("returns message from Error object", () => {
      const error = new Error("Test error message");
      const result = getErrorMessage(error);
      expect(result).toBe("Test error message");
    });

    it("returns message from object with message property", () => {
      const error = { message: "Custom error message" };
      const result = getErrorMessage(error);
      expect(result).toBe("Custom error message");
    });

    it("returns string error as-is", () => {
      const error = "String error message";
      const result = getErrorMessage(error);
      expect(result).toBe("String error message");
    });

    it("returns default message for unknown error types", () => {
      const error = 123;
      const result = getErrorMessage(error);
      expect(result).toBe("An error occurred");
    });

    it("returns default message for null/undefined", () => {
      expect(getErrorMessage(null)).toBe("An error occurred");
      expect(getErrorMessage(undefined)).toBe("An error occurred");
    });

    it("handles object without message property", () => {
      const error = { code: 500, data: "some data" };
      const result = getErrorMessage(error);
      expect(result).toBe("An error occurred");
    });

    it("handles empty object", () => {
      const error = {};
      const result = getErrorMessage(error);
      expect(result).toBe("An error occurred");
    });
  });
});
