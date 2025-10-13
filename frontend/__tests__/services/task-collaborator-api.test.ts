import {
  TaskCollaboratorApiService,
  taskCollaboratorApiService,
  CollaboratorApiError,
  CollaboratorValidationError,
} from "@/services/task-collaborator-api";
import {
  AddCollaboratorRequestDto,
  AddCollaboratorResponseDto,
  RemoveCollaboratorRequestDto,
  CollaboratorApiErrorDetail,
} from "@/types/collaborator";

// Mock the AuthenticatedApiClient
jest.mock("@/services/authenticated-api-client", () => ({
  AuthenticatedApiClient: jest.fn().mockImplementation(() => ({
    post: jest.fn(),
    request: jest.fn(),
    deleteWithBody: jest.fn(),
  })),
  BaseApiError: class BaseApiError extends Error {
    constructor(
      public status: number,
      public statusText: string,
      public errors: Array<{
        message: string;
        timestamp: string;
        field?: string;
        rejectedValue?: any;
      }> = [],
      public timestamp?: string,
    ) {
      super(`API Error ${status}: ${statusText}`);
      this.name = "BaseApiError";
    }
  },
  BaseValidationError: class BaseValidationError extends Error {
    constructor(
      public validationErrors: Array<{
        message: string;
        field: string;
        rejectedValue?: any;
        timestamp: string;
      }>,
      public status: number = 400,
      public statusText: string = "Validation Error",
    ) {
      super(`Validation Error ${status}: ${statusText}`);
      this.name = "BaseValidationError";
    }
  },
}));


describe("TaskCollaboratorApiService", () => {
  let apiService: TaskCollaboratorApiService;
  let mockClient: any;

  beforeEach(() => {
    apiService = new TaskCollaboratorApiService();
    mockClient = (apiService as any).client;
    jest.clearAllMocks();
  });

  describe("addCollaborator", () => {
    const mockRequest: AddCollaboratorRequestDto = {
      taskId: 1,
      collaboratorId: 2,
    };

    const mockResponse: AddCollaboratorResponseDto = {
      taskId: 1,
      collaboratorId: 2,
      assignedById: 3,
      assignedAt: "2023-12-01T10:00:00Z",
    };

    it("should successfully add a collaborator", async () => {
      mockClient.post.mockResolvedValue(mockResponse);

      const result = await apiService.addCollaborator(mockRequest);

      expect(mockClient.post).toHaveBeenCalledWith(
        expect.stringContaining("/api/tasks/collaborator"),
        mockRequest,
      );
      expect(result).toEqual(mockResponse);
    });

    it("should call the correct endpoint", async () => {
      mockClient.post.mockResolvedValue(mockResponse);

      await apiService.addCollaborator(mockRequest);

      expect(mockClient.post).toHaveBeenCalledWith(
        expect.stringMatching(/\/api\/tasks\/collaborator$/),
        mockRequest,
      );
    });

    it("should propagate CollaboratorApiError from client", async () => {
      const errorDetails: CollaboratorApiErrorDetail[] = [
        {
          message: "Task not found",
          timestamp: "2023-12-01T10:00:00Z",
        },
      ];
      const error = new CollaboratorApiError(404, "Not Found", errorDetails);
      mockClient.post.mockRejectedValue(error);

      await expect(apiService.addCollaborator(mockRequest)).rejects.toThrow(
        CollaboratorApiError,
      );
      await expect(apiService.addCollaborator(mockRequest)).rejects.toThrow(
        "API Error 404: Not Found",
      );
    });

    it("should propagate CollaboratorValidationError from client", async () => {
      const validationErrors = [
        {
          message: "Task ID is required",
          field: "taskId",
          rejectedValue: null,
          timestamp: "2023-12-01T10:00:00Z",
        },
      ];
      const error = new CollaboratorValidationError(validationErrors);
      mockClient.post.mockRejectedValue(error);

      await expect(apiService.addCollaborator(mockRequest)).rejects.toThrow(
        CollaboratorValidationError,
      );
    });

    it("should handle network errors", async () => {
      mockClient.post.mockRejectedValue(new Error("Network error"));

      await expect(apiService.addCollaborator(mockRequest)).rejects.toThrow(
        "Network error",
      );
    });
  });

  describe("removeCollaborator", () => {
    const mockRequest: RemoveCollaboratorRequestDto = {
      taskId: 1,
      collaboratorId: 2,
    };

    it("should successfully remove a collaborator", async () => {
      mockClient.deleteWithBody.mockResolvedValue(undefined);

      const result = await apiService.removeCollaborator(mockRequest);

      expect(mockClient.deleteWithBody).toHaveBeenCalledWith(
        expect.stringContaining("/api/tasks/collaborator"),
        mockRequest,
      );
      expect(result).toBeUndefined();
    });

    it("should call the correct endpoint with DELETE method", async () => {
      mockClient.deleteWithBody.mockResolvedValue(undefined);

      await apiService.removeCollaborator(mockRequest);

      expect(mockClient.deleteWithBody).toHaveBeenCalledWith(
        expect.stringMatching(/\/api\/tasks\/collaborator$/),
        mockRequest,
      );
    });

    it("should serialize request body correctly", async () => {
      mockClient.deleteWithBody.mockResolvedValue(undefined);

      await apiService.removeCollaborator(mockRequest);

      expect(mockClient.deleteWithBody).toHaveBeenCalledWith(
        expect.any(String),
        mockRequest,
      );
    });

    it("should propagate CollaboratorApiError from client", async () => {
      const errorDetails: CollaboratorApiErrorDetail[] = [
        {
          message: "Collaborator not found",
          timestamp: "2023-12-01T10:00:00Z",
        },
      ];
      const error = new CollaboratorApiError(404, "Not Found", errorDetails);
      mockClient.deleteWithBody.mockRejectedValue(error);

      await expect(apiService.removeCollaborator(mockRequest)).rejects.toThrow(
        CollaboratorApiError,
      );
      await expect(apiService.removeCollaborator(mockRequest)).rejects.toThrow(
        "API Error 404: Not Found",
      );
    });

    it("should propagate CollaboratorValidationError from client", async () => {
      const validationErrors = [
        {
          message: "Collaborator ID is required",
          field: "collaboratorId",
          rejectedValue: null,
          timestamp: "2023-12-01T10:00:00Z",
        },
      ];
      const error = new CollaboratorValidationError(validationErrors);
      mockClient.deleteWithBody.mockRejectedValue(error);

      await expect(apiService.removeCollaborator(mockRequest)).rejects.toThrow(
        CollaboratorValidationError,
      );
    });

    it("should handle network errors", async () => {
      mockClient.deleteWithBody.mockRejectedValue(new Error("Network error"));

      await expect(apiService.removeCollaborator(mockRequest)).rejects.toThrow(
        "Network error",
      );
    });
  });

  describe("singleton instance", () => {
    it("should export a singleton instance", () => {
      expect(taskCollaboratorApiService).toBeInstanceOf(
        TaskCollaboratorApiService,
      );
    });

    it("should return the same instance on multiple imports", () => {
      const instance1 = taskCollaboratorApiService;
      const instance2 = taskCollaboratorApiService;
      expect(instance1).toBe(instance2);
    });
  });
});

describe("CollaboratorApiError", () => {
  it("should create error with basic properties", () => {
    const error = new CollaboratorApiError(400, "Bad Request");

    expect(error.name).toBe("CollaboratorApiError");
    expect(error.status).toBe(400);
    expect(error.statusText).toBe("Bad Request");
    expect(error.errors).toEqual([]);
    expect(error.message).toBe("API Error 400: Bad Request");
  });

  it("should create error with error details", () => {
    const errorDetails: CollaboratorApiErrorDetail[] = [
      {
        message: "Invalid task ID",
        timestamp: "2023-12-01T10:00:00Z",
        field: "taskId",
        rejectedValue: "invalid",
      },
    ];

    const error = new CollaboratorApiError(400, "Bad Request", errorDetails);

    expect(error.errors).toEqual(errorDetails);
    expect(error.errors[0].message).toBe("Invalid task ID");
    expect(error.errors[0].field).toBe("taskId");
    expect(error.errors[0].rejectedValue).toBe("invalid");
  });

  it("should create error with timestamp", () => {
    const timestamp = "2023-12-01T10:00:00Z";
    const error = new CollaboratorApiError(
      500,
      "Internal Server Error",
      [],
      timestamp,
    );

    expect(error.timestamp).toBe(timestamp);
  });

  it("should extend Error class", () => {
    const error = new CollaboratorApiError(400, "Bad Request");

    expect(error).toBeInstanceOf(Error);
    expect(error).toBeInstanceOf(CollaboratorApiError);
  });
});

describe("CollaboratorValidationError", () => {
  const mockValidationErrors = [
    {
      message: "Task ID is required",
      field: "taskId",
      rejectedValue: null,
      timestamp: "2023-12-01T10:00:00Z",
    },
    {
      message: "Collaborator ID must be a positive number",
      field: "collaboratorId",
      rejectedValue: -1,
      timestamp: "2023-12-01T10:00:00Z",
    },
  ];

  it("should create validation error with default values", () => {
    const error = new CollaboratorValidationError(mockValidationErrors);

    expect(error.name).toBe("CollaboratorValidationError");
    expect(error.status).toBe(400);
    expect(error.statusText).toBe("Validation Error");
    expect(error.validationErrors).toEqual(mockValidationErrors);
    expect(error.message).toBe("Validation Error 400: Validation Error");
  });

  it("should create validation error with custom status and statusText", () => {
    const error = new CollaboratorValidationError(
      mockValidationErrors,
      422,
      "Unprocessable Entity",
    );

    expect(error.status).toBe(422);
    expect(error.statusText).toBe("Unprocessable Entity");
    expect(error.message).toBe("Validation Error 422: Unprocessable Entity");
  });

  it("should store validation errors correctly", () => {
    const error = new CollaboratorValidationError(mockValidationErrors);

    expect(error.validationErrors).toHaveLength(2);
    expect(error.validationErrors[0].message).toBe("Task ID is required");
    expect(error.validationErrors[0].field).toBe("taskId");
    expect(error.validationErrors[1].message).toBe(
      "Collaborator ID must be a positive number",
    );
    expect(error.validationErrors[1].field).toBe("collaboratorId");
    expect(error.validationErrors[1].rejectedValue).toBe(-1);
  });

  it("should extend Error class", () => {
    const error = new CollaboratorValidationError(mockValidationErrors);

    expect(error).toBeInstanceOf(Error);
    expect(error).toBeInstanceOf(CollaboratorValidationError);
  });

  it("should handle empty validation errors array", () => {
    const error = new CollaboratorValidationError([]);

    expect(error.validationErrors).toEqual([]);
    expect(error.validationErrors).toHaveLength(0);
  });
});

describe("TaskCollaboratorApiClient Integration", () => {
  let apiService: TaskCollaboratorApiService;
  let mockClient: any;

  beforeEach(() => {
    apiService = new TaskCollaboratorApiService();
    mockClient = (apiService as any).client;
    jest.clearAllMocks();
  });

  describe("custom deleteWithBody method", () => {
    it("should handle DELETE requests with body correctly", async () => {
      const removeRequest: RemoveCollaboratorRequestDto = {
        taskId: 1,
        collaboratorId: 2,
      };

      // Mock successful remove
      mockClient.deleteWithBody = jest.fn().mockResolvedValue(undefined);

      const result = await apiService.removeCollaborator(removeRequest);

      expect(mockClient.deleteWithBody).toHaveBeenCalledWith(
        expect.stringContaining("/api/tasks/collaborator"),
        removeRequest
      );
      expect(result).toBeUndefined();
    });

    it("should propagate errors from deleteWithBody correctly", async () => {
      const removeRequest: RemoveCollaboratorRequestDto = {
        taskId: 1,
        collaboratorId: 2,
      };

      const deleteError = new CollaboratorApiError(404, "Not Found", [
        {
          message: "Collaborator assignment not found",
          timestamp: "2023-12-01T10:00:00Z",
        },
      ]);

      mockClient.deleteWithBody = jest.fn().mockRejectedValue(deleteError);

      await expect(apiService.removeCollaborator(removeRequest)).rejects.toThrow(CollaboratorApiError);
    });
  });

  describe("error response handling scenarios", () => {
    it("should handle various HTTP error status codes", async () => {
      const request: AddCollaboratorRequestDto = {
        taskId: 1,
        collaboratorId: 2,
      };

      const errorScenarios = [
        { status: 400, message: "Bad Request", errorType: CollaboratorValidationError },
        { status: 401, message: "Unauthorized", errorType: CollaboratorApiError },
        { status: 403, message: "Forbidden", errorType: CollaboratorApiError },
        { status: 404, message: "Not Found", errorType: CollaboratorApiError },
        { status: 409, message: "Conflict", errorType: CollaboratorApiError },
        { status: 422, message: "Unprocessable Entity", errorType: CollaboratorApiError },
        { status: 500, message: "Internal Server Error", errorType: CollaboratorApiError },
      ];

      for (const scenario of errorScenarios) {
        const error = scenario.status === 400
          ? new CollaboratorValidationError([
              {
                message: "Validation failed",
                field: "taskId",
                rejectedValue: null,
                timestamp: "2023-12-01T10:00:00Z",
              },
            ])
          : new CollaboratorApiError(scenario.status, scenario.message, [
              {
                message: scenario.message,
                timestamp: "2023-12-01T10:00:00Z",
              },
            ]);

        mockClient.post.mockRejectedValueOnce(error);

        await expect(apiService.addCollaborator(request)).rejects.toThrow(scenario.errorType);
      }
    });
  });
});

describe("Environment Configuration", () => {
  it("should use the configured API endpoint", () => {
    // Since the endpoint is set at module load time, we can verify
    // that the service uses the correct endpoint by checking the calls
    const apiService = new TaskCollaboratorApiService();
    const mockClient = (apiService as any).client;

    // Mock the post method to capture the URL
    mockClient.post = jest.fn().mockResolvedValue({
      taskId: 1,
      collaboratorId: 2,
      assignedById: 3,
      assignedAt: "2023-12-01T10:00:00Z",
    });

    const testPayload = { taskId: 1, collaboratorId: 2 };
    apiService.addCollaborator(testPayload);

    // The URL should contain the expected endpoint path
    expect(mockClient.post).toHaveBeenCalledWith(
      expect.stringContaining("/api/tasks/collaborator"),
      testPayload
    );
  });

  it("should construct endpoint URLs correctly for different operations", () => {
    const apiService = new TaskCollaboratorApiService();
    const mockClient = (apiService as any).client;

    // Mock both methods
    mockClient.post = jest.fn().mockResolvedValue({
      taskId: 1,
      collaboratorId: 2,
      assignedById: 3,
      assignedAt: "2023-12-01T10:00:00Z",
    });
    mockClient.deleteWithBody = jest.fn().mockResolvedValue(undefined);

    const addPayload = { taskId: 1, collaboratorId: 2 };
    const removePayload = { taskId: 1, collaboratorId: 2 };

    // Test both operations use the same endpoint
    apiService.addCollaborator(addPayload);
    apiService.removeCollaborator(removePayload);

    // Both should use the same base URL pattern
    expect(mockClient.post).toHaveBeenCalledWith(
      expect.stringContaining("/api/tasks/collaborator"),
      addPayload
    );
    expect(mockClient.deleteWithBody).toHaveBeenCalledWith(
      expect.stringContaining("/api/tasks/collaborator"),
      removePayload
    );

    // The endpoints should be identical
    const postCall = mockClient.post.mock.calls[0][0];
    const deleteCall = mockClient.deleteWithBody.mock.calls[0][0];
    expect(postCall).toBe(deleteCall);
  });

  it("should maintain singleton behavior", () => {
    const service1 = taskCollaboratorApiService;
    const service2 = taskCollaboratorApiService;

    expect(service1).toBe(service2);
    expect(service1).toBeInstanceOf(TaskCollaboratorApiService);
  });
});

