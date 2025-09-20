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
      assignedById: 3,
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
      assignedById: 3,
    };

    it("should successfully remove a collaborator", async () => {
      mockClient.request.mockResolvedValue(undefined);

      const result = await apiService.removeCollaborator(mockRequest);

      expect(mockClient.request).toHaveBeenCalledWith(
        expect.stringContaining("/api/tasks/collaborator"),
        {
          method: "DELETE",
          body: JSON.stringify(mockRequest),
        },
      );
      expect(result).toBeUndefined();
    });

    it("should call the correct endpoint with DELETE method", async () => {
      mockClient.request.mockResolvedValue(undefined);

      await apiService.removeCollaborator(mockRequest);

      expect(mockClient.request).toHaveBeenCalledWith(
        expect.stringMatching(/\/api\/tasks\/collaborator$/),
        {
          method: "DELETE",
          body: JSON.stringify(mockRequest),
        },
      );
    });

    it("should serialize request body correctly", async () => {
      mockClient.request.mockResolvedValue(undefined);

      await apiService.removeCollaborator(mockRequest);

      expect(mockClient.request).toHaveBeenCalledWith(
        expect.any(String),
        expect.objectContaining({
          body: JSON.stringify(mockRequest),
        }),
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
      mockClient.request.mockRejectedValue(error);

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
      mockClient.request.mockRejectedValue(error);

      await expect(apiService.removeCollaborator(mockRequest)).rejects.toThrow(
        CollaboratorValidationError,
      );
    });

    it("should handle network errors", async () => {
      mockClient.request.mockRejectedValue(new Error("Network error"));

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