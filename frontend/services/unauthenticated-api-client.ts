/**
 * Unauthenticated HTTP client for public API endpoints
 *
 * This client handles requests that don't require authentication,
 * such as user signup and public endpoints.
 */

import { BaseApiError, BaseValidationError } from "./authenticated-api-client";

export class UnauthenticatedApiClient {
  private baseUrl: string;

  constructor() {
    this.baseUrl =
      process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080";
  }

  protected async request<T>(
    url: string,
    options: RequestInit = {},
  ): Promise<T> {
    const fullUrl = `${this.baseUrl}${url}`;

    // Default headers for JSON content
    const defaultHeaders = {
      "Content-Type": "application/json",
    };

    const finalConfig: RequestInit = {
      ...options,
      headers: {
        ...defaultHeaders,
        ...options.headers,
      },
    };

    console.group("🌐 Unauthenticated API Request");
    console.log("🎯 URL:", fullUrl);
    console.log("📋 Method:", finalConfig.method || "GET");
    console.log("📤 Headers:", finalConfig.headers);
    if (finalConfig.body) {
      console.log("📦 Body:", finalConfig.body);
    }

    try {
      const response = await fetch(fullUrl, finalConfig);

      console.log("📥 Response Status:", response.status, response.statusText);
      console.log(
        "📥 Response Headers:",
        Object.fromEntries(response.headers.entries()),
      );
      console.groupEnd();

      if (!response.ok) {
        await this.handleErrorResponse(response);
      }

      // Handle empty responses
      if (
        response.status === 204 ||
        response.headers.get("content-length") === "0"
      ) {
        return undefined as T;
      }

      // Try to parse JSON response
      try {
        return await response.json();
      } catch (parseError) {
        // If JSON parsing fails, return the text content
        const text = await response.text();
        return text as unknown as T;
      }
    } catch (error) {
      console.groupEnd();

      if (error instanceof BaseApiError) {
        throw error;
      }
      // Network or other errors
      throw new BaseApiError(0, "Network Error", [
        {
          message:
            error instanceof Error
              ? error.message
              : "An unexpected error occurred",
          timestamp: new Date().toISOString(),
        },
      ]);
    }
  }

  /**
   * Error handling for unauthenticated requests
   */
  protected async handleErrorResponse(response: Response): Promise<never> {
    console.error("API Error Response:", {
      status: response.status,
      statusText: response.statusText,
      url: response.url,
      headers: Object.fromEntries(response.headers.entries()),
    });

    let errorData: any;

    try {
      errorData = await response.json();
      console.error("Error Response Body:", errorData);
    } catch (parseError) {
      console.log("Failed to parse error response as JSON");

      // Try to get the response as text
      try {
        const textResponse = await response.text();
        console.log("Error Response Text:", textResponse);
      } catch (textError) {
        console.log("Failed to get error response as text:", textError);
      }

      // If JSON parsing fails, create a generic error
      throw new BaseApiError(response.status, response.statusText, [
        {
          message: "An unexpected error occurred",
          timestamp: new Date().toISOString(),
        },
      ]);
    }

    // Handle validation errors (400)
    if (response.status === 400 && errorData.errors) {
      const validationErrors = errorData.errors.map((error: any) => ({
        message: error.message || "Validation error",
        field: error.field || "",
        rejectedValue: error.rejectedValue,
        timestamp: errorData.timestamp || new Date().toISOString(),
      }));

      throw new BaseValidationError(validationErrors);
    }

    // Handle other API errors
    throw new BaseApiError(
      response.status,
      response.statusText,
      errorData.errors || [
        {
          message: errorData.message || "An error occurred",
          timestamp: errorData.timestamp || new Date().toISOString(),
        },
      ],
    );
  }

  // HTTP method implementations
  async get<T>(url: string): Promise<T> {
    return this.request<T>(url, { method: "GET" });
  }

  async post<T>(url: string, data: any): Promise<T> {
    return this.request<T>(url, {
      method: "POST",
      body: JSON.stringify(data),
    });
  }

  async put<T>(url: string, data: any): Promise<T> {
    return this.request<T>(url, {
      method: "PUT",
      body: JSON.stringify(data),
    });
  }

  async delete<T>(url: string): Promise<T> {
    return this.request<T>(url, { method: "DELETE" });
  }

  async patch<T>(url: string, data: any): Promise<T> {
    return this.request<T>(url, {
      method: "PATCH",
      body: JSON.stringify(data),
    });
  }
}