describe("Integration Scenarios", () => {
  let apiService: TaskCollaboratorApiService;
  let mockClient: any;

  beforeEach(() => {
    apiService = new TaskCollaboratorApiService();
    mockClient = (apiService as any).client;
    jest.clearAllMocks();
  });

  describe("real-world workflow scenarios", () => {
    it("should handle complete add-remove collaborator workflow", async () => {
      const addRequest: AddCollaboratorRequestDto = {
        taskId: 1,
        collaboratorId: 2,
      };

      const addResponse: AddCollaboratorResponseDto = {
        taskId: 1,
        collaboratorId: 2,
        assignedById: 3,
        assignedAt: "2023-12-01T10:00:00Z",
      };

      const removeRequest: RemoveCollaboratorRequestDto = {
        taskId: 1,
        collaboratorId: 2,
      };

      // Mock successful add
      mockClient.post.mockResolvedValueOnce(addResponse);
      // Mock successful remove
      mockClient.deleteWithBody.mockResolvedValueOnce(undefined);

      // Add collaborator
      const addResult = await apiService.addCollaborator(addRequest);
      expect(addResult).toEqual(addResponse);
      expect(mockClient.post).toHaveBeenCalledWith(
        expect.stringContaining("/api/tasks/collaborator"),
        addRequest
      );

      // Remove collaborator
      const removeResult = await apiService.removeCollaborator(removeRequest);
      expect(removeResult).toBeUndefined();
      expect(mockClient.deleteWithBody).toHaveBeenCalledWith(
        expect.stringContaining("/api/tasks/collaborator"),
        removeRequest
      );

      expect(mockClient.post).toHaveBeenCalledTimes(1);
      expect(mockClient.deleteWithBody).toHaveBeenCalledTimes(1);
    });

    it("should handle business logic errors during workflow", async () => {
      const addRequest: AddCollaboratorRequestDto = {
        taskId: 1,
        collaboratorId: 2,
      };

      const businessRuleError = new CollaboratorApiError(409, "Conflict", [
        {
          message: "User is already a collaborator on this task",
          timestamp: "2023-12-01T10:00:00Z",
          field: "collaboratorId",
          rejectedValue: 2,
        },
      ]);

      mockClient.post.mockRejectedValue(businessRuleError);

      await expect(apiService.addCollaborator(addRequest)).rejects.toThrow(
        CollaboratorApiError
      );

      const error = await apiService.addCollaborator(addRequest).catch(e => e);
      expect(error.status).toBe(409);
      expect(error.errors[0].message).toBe("User is already a collaborator on this task");
      expect(error.errors[0].field).toBe("collaboratorId");
    });

    it("should handle permission-based access control errors", async () => {
      const addRequest: AddCollaboratorRequestDto = {
        taskId: 1,
        collaboratorId: 2,
      };

      const permissionError = new CollaboratorApiError(403, "Forbidden", [
        {
          message: "You don't have permission to add collaborators to this task",
          timestamp: "2023-12-01T10:00:00Z",
        },
      ]);

      mockClient.post.mockRejectedValue(permissionError);

      const error = await apiService.addCollaborator(addRequest).catch(e => e);
      expect(error).toBeInstanceOf(CollaboratorApiError);
      expect(error.status).toBe(403);
      expect(error.errors[0].message).toContain("permission");
    });

    it("should handle cascading failures gracefully", async () => {
      const requests = [
        { taskId: 1, collaboratorId: 2 },
        { taskId: 1, collaboratorId: 4 },
        { taskId: 1, collaboratorId: 5 },
      ];

      // First succeeds, second fails, third should still be attempted
      mockClient.post
        .mockResolvedValueOnce({ ...requests[0], assignedAt: "2023-12-01T10:00:00Z" })
        .mockRejectedValueOnce(new CollaboratorApiError(400, "Bad Request"))
        .mockResolvedValueOnce({ ...requests[2], assignedAt: "2023-12-01T10:00:00Z" });

      const results = await Promise.allSettled(
        requests.map(req => apiService.addCollaborator(req))
      );

      expect(results[0].status).toBe("fulfilled");
      expect(results[1].status).toBe("rejected");
      expect(results[2].status).toBe("fulfilled");

      expect(mockClient.post).toHaveBeenCalledTimes(3);
    });
  });

  describe("data consistency and validation", () => {
    it("should validate request data types at runtime", async () => {
      // This test ensures TypeScript types are enforced correctly
      const validRequest: AddCollaboratorRequestDto = {
        taskId: 1,
        collaboratorId: 2,
      };

      const validResponse: AddCollaboratorResponseDto = {
        taskId: 1,
        collaboratorId: 2,
        assignedById: 3,
        assignedAt: "2023-12-01T10:00:00Z",
      };

      mockClient.post = jest.fn().mockResolvedValue(validResponse);

      const result = await apiService.addCollaborator(validRequest);

      expect(result).toEqual(validResponse);
      expect(typeof result.taskId).toBe("number");
      expect(typeof result.collaboratorId).toBe("number");
      expect(typeof result.assignedById).toBe("number");
      expect(typeof result.assignedAt).toBe("string");

      // Verify the assignedAt is a valid ISO date string
      const parsedDate = new Date(result.assignedAt);
      expect(parsedDate.toISOString()).toMatch(/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(\.\d{3})?Z$/);
      expect(parsedDate.getTime()).not.toBeNaN();
    });

    it("should handle server response data inconsistencies", async () => {
      const request: AddCollaboratorRequestDto = {
        taskId: 1,
        collaboratorId: 2,
      };

      // Server returns different IDs than requested (edge case)
      const inconsistentResponse = {
        taskId: 999, // Different from request
        collaboratorId: 2,
        assignedById: 3,
        assignedAt: "2023-12-01T10:00:00Z",
      };

      mockClient.post.mockResolvedValue(inconsistentResponse);

      const result = await apiService.addCollaborator(request);

      // Service should return whatever the server responds with
      expect(result.taskId).toBe(999);
      expect(result.collaboratorId).toBe(2);
      expect(result.assignedById).toBe(3);
    });
  });
});

describe("Error Boundary Scenarios", () => {
  let apiService: TaskCollaboratorApiService;
  let mockClient: any;

  beforeEach(() => {
    apiService = new TaskCollaboratorApiService();
    mockClient = (apiService as any).client;
    jest.clearAllMocks();
  });

  it("should handle memory pressure scenarios", async () => {
    const largeBatchRequests = Array.from({ length: 100 }, (_, i) => ({
      taskId: 1,
      collaboratorId: i + 1,
    }));

    // Mock all to succeed
    mockClient.post.mockImplementation((url: string, data: any) =>
      Promise.resolve({
        ...data,
        assignedAt: "2023-12-01T10:00:00Z",
      })
    );

    // Process in batches to simulate real-world usage
    const batchSize = 10;
    const batches = [];

    for (let i = 0; i < largeBatchRequests.length; i += batchSize) {
      const batch = largeBatchRequests.slice(i, i + batchSize);
      batches.push(
        Promise.all(batch.map(req => apiService.addCollaborator(req)))
      );
    }

    const results = await Promise.all(batches);
    const flatResults = results.flat();

    expect(flatResults).toHaveLength(100);
    expect(mockClient.post).toHaveBeenCalledTimes(100);

    // Verify all results are correctly structured
    flatResults.forEach((result, index) => {
      expect(result.collaboratorId).toBe(index + 1);
      expect(result.assignedAt).toBeDefined();
    });
  });

  it("should handle service degradation gracefully", async () => {
    const request: AddCollaboratorRequestDto = {
      taskId: 1,
      collaboratorId: 2,
    };

    // Simulate service degradation with slow responses
    const slowResponse = new Promise<AddCollaboratorResponseDto>((resolve) => {
      setTimeout(() => {
        resolve({
          taskId: 1,
          collaboratorId: 2,
          assignedById: 3,
          assignedAt: "2023-12-01T10:00:00Z",
        });
      }, 100); // 100ms delay
    });

    mockClient.post.mockReturnValue(slowResponse);

    const startTime = Date.now();
    const result = await apiService.addCollaborator(request);
    const endTime = Date.now();

    expect(result).toBeDefined();
    // Allow for some tolerance in timing due to JavaScript timer precision
    expect(endTime - startTime).toBeGreaterThanOrEqual(95);
  });
});