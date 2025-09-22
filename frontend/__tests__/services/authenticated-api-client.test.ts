import {
  AuthenticatedApiClient,
  BaseApiError,
  BaseValidationError,
} from "../../services/authenticated-api-client";
import { createAuthenticatedRequestConfig } from "@/lib/auth-utils";

// Mock the auth-utils module
jest.mock("@/lib/auth-utils", () => ({
  createAuthenticatedRequestConfig: jest.fn(),
}));

const mockCreateAuthenticatedRequestConfig = createAuthenticatedRequestConfig as jest.MockedFunction<
  typeof createAuthenticatedRequestConfig
>;

// Mock fetch globally
const mockFetch = global.fetch as jest.MockedFunction<typeof fetch>;

describe("AuthenticatedApiClient", () => {
  let client: AuthenticatedApiClient;

  const mockAuthConfig = {
    method: "GET" as const,
    headers: {
      "Content-Type": "application/json",
      Authorization: "Bearer mock-token",
      accept: "*/*",
    },
  };

  beforeEach(() => {
    client = new AuthenticatedApiClient();
    mockCreateAuthenticatedRequestConfig.mockClear();
    mockFetch.mockClear();
    mockCreateAuthenticatedRequestConfig.mockResolvedValue(mockAuthConfig);
    
    // Mock console methods to reduce noise
    jest.spyOn(console, "error").mockImplementation(() => {});
    jest.spyOn(console, "log").mockImplementation(() => {});
  });

  afterEach(() => {
    jest.restoreAllMocks();
  });

  describe("GET requests", () => {
    it("makes successful GET request", async () => {
      const mockData = { id: 1, name: "Test" };
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockData,
        headers: new Headers(),
        status: 200,
        statusText: "OK",
      } as Response);

      const result = await client.get("/api/test");

      expect(mockCreateAuthenticatedRequestConfig).toHaveBeenCalledWith("GET", undefined);
      expect(mockFetch).toHaveBeenCalledWith(
        "http://localhost:8080/api/test",
        mockAuthConfig
      );
      expect(result).toEqual(mockData);
    });

    it("handles 204 No Content response", async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        status: 204,
        headers: new Headers(),
      } as Response);

      const result = await client.get("/api/test");

      expect(result).toBeUndefined();
    });

    it("handles empty content-length response", async () => {
      const headers = new Headers();
      headers.set("content-length", "0");
      
      mockFetch.mockResolvedValueOnce({
        ok: true,
        status: 200,
        headers,
      } as Response);

      const result = await client.get("/api/test");

      expect(result).toBeUndefined();
    });
  });

  describe("POST requests", () => {
    it("makes successful POST request", async () => {
      const requestData = { name: "New Item" };
      const responseData = { id: 2, name: "New Item" };

      mockCreateAuthenticatedRequestConfig.mockResolvedValueOnce({
        method: "POST",
        headers: mockAuthConfig.headers,
        body: JSON.stringify(requestData),
      });

      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => responseData,
        headers: new Headers(),
        status: 201,
        statusText: "Created",
      } as Response);

      const result = await client.post("/api/test", requestData);

      expect(mockCreateAuthenticatedRequestConfig).toHaveBeenCalledWith("POST", requestData);
      expect(result).toEqual(responseData);
    });
  });

  describe("PUT requests", () => {
    it("makes successful PUT request", async () => {
      const requestData = { id: 1, name: "Updated Item" };
      const responseData = { id: 1, name: "Updated Item" };

      mockCreateAuthenticatedRequestConfig.mockResolvedValueOnce({
        method: "PUT",
        headers: mockAuthConfig.headers,
        body: JSON.stringify(requestData),
      });

      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => responseData,
        headers: new Headers(),
        status: 200,
        statusText: "OK",
      } as Response);

      const result = await client.put("/api/test", requestData);

      expect(mockCreateAuthenticatedRequestConfig).toHaveBeenCalledWith("PUT", requestData);
      expect(result).toEqual(responseData);
    });
  });

  describe("DELETE requests", () => {
    it("makes successful DELETE request", async () => {
      mockCreateAuthenticatedRequestConfig.mockResolvedValueOnce({
        method: "DELETE",
        headers: mockAuthConfig.headers,
      });

      mockFetch.mockResolvedValueOnce({
        ok: true,
        status: 204,
        headers: new Headers(),
      } as Response);

      const result = await client.delete("/api/test/1");

      expect(mockCreateAuthenticatedRequestConfig).toHaveBeenCalledWith("DELETE", undefined);
      expect(result).toBeUndefined();
    });
  });

  describe("PATCH requests", () => {
    it("makes successful PATCH request", async () => {
      const requestData = { name: "Patched Item" };
      const responseData = { id: 1, name: "Patched Item" };

      mockCreateAuthenticatedRequestConfig.mockResolvedValueOnce({
        method: "PATCH",
        headers: mockAuthConfig.headers,
        body: JSON.stringify(requestData),
      });

      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => responseData,
        headers: new Headers(),
        status: 200,
        statusText: "OK",
      } as Response);

      const result = await client.patch("/api/test/1", requestData);

      expect(mockCreateAuthenticatedRequestConfig).toHaveBeenCalledWith("PATCH", requestData);
      expect(result).toEqual(responseData);
    });
  });

  describe("Error handling", () => {
    it("throws BaseValidationError for 400 with validation errors", async () => {
      const errorData = {
        errors: [
          {
            message: "Field is required",
            field: "email",
            rejectedValue: "",
          },
        ],
        timestamp: "2024-01-01T12:00:00Z",
      };

      mockFetch.mockResolvedValueOnce({
        ok: false,
        status: 400,
        statusText: "Bad Request",
        json: jest.fn().mockResolvedValueOnce(errorData),
        headers: new Headers(),
      } as unknown as Response);
      
      try {
        await client.get("/api/test");
      } catch (error) {
        expect(error).toBeInstanceOf(BaseValidationError);
        const validationError = error as BaseValidationError;
        expect(validationError.status).toBe(400);
        expect(validationError.validationErrors[0].message).toBe("Field is required");
        expect(validationError.validationErrors[0].field).toBe("email");
      }
    });

    it("throws BaseApiError for other HTTP errors", async () => {
      const errorData = {
        message: "Not found",
        timestamp: "2024-01-01T12:00:00Z",
      };

      mockFetch.mockResolvedValueOnce({
        ok: false,
        status: 404,
        statusText: "Not Found",
        json: jest.fn().mockResolvedValueOnce(errorData),
        headers: new Headers(),
      } as unknown as Response);
      
      try {
        await client.get("/api/test");
      } catch (error) {
        expect(error).toBeInstanceOf(BaseApiError);
        const apiError = error as BaseApiError;
        expect(apiError.status).toBe(404);
        expect(apiError.statusText).toBe("Not Found");
        expect(apiError.errors[0].message).toBe("Not found");
      }
    });

    it("handles JSON parsing errors in error response", async () => {
      mockFetch.mockResolvedValueOnce({
        ok: false,
        status: 500,
        statusText: "Internal Server Error",
        json: jest.fn().mockRejectedValueOnce(new Error("Invalid JSON")),
        text: jest.fn().mockResolvedValueOnce("Server Error"),
        headers: new Headers(),
      } as unknown as Response);
      
      try {
        await client.get("/api/test");
        fail("Expected error to be thrown");
      } catch (error) {
        expect(error).toBeInstanceOf(BaseApiError);
        const apiError = error as BaseApiError;
        expect(apiError.status).toBe(500);
        expect(apiError.errors[0].message).toBe("An unexpected error occurred");
      }
    });

    it("handles network errors", async () => {
      mockFetch.mockRejectedValueOnce(new Error("Network failure"));
      
      try {
        await client.get("/api/test");
      } catch (error) {
        expect(error).toBeInstanceOf(BaseApiError);
        const apiError = error as BaseApiError;
        expect(apiError.status).toBe(0);
        expect(apiError.statusText).toBe("Network Error");
        expect(apiError.errors[0].message).toBe("Network failure");
      }
    });

    it("handles text response when JSON parsing fails for success", async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: async () => {
          throw new Error("Invalid JSON");
        },
        text: async () => "Plain text response",
        headers: new Headers(),
      } as unknown as Response);

      const result = await client.get("/api/test");
      expect(result).toBe("Plain text response");
    });

    it("uses custom base URL from environment", async () => {
      const originalEnv = process.env.NEXT_PUBLIC_API_BASE_URL;
      process.env.NEXT_PUBLIC_API_BASE_URL = "https://api.example.com";
      
      const customClient = new AuthenticatedApiClient();
      
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({}),
        headers: new Headers(),
        status: 200,
      } as unknown as Response);

      await customClient.get("/test");

      expect(mockFetch).toHaveBeenCalledWith(
        "https://api.example.com/test",
        expect.any(Object)
      );

      process.env.NEXT_PUBLIC_API_BASE_URL = originalEnv;
    });

    it("merges custom headers with auth config", async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({}),
        headers: new Headers(),
        status: 200,
      } as Response);

      // Access protected request method through any to test header merging
      const protectedClient = client as any;
      await protectedClient.request("/test", {
        headers: {
          "Custom-Header": "custom-value",
        },
      });

      expect(mockFetch).toHaveBeenCalledWith(
        expect.any(String),
        expect.objectContaining({
          headers: expect.objectContaining({
            "Content-Type": "application/json",
            Authorization: "Bearer mock-token",
            "Custom-Header": "custom-value",
          }),
        })
      );
    });
  });

  describe("BaseApiError", () => {
    it("creates error with all properties", () => {
      const errors = [
        {
          message: "Test error",
          timestamp: "2024-01-01T12:00:00Z",
          field: "testField",
          rejectedValue: "invalid",
        },
      ];

      const error = new BaseApiError(400, "Bad Request", errors, "2024-01-01T12:00:00Z");

      expect(error.status).toBe(400);
      expect(error.statusText).toBe("Bad Request");
      expect(error.errors).toEqual(errors);
      expect(error.timestamp).toBe("2024-01-01T12:00:00Z");
      expect(error.message).toBe("API Error 400: Bad Request");
      expect(error.name).toBe("BaseApiError");
    });

    it("creates error with minimal properties", () => {
      const error = new BaseApiError(500, "Internal Server Error");

      expect(error.status).toBe(500);
      expect(error.statusText).toBe("Internal Server Error");
      expect(error.errors).toEqual([]);
      expect(error.timestamp).toBeUndefined();
    });
  });

  describe("BaseValidationError", () => {
    it("creates validation error with all properties", () => {
      const validationErrors = [
        {
          message: "Field is required",
          field: "email",
          rejectedValue: "",
          timestamp: "2024-01-01T12:00:00Z",
        },
      ];

      const error = new BaseValidationError(validationErrors, 422, "Unprocessable Entity");

      expect(error.validationErrors).toEqual(validationErrors);
      expect(error.status).toBe(422);
      expect(error.statusText).toBe("Unprocessable Entity");
      expect(error.name).toBe("BaseValidationError");
    });

    it("creates validation error with default status", () => {
      const validationErrors = [
        {
          message: "Invalid value",
          field: "name",
          timestamp: "2024-01-01T12:00:00Z",
        },
      ];

      const error = new BaseValidationError(validationErrors);

      expect(error.status).toBe(400);
      expect(error.statusText).toBe("Validation Error");
    });
  });
});
