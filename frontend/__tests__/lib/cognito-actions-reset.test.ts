import { handleVerifyResetCode, handleResetPassword } from "../../lib/cognito-actions";
import { confirmResetPassword } from "aws-amplify/auth";
import { Route } from "../../enums/Route";

// Mock AWS Amplify auth
jest.mock("aws-amplify/auth", () => ({
  confirmResetPassword: jest.fn(),
}));

// Mock sessionStorage
const mockSessionStorage = {
  getItem: jest.fn(),
  setItem: jest.fn(),
  removeItem: jest.fn(),
};
Object.defineProperty(window, 'sessionStorage', {
  value: mockSessionStorage,
  writable: true,
});

const mockConfirmResetPassword = confirmResetPassword as jest.MockedFunction<typeof confirmResetPassword>;

describe("handleVerifyResetCode", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("validates code format and stores code for valid 6-digit code", async () => {
    mockSessionStorage.getItem.mockReturnValue("test@example.com");
    
    const formData = new FormData();
    formData.append("code", "123456");
    
    const result = await handleVerifyResetCode(undefined, formData);
    
    expect(result).toEqual({ success: true, codeVerified: true });
    expect(mockSessionStorage.setItem).toHaveBeenCalledWith("verified-reset-code", "123456");
  });

  it("rejects invalid code format", async () => {
    mockSessionStorage.getItem.mockReturnValue("test@example.com");
    
    const formData = new FormData();
    formData.append("code", "12345"); // Only 5 digits
    
    const result = await handleVerifyResetCode(undefined, formData);
    
    expect(result).toBe("Please enter a valid 6-digit verification code.");
    expect(mockSessionStorage.setItem).not.toHaveBeenCalled();
  });

  it("rejects non-numeric code", async () => {
    mockSessionStorage.getItem.mockReturnValue("test@example.com");
    
    const formData = new FormData();
    formData.append("code", "12345a");
    
    const result = await handleVerifyResetCode(undefined, formData);
    
    expect(result).toBe("Please enter a valid 6-digit verification code.");
    expect(mockSessionStorage.setItem).not.toHaveBeenCalled();
  });

  it("handles session expiration", async () => {
    mockSessionStorage.getItem.mockReturnValue(null); // No email stored
    
    const formData = new FormData();
    formData.append("code", "123456");
    
    const result = await handleVerifyResetCode(undefined, formData);
    
    expect(result).toBe("Session expired. Please restart the password reset process.");
  });
});

describe("handleResetPassword with verified code", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("successfully resets password with stored code", async () => {
    mockSessionStorage.getItem
      .mockReturnValueOnce("test@example.com") // reset-email
      .mockReturnValueOnce("123456"); // verified-reset-code
    
    mockConfirmResetPassword.mockResolvedValue(undefined as any);
    
    const formData = new FormData();
    formData.append("password", "NewPass123!");
    
    const result = await handleResetPassword(undefined, formData);
    
    expect(mockConfirmResetPassword).toHaveBeenCalledWith({
      username: "test@example.com",
      confirmationCode: "123456",
      newPassword: "NewPass123!",
    });
    
    expect(result).toEqual({ success: true, redirectTo: Route.SignIn });
    expect(mockSessionStorage.removeItem).toHaveBeenCalledWith("reset-email");
    expect(mockSessionStorage.removeItem).toHaveBeenCalledWith("verified-reset-code");
  });

  it("handles missing email session", async () => {
    mockSessionStorage.getItem
      .mockReturnValueOnce(null) // reset-email missing
      .mockReturnValueOnce("123456"); // verified-reset-code
    
    const formData = new FormData();
    formData.append("password", "NewPass123!");
    
    const result = await handleResetPassword(undefined, formData);
    
    expect(result).toBe("Session expired. Please restart the password reset process.");
  });

  it("handles missing verified code", async () => {
    mockSessionStorage.getItem
      .mockReturnValueOnce("test@example.com") // reset-email
      .mockReturnValueOnce(null); // verified-reset-code missing
    
    const formData = new FormData();
    formData.append("password", "NewPass123!");
    
    const result = await handleResetPassword(undefined, formData);
    
    expect(result).toBe("Code verification required. Please verify your code first.");
  });

  it("clears verified code on reset failure", async () => {
    mockSessionStorage.getItem
      .mockReturnValueOnce("test@example.com") // reset-email
      .mockReturnValueOnce("123456"); // verified-reset-code
    
    const error = new Error("Invalid or expired confirmation code");
    mockConfirmResetPassword.mockRejectedValue(error);
    
    const formData = new FormData();
    formData.append("password", "NewPass123!");
    
    const result = await handleResetPassword(undefined, formData);
    
    expect(result).toBe("Invalid or expired confirmation code");
    expect(mockSessionStorage.removeItem).toHaveBeenCalledWith("verified-reset-code");
  });
});