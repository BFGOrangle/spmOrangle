import {
  getBearerToken,
  createAuthenticatedRequestConfig,
} from "../../lib/auth-utils";
import { fetchAuthSession } from "aws-amplify/auth";

// Mock AWS Amplify Auth
jest.mock("aws-amplify/auth", () => ({
  fetchAuthSession: jest.fn(),
}));

const mockFetchAuthSession = fetchAuthSession as jest.MockedFunction<typeof fetchAuthSession>;

describe("Auth Utils", () => {
  beforeEach(() => {
    mockFetchAuthSession.mockClear();
    
    // Mock console.error
    jest.spyOn(console, "error").mockImplementation(() => {});
  });

  afterEach(() => {
    jest.restoreAllMocks();
  });

  describe("getBearerToken", () => {
    it("returns bearer token when session has access token", async () => {
      const mockSession = {
        tokens: {
          accessToken: "mock-access-token-123",
        },
      };

      mockFetchAuthSession.mockResolvedValueOnce(mockSession as any);

      const result = await getBearerToken();

      expect(result).toBe("Bearer mock-access-token-123");
      expect(mockFetchAuthSession).toHaveBeenCalled();
    });

    it("returns empty string when session is null", async () => {
      mockFetchAuthSession.mockResolvedValueOnce(null as any);

      const result = await getBearerToken();

      expect(result).toBe("");
      expect(console.error).toHaveBeenCalledWith("Error in retrieving access token.");
    });

    it("returns empty string when session has no tokens", async () => {
      const mockSession = {
        tokens: null,
      };

      mockFetchAuthSession.mockResolvedValueOnce(mockSession as any);

      const result = await getBearerToken();

      expect(result).toBe("");
      expect(console.error).toHaveBeenCalledWith("Error in retrieving access token.");
    });

    it("returns empty string when session tokens have no access token", async () => {
      const mockSession = {
        tokens: {
          accessToken: null,
        },
      };

      mockFetchAuthSession.mockResolvedValueOnce(mockSession as any);

      const result = await getBearerToken();

      expect(result).toBe("");
      expect(console.error).toHaveBeenCalledWith("Error in retrieving access token.");
    });

    it("returns empty string when fetchAuthSession throws error", async () => {
      mockFetchAuthSession.mockRejectedValueOnce(new Error("Auth session failed"));

      const result = await getBearerToken();

      expect(result).toBe("");
      expect(console.error).toHaveBeenCalledWith("Error in retrieving access token.");
    });

    it("handles undefined session", async () => {
      mockFetchAuthSession.mockResolvedValueOnce(undefined as any);

      const result = await getBearerToken();

      expect(result).toBe("");
      expect(console.error).toHaveBeenCalledWith("Error in retrieving access token.");
    });
  });

  describe("createAuthenticatedRequestConfig", () => {
    it("creates config for GET request without body", async () => {
      const mockSession = {
        tokens: {
          accessToken: "test-token-456",
        },
      };

      mockFetchAuthSession.mockResolvedValueOnce(mockSession as any);

      const result = await createAuthenticatedRequestConfig("GET");

      expect(result).toEqual({
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          Authorization: "Bearer test-token-456",
          accept: "*/*",
        },
      });
    });

    it("creates config for POST request with body", async () => {
      const mockSession = {
        tokens: {
          accessToken: "test-token-789",
        },
      };

      const requestBody = { name: "John", email: "john@example.com" };

      mockFetchAuthSession.mockResolvedValueOnce(mockSession as any);

      const result = await createAuthenticatedRequestConfig("POST", requestBody);

      expect(result).toEqual({
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: "Bearer test-token-789",
          accept: "*/*",
        },
        body: JSON.stringify(requestBody),
      });
    });

    it("creates config for PUT request with body", async () => {
      const mockSession = {
        tokens: {
          accessToken: "put-token-123",
        },
      };

      const requestBody = { id: 1, name: "Updated Name" };

      mockFetchAuthSession.mockResolvedValueOnce(mockSession as any);

      const result = await createAuthenticatedRequestConfig("PUT", requestBody);

      expect(result).toEqual({
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: "Bearer put-token-123",
          accept: "*/*",
        },
        body: JSON.stringify(requestBody),
      });
    });

    it("creates config for DELETE request without body", async () => {
      const mockSession = {
        tokens: {
          accessToken: "delete-token-456",
        },
      };

      mockFetchAuthSession.mockResolvedValueOnce(mockSession as any);

      const result = await createAuthenticatedRequestConfig("DELETE");

      expect(result).toEqual({
        method: "DELETE",
        headers: {
          "Content-Type": "application/json",
          Authorization: "Bearer delete-token-456",
          accept: "*/*",
        },
      });
    });

    it("creates config for PATCH request with body", async () => {
      const mockSession = {
        tokens: {
          accessToken: "patch-token-789",
        },
      };

      const requestBody = { status: "active" };

      mockFetchAuthSession.mockResolvedValueOnce(mockSession as any);

      const result = await createAuthenticatedRequestConfig("PATCH", requestBody);

      expect(result).toEqual({
        method: "PATCH",
        headers: {
          "Content-Type": "application/json",
          Authorization: "Bearer patch-token-789",
          accept: "*/*",
        },
        body: JSON.stringify(requestBody),
      });
    });

    it("uses default GET method when no method specified", async () => {
      const mockSession = {
        tokens: {
          accessToken: "default-token-123",
        },
      };

      mockFetchAuthSession.mockResolvedValueOnce(mockSession as any);

      const result = await createAuthenticatedRequestConfig();

      expect(result).toEqual({
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          Authorization: "Bearer default-token-123",
          accept: "*/*",
        },
      });
    });

    it("handles empty bearer token gracefully", async () => {
      mockFetchAuthSession.mockResolvedValueOnce(null as any);

      const result = await createAuthenticatedRequestConfig("GET");

      expect(result).toEqual({
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          Authorization: "",
          accept: "*/*",
        },
      });
    });

    it("handles complex request body", async () => {
      const mockSession = {
        tokens: {
          accessToken: "complex-token-456",
        },
      };

      const complexBody = {
        user: {
          id: 1,
          profile: {
            firstName: "John",
            lastName: "Doe",
            preferences: ["email", "sms"],
          },
        },
        metadata: {
          timestamp: new Date().toISOString(),
          source: "web-app",
        },
      };

      mockFetchAuthSession.mockResolvedValueOnce(mockSession as any);

      const result = await createAuthenticatedRequestConfig("POST", complexBody);

      expect(result.body).toBe(JSON.stringify(complexBody));
      expect(result.method).toBe("POST");
    });

    it("handles undefined body correctly", async () => {
      const mockSession = {
        tokens: {
          accessToken: "undefined-body-token",
        },
      };

      mockFetchAuthSession.mockResolvedValueOnce(mockSession as any);

      const result = await createAuthenticatedRequestConfig("POST", undefined);

      expect(result).not.toHaveProperty("body");
    });

    it("handles null body correctly", async () => {
      const mockSession = {
        tokens: {
          accessToken: "null-body-token",
        },
      };

      mockFetchAuthSession.mockResolvedValueOnce(mockSession as any);

      const result = await createAuthenticatedRequestConfig("POST", undefined);

      expect(result).not.toHaveProperty("body");
    });

    it("handles empty object body", async () => {
      const mockSession = {
        tokens: {
          accessToken: "empty-body-token",
        },
      };

      mockFetchAuthSession.mockResolvedValueOnce(mockSession as any);

      const result = await createAuthenticatedRequestConfig("POST", {});

      expect(result.body).toBe(JSON.stringify({}));
    });
  });
});
