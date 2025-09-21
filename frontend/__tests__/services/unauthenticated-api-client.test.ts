import { UnauthenticatedApiClient } from "../../services/unauthenticated-api-client";
import { BaseApiError, BaseValidationError } from "../../services/authenticated-api-client";

// Mock fetch globally
const mockFetch = global.fetch as jest.MockedFunction<typeof fetch>;

describe("UnauthenticatedApiClient", () => {
  let client: UnauthenticatedApiClient;

  beforeEach(() => {
    client = new UnauthenticatedApiClient();
    mockFetch.mockClear();
    
    // Mock console methods to reduce noise
    jest.spyOn(console, "error").mockImplementation(() => {});
    jest.spyOn(console, "log").mockImplementation(() => {});
    jest.spyOn(console, "group").mockImplementation(() => {});
    jest.spyOn(console, "groupEnd").mockImplementation(() => {});
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

      const result = await client.get("/api/public");

      expect(mockFetch).toHaveBeenCalledWith(
        "http://localhost:8080/api/public",
        expect.objectContaining({
          method: "GET",
          headers: {
            "Content-Type": "application/json",
          },
        })
      );
      expect(result).toEqual(mockData);
    });

    it("handles 204 No Content response", async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        status: 204,
        headers: new Headers(),
      } as Response);

      const result = await client.get("/api/public");

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

      const result = await client.get("/api/public");

      expect(result).toBeUndefined();
    });
  });

  describe("POST requests", () => {
    it("makes successful POST request", async () => {
      const requestData = { name: "New Item" };
      const responseData = { id: 2, name: "New Item" };

      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => responseData,
        headers: new Headers(),
        status: 201,
        statusText: "Created",
      } as Response);

      const result = await client.post("/api/signup", requestData);

      expect(mockFetch).toHaveBeenCalledWith(
        "http://localhost:8080/api/signup",
        expect.objectContaining({
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify(requestData),
        })
      );
      expect(result).toEqual(responseData);
    });
  });

  describe("PUT requests", () => {
    it("makes successful PUT request", async () => {
      const requestData = { id: 1, name: "Updated Item" };
      const responseData = { id: 1, name: "Updated Item" };

      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => responseData,
        headers: new Headers(),
        status: 200,
        statusText: "OK",
      } as Response);

      const result = await client.put("/api/public", requestData);

      expect(mockFetch).toHaveBeenCalledWith(
        "http://localhost:8080/api/public",
        expect.objectContaining({
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify(requestData),
        })
      );
      expect(result).toEqual(responseData);
    });
  });

  describe("DELETE requests", () => {
    it("makes successful DELETE request", async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        status: 204,
        headers: new Headers(),
      } as Response);

      const result = await client.delete("/api/public/1");

      expect(mockFetch).toHaveBeenCalledWith(
        "http://localhost:8080/api/public/1",
        expect.objectContaining({
          method: "DELETE",
          headers: {
            "Content-Type": "application/json",
          },
        })
      );
      expect(result).toBeUndefined();
    });
  });

  describe("PATCH requests", () => {
    it("makes successful PATCH request", async () => {
      const requestData = { name: "Patched Item" };
      const responseData = { id: 1, name: "Patched Item" };

      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => responseData,
        headers: new Headers(),
        status: 200,
        statusText: "OK",
      } as Response);

      const result = await client.patch("/api/public/1", requestData);

      expect(mockFetch).toHaveBeenCalledWith(
        "http://localhost:8080/api/public/1",
        expect.objectContaining({
          method: "PATCH",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify(requestData),
        })
      );
      expect(result).toEqual(responseData);
    });
  });

  describe("Error handling", () => {
    it("throws BaseValidationError for 400 with validation errors", async () => {
      const errorData = {
        errors: [
          {
            message: "Email is required",
            field: "email",
            rejectedValue: "",
          },
        ],
        timestamp: "2024-01-01T12:00:00Z",
      };

      mockFetch.mockResolvedValue({
        ok: false,
        status: 400,
        statusText: "Bad Request",
        json: async () => errorData,
        headers: new Headers(),
        url: "http://localhost:8080/api/signup",
      } as Response);
      
      try {
        await client.post("/api/signup", {});
      } catch (error) {
        expect(error).toBeInstanceOf(BaseValidationError);
        const validationError = error as BaseValidationError;
        expect(validationError.status).toBe(400);
        expect(validationError.validationErrors[0].message).toBe("Email is required");
        expect(validationError.validationErrors[0].field).toBe("email");
      }
    });

    it("throws BaseApiError for other HTTP errors", async () => {
      const errorData = {
        message: "User not found",
        timestamp: "2024-01-01T12:00:00Z",
      };

      mockFetch.mockResolvedValue({
        ok: false,
        status: 404,
        statusText: "Not Found",
        json: async () => errorData,
        headers: new Headers(),
        url: "http://localhost:8080/api/public/user/999",
      } as Response);
      
      try {
        await client.get("/api/public/user/999");
      } catch (error) {
        expect(error).toBeInstanceOf(BaseApiError);
        const apiError = error as BaseApiError;
        expect(apiError.status).toBe(404);
        expect(apiError.statusText).toBe("Not Found");
        expect(apiError.errors[0].message).toBe("User not found");
      }
    });

    it("handles JSON parsing errors in error response", async () => {
      mockFetch.mockResolvedValue({
        ok: false,
        status: 500,
        statusText: "Internal Server Error",
        json: async () => {
          throw new Error("Invalid JSON");
        },
        text: async () => "Internal Server Error",
        headers: new Headers(),
        url: "http://localhost:8080/api/signup",
      } as unknown as Response);
      
      try {
        await client.post("/api/signup", {});
      } catch (error) {
        expect(error).toBeInstanceOf(BaseApiError);
        const apiError = error as BaseApiError;
        expect(apiError.status).toBe(500);
        expect(apiError.errors[0].message).toBe("An unexpected error occurred");
      }
    });

    it("handles network errors", async () => {
      mockFetch.mockRejectedValue(new Error("Network failure"));
      
      try {
        await client.get("/api/public");
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

      const result = await client.get("/api/public");
      expect(result).toBe("Plain text response");
    });

    it("handles errors without detailed error data", async () => {
      const errorData = {
        message: "Generic error",
      };

      mockFetch.mockResolvedValueOnce({
        ok: false,
        status: 500,
        statusText: "Internal Server Error",
        json: async () => errorData,
        headers: new Headers(),
        url: "http://localhost:8080/api/public",
      } as Response);

      try {
        await client.get("/api/public");
      } catch (error) {
        expect(error).toBeInstanceOf(BaseApiError);
        const apiError = error as BaseApiError;
        expect(apiError.errors[0].message).toBe("Generic error");
      }
    });

    it("handles missing error message gracefully", async () => {
      const errorData = {};

      mockFetch.mockResolvedValueOnce({
        ok: false,
        status: 422,
        statusText: "Unprocessable Entity",
        json: async () => errorData,
        headers: new Headers(),
        url: "http://localhost:8080/api/signup",
      } as Response);

      try {
        await client.post("/api/signup", {});
      } catch (error) {
        expect(error).toBeInstanceOf(BaseApiError);
        const apiError = error as BaseApiError;
        expect(apiError.errors[0].message).toBe("An error occurred");
      }
    });
  });

  describe("Configuration", () => {
    it("uses custom base URL from environment", () => {
      const originalEnv = process.env.NEXT_PUBLIC_API_BASE_URL;
      process.env.NEXT_PUBLIC_API_BASE_URL = "https://api.example.com";
      
      const customClient = new UnauthenticatedApiClient();
      
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({}),
        headers: new Headers(),
        status: 200,
      } as Response);

      customClient.get("/test");

      expect(mockFetch).toHaveBeenCalledWith(
        "https://api.example.com/test",
        expect.any(Object)
      );

      process.env.NEXT_PUBLIC_API_BASE_URL = originalEnv;
    });

    it("merges custom headers with default headers", async () => {
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
            "Custom-Header": "custom-value",
          }),
        })
      );
    });

    it("logs request and response details", async () => {
      const consoleSpy = jest.spyOn(console, "log");
      
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ success: true }),
        headers: new Headers({ "Content-Type": "application/json" }),
        status: 200,
        statusText: "OK",
      } as Response);

      await client.get("/api/public");

      // Verify that logging occurs (console methods are mocked but we can verify they were called)
      expect(consoleSpy).toHaveBeenCalled();
    });

    it("handles text response error parsing failure", async () => {
      mockFetch.mockResolvedValueOnce({
        ok: false,
        status: 500,
        statusText: "Internal Server Error",
        json: async () => {
          throw new Error("Invalid JSON");
        },
        text: async () => {
          throw new Error("Text parse error");
        },
        headers: new Headers(),
        url: "http://localhost:8080/api/public",
      } as unknown as Response);

      try {
        await client.get("/api/public");
      } catch (error) {
        expect(error).toBeInstanceOf(BaseApiError);
        const apiError = error as BaseApiError;
        expect(apiError.status).toBe(500);
        expect(apiError.errors[0].message).toBe("An unexpected error occurred");
      }
    });
  });
});
