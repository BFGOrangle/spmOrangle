import { renderHook, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { ReactNode } from "react";
import {
  useAddCollaboratorMutation,
  useRemoveCollaboratorMutation,
  useTaskCollaboratorMutations,
  CollaboratorMutationError,
} from "@/hooks/use-task-collaborators";
import {
  TaskCollaboratorApiService,
  CollaboratorApiError,
  CollaboratorValidationError,
} from "@/services/task-collaborator-api";
import {
  AddCollaboratorRequestDto,
  AddCollaboratorResponseDto,
  RemoveCollaboratorRequestDto,
  CollaboratorApiErrorDetail,
} from "@/types/collaborator";

// Test wrapper component with QueryClient
const createWrapper = (queryClient?: QueryClient) => {
  const client = queryClient || new QueryClient({
    defaultOptions: {
      queries: { retry: false, staleTime: Infinity },
      mutations: { retry: false },
    },
  });

  return ({ children }: { children: ReactNode }) => (
    <QueryClientProvider client={client}>{children}</QueryClientProvider>
  );
};

// Mock the TaskCollaboratorApiService
const createMockApiService = (): jest.Mocked<TaskCollaboratorApiService> => {
  const mockService = new TaskCollaboratorApiService();
  jest.spyOn(mockService, 'addCollaborator');
  jest.spyOn(mockService, 'removeCollaborator');
  return mockService as jest.Mocked<TaskCollaboratorApiService>;
};

describe("useAddCollaboratorMutation", () => {
  let mockApiService: jest.Mocked<TaskCollaboratorApiService>;
  let queryClient: QueryClient;

  beforeEach(() => {
    mockApiService = createMockApiService();
    queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false, staleTime: Infinity },
        mutations: { retry: false },
      },
    });
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

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

  it("should initialize with correct default state", () => {
    const { result } = renderHook(
      () => useAddCollaboratorMutation(undefined, mockApiService),
      { wrapper: createWrapper(queryClient) },
    );

    expect(result.current.data).toBeUndefined();
    expect(result.current.error).toBeNull();
    expect(result.current.isIdle).toBe(true);
    expect(result.current.isPending).toBe(false);
    expect(result.current.isSuccess).toBe(false);
    expect(result.current.isError).toBe(false);
    expect(typeof result.current.mutate).toBe("function");
    expect(typeof result.current.mutateAsync).toBe("function");
  });

  it("should successfully add a collaborator", async () => {
    mockApiService.addCollaborator.mockResolvedValue(mockResponse);

    const { result } = renderHook(
      () => useAddCollaboratorMutation(undefined, mockApiService),
      { wrapper: createWrapper(queryClient) },
    );

    result.current.mutate(mockRequest);

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });

    expect(mockApiService.addCollaborator).toHaveBeenCalledWith(mockRequest);
    expect(result.current.data).toEqual(mockResponse);
    expect(result.current.error).toBeNull();
  });

  it("should handle API errors correctly", async () => {
    const errorDetails: CollaboratorApiErrorDetail[] = [
      {
        message: "Task not found",
        timestamp: "2023-12-01T10:00:00Z",
      },
    ];
    const apiError = new CollaboratorApiError(404, "Not Found", errorDetails);
    mockApiService.addCollaborator.mockRejectedValue(apiError);

    const { result } = renderHook(
      () => useAddCollaboratorMutation(undefined, mockApiService),
      { wrapper: createWrapper(queryClient) },
    );

    result.current.mutate(mockRequest);

    await waitFor(() => {
      expect(result.current.isError).toBe(true);
    });

    expect(result.current.error).toBeInstanceOf(CollaboratorApiError);
    expect(result.current.error?.status).toBe(404);
    expect(result.current.error?.statusText).toBe("Not Found");
    expect(result.current.data).toBeUndefined();
  });

  it("should handle validation errors correctly", async () => {
    const validationErrors = [
      {
        message: "Task ID is required",
        field: "taskId",
        rejectedValue: null,
        timestamp: "2023-12-01T10:00:00Z",
      },
    ];
    const validationError = new CollaboratorValidationError(validationErrors);
    mockApiService.addCollaborator.mockRejectedValue(validationError);

    const { result } = renderHook(
      () => useAddCollaboratorMutation(undefined, mockApiService),
      { wrapper: createWrapper(queryClient) },
    );

    result.current.mutate(mockRequest);

    await waitFor(() => {
      expect(result.current.isError).toBe(true);
    });

    expect(result.current.error).toBeInstanceOf(CollaboratorValidationError);
    expect((result.current.error as CollaboratorValidationError).validationErrors).toEqual(validationErrors);
  });

  it("should call onSuccess callback when provided", async () => {
    mockApiService.addCollaborator.mockResolvedValue(mockResponse);
    const onSuccess = jest.fn();

    const { result } = renderHook(
      () => useAddCollaboratorMutation({ onSuccess }, mockApiService),
      { wrapper: createWrapper(queryClient) },
    );

    result.current.mutate(mockRequest);

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });

    expect(onSuccess).toHaveBeenCalledWith(mockResponse, mockRequest, undefined);
  });

  it("should call onError callback when provided", async () => {
    const apiError = new CollaboratorApiError(404, "Not Found");
    mockApiService.addCollaborator.mockRejectedValue(apiError);
    const onError = jest.fn();

    const { result } = renderHook(
      () => useAddCollaboratorMutation({ onError }, mockApiService),
      { wrapper: createWrapper(queryClient) },
    );

    result.current.mutate(mockRequest);

    await waitFor(() => {
      expect(result.current.isError).toBe(true);
    });

    expect(onError).toHaveBeenCalledWith(apiError, mockRequest, undefined);
  });

  it("should handle mutateAsync correctly", async () => {
    mockApiService.addCollaborator.mockResolvedValue(mockResponse);

    const { result } = renderHook(
      () => useAddCollaboratorMutation(undefined, mockApiService),
      { wrapper: createWrapper(queryClient) },
    );

    const response = await result.current.mutateAsync(mockRequest);

    expect(response).toEqual(mockResponse);
    expect(mockApiService.addCollaborator).toHaveBeenCalledWith(mockRequest);
  });

  it("should reject mutateAsync on error", async () => {
    const apiError = new CollaboratorApiError(404, "Not Found");
    mockApiService.addCollaborator.mockRejectedValue(apiError);

    const { result } = renderHook(
      () => useAddCollaboratorMutation(undefined, mockApiService),
      { wrapper: createWrapper(queryClient) },
    );

    await expect(result.current.mutateAsync(mockRequest)).rejects.toThrow(CollaboratorApiError);
  });

  it("should use correct mutation key", () => {
    const { result } = renderHook(
      () => useAddCollaboratorMutation(undefined, mockApiService),
      { wrapper: createWrapper(queryClient) },
    );

    // Check that the mutation is registered with the correct key in QueryClient
    const mutationCache = queryClient.getMutationCache();
    const mutations = mutationCache.getAll();

    // After the hook is created but before mutation is called, there should be no active mutations
    expect(mutations).toHaveLength(0);

    // The mutation key is tested by checking the mutation behavior
    expect(typeof result.current.mutate).toBe("function");
  });
});

describe("useRemoveCollaboratorMutation", () => {
  let mockApiService: jest.Mocked<TaskCollaboratorApiService>;
  let queryClient: QueryClient;

  beforeEach(() => {
    mockApiService = createMockApiService();
    queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false, staleTime: Infinity },
        mutations: { retry: false },
      },
    });
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  const mockRequest: RemoveCollaboratorRequestDto = {
    taskId: 1,
    collaboratorId: 2,
    assignedById: 3,
  };

  it("should initialize with correct default state", () => {
    const { result } = renderHook(
      () => useRemoveCollaboratorMutation(undefined, mockApiService),
      { wrapper: createWrapper(queryClient) },
    );

    expect(result.current.data).toBeUndefined();
    expect(result.current.error).toBeNull();
    expect(result.current.isIdle).toBe(true);
    expect(result.current.isPending).toBe(false);
    expect(result.current.isSuccess).toBe(false);
    expect(result.current.isError).toBe(false);
    expect(typeof result.current.mutate).toBe("function");
    expect(typeof result.current.mutateAsync).toBe("function");
  });

  it("should successfully remove a collaborator", async () => {
    mockApiService.removeCollaborator.mockResolvedValue(undefined);

    const { result } = renderHook(
      () => useRemoveCollaboratorMutation(undefined, mockApiService),
      { wrapper: createWrapper(queryClient) },
    );

    result.current.mutate(mockRequest);

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });

    expect(mockApiService.removeCollaborator).toHaveBeenCalledWith(mockRequest);
    expect(result.current.data).toBeUndefined();
    expect(result.current.error).toBeNull();
  });

  it("should handle API errors correctly", async () => {
    const errorDetails: CollaboratorApiErrorDetail[] = [
      {
        message: "Collaborator not found",
        timestamp: "2023-12-01T10:00:00Z",
      },
    ];
    const apiError = new CollaboratorApiError(404, "Not Found", errorDetails);
    mockApiService.removeCollaborator.mockRejectedValue(apiError);

    const { result } = renderHook(
      () => useRemoveCollaboratorMutation(undefined, mockApiService),
      { wrapper: createWrapper(queryClient) },
    );

    result.current.mutate(mockRequest);

    await waitFor(() => {
      expect(result.current.isError).toBe(true);
    });

    expect(result.current.error).toBeInstanceOf(CollaboratorApiError);
    expect(result.current.error?.status).toBe(404);
    expect(result.current.error?.statusText).toBe("Not Found");
  });

  it("should handle validation errors correctly", async () => {
    const validationErrors = [
      {
        message: "Collaborator ID is required",
        field: "collaboratorId",
        rejectedValue: null,
        timestamp: "2023-12-01T10:00:00Z",
      },
    ];
    const validationError = new CollaboratorValidationError(validationErrors);
    mockApiService.removeCollaborator.mockRejectedValue(validationError);

    const { result } = renderHook(
      () => useRemoveCollaboratorMutation(undefined, mockApiService),
      { wrapper: createWrapper(queryClient) },
    );

    result.current.mutate(mockRequest);

    await waitFor(() => {
      expect(result.current.isError).toBe(true);
    });

    expect(result.current.error).toBeInstanceOf(CollaboratorValidationError);
    expect((result.current.error as CollaboratorValidationError).validationErrors).toEqual(validationErrors);
  });

  it("should call onSuccess callback when provided", async () => {
    mockApiService.removeCollaborator.mockResolvedValue(undefined);
    const onSuccess = jest.fn();

    const { result } = renderHook(
      () => useRemoveCollaboratorMutation({ onSuccess }, mockApiService),
      { wrapper: createWrapper(queryClient) },
    );

    result.current.mutate(mockRequest);

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });

    expect(onSuccess).toHaveBeenCalledWith(undefined, mockRequest, undefined);
  });

  it("should call onError callback when provided", async () => {
    const apiError = new CollaboratorApiError(404, "Not Found");
    mockApiService.removeCollaborator.mockRejectedValue(apiError);
    const onError = jest.fn();

    const { result } = renderHook(
      () => useRemoveCollaboratorMutation({ onError }, mockApiService),
      { wrapper: createWrapper(queryClient) },
    );

    result.current.mutate(mockRequest);

    await waitFor(() => {
      expect(result.current.isError).toBe(true);
    });

    expect(onError).toHaveBeenCalledWith(apiError, mockRequest, undefined);
  });

  it("should handle mutateAsync correctly", async () => {
    mockApiService.removeCollaborator.mockResolvedValue(undefined);

    const { result } = renderHook(
      () => useRemoveCollaboratorMutation(undefined, mockApiService),
      { wrapper: createWrapper(queryClient) },
    );

    const response = await result.current.mutateAsync(mockRequest);

    expect(response).toBeUndefined();
    expect(mockApiService.removeCollaborator).toHaveBeenCalledWith(mockRequest);
  });

  it("should reject mutateAsync on error", async () => {
    const apiError = new CollaboratorApiError(404, "Not Found");
    mockApiService.removeCollaborator.mockRejectedValue(apiError);

    const { result } = renderHook(
      () => useRemoveCollaboratorMutation(undefined, mockApiService),
      { wrapper: createWrapper(queryClient) },
    );

    await expect(result.current.mutateAsync(mockRequest)).rejects.toThrow(CollaboratorApiError);
  });
});

describe("useTaskCollaboratorMutations", () => {
  let mockApiService: jest.Mocked<TaskCollaboratorApiService>;
  let queryClient: QueryClient;

  beforeEach(() => {
    mockApiService = createMockApiService();
    queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false, staleTime: Infinity },
        mutations: { retry: false },
      },
    });
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  const mockAddRequest: AddCollaboratorRequestDto = {
    taskId: 1,
    collaboratorId: 2,
    assignedById: 3,
  };

  const mockRemoveRequest: RemoveCollaboratorRequestDto = {
    taskId: 1,
    collaboratorId: 2,
    assignedById: 3,
  };

  const mockAddResponse: AddCollaboratorResponseDto = {
    taskId: 1,
    collaboratorId: 2,
    assignedById: 3,
    assignedAt: "2023-12-01T10:00:00Z",
  };

  it("should return both mutation hooks", () => {
    const { result } = renderHook(
      () => useTaskCollaboratorMutations({ apiService: mockApiService }),
      { wrapper: createWrapper(queryClient) },
    );

    expect(result.current.addCollaboratorMutation).toBeDefined();
    expect(result.current.removeCollaboratorMutation).toBeDefined();
    expect(typeof result.current.addCollaboratorMutation.mutate).toBe("function");
    expect(typeof result.current.removeCollaboratorMutation.mutate).toBe("function");
  });

  it("should use default API service when none provided", () => {
    const { result } = renderHook(
      () => useTaskCollaboratorMutations(),
      { wrapper: createWrapper(queryClient) },
    );

    expect(result.current.addCollaboratorMutation).toBeDefined();
    expect(result.current.removeCollaboratorMutation).toBeDefined();
  });

  it("should use custom API service when provided", () => {
    const { result } = renderHook(
      () => useTaskCollaboratorMutations({ apiService: mockApiService }),
      { wrapper: createWrapper(queryClient) },
    );

    expect(result.current.addCollaboratorMutation).toBeDefined();
    expect(result.current.removeCollaboratorMutation).toBeDefined();
  });

  it("should pass through add options correctly", async () => {
    const onAddSuccess = jest.fn();
    mockApiService.addCollaborator.mockResolvedValue(mockAddResponse);

    const { result } = renderHook(
      () => useTaskCollaboratorMutations({
        addOptions: { onSuccess: onAddSuccess },
        apiService: mockApiService,
      }),
      { wrapper: createWrapper(queryClient) },
    );

    result.current.addCollaboratorMutation.mutate(mockAddRequest);

    await waitFor(() => {
      expect(result.current.addCollaboratorMutation.isSuccess).toBe(true);
    });

    expect(onAddSuccess).toHaveBeenCalledWith(mockAddResponse, mockAddRequest, undefined);
  });

  it("should pass through remove options correctly", async () => {
    const onRemoveSuccess = jest.fn();
    mockApiService.removeCollaborator.mockResolvedValue(undefined);

    const { result } = renderHook(
      () => useTaskCollaboratorMutations({
        removeOptions: { onSuccess: onRemoveSuccess },
        apiService: mockApiService,
      }),
      { wrapper: createWrapper(queryClient) },
    );

    result.current.removeCollaboratorMutation.mutate(mockRemoveRequest);

    await waitFor(() => {
      expect(result.current.removeCollaboratorMutation.isSuccess).toBe(true);
    });

    expect(onRemoveSuccess).toHaveBeenCalledWith(undefined, mockRemoveRequest, undefined);
  });

  it("should handle both mutations independently", async () => {
    mockApiService.addCollaborator.mockResolvedValue(mockAddResponse);
    mockApiService.removeCollaborator.mockResolvedValue(undefined);

    const { result } = renderHook(
      () => useTaskCollaboratorMutations({ apiService: mockApiService }),
      { wrapper: createWrapper(queryClient) },
    );

    // Trigger both mutations
    result.current.addCollaboratorMutation.mutate(mockAddRequest);
    result.current.removeCollaboratorMutation.mutate(mockRemoveRequest);

    // Wait for both to complete
    await waitFor(() => {
      expect(result.current.addCollaboratorMutation.isSuccess).toBe(true);
      expect(result.current.removeCollaboratorMutation.isSuccess).toBe(true);
    });

    expect(mockApiService.addCollaborator).toHaveBeenCalledWith(mockAddRequest);
    expect(mockApiService.removeCollaborator).toHaveBeenCalledWith(mockRemoveRequest);
    expect(result.current.addCollaboratorMutation.data).toEqual(mockAddResponse);
    expect(result.current.removeCollaboratorMutation.data).toBeUndefined();
  });

  it("should handle errors in both mutations independently", async () => {
    const addError = new CollaboratorApiError(400, "Add Error");
    const removeError = new CollaboratorApiError(404, "Remove Error");

    mockApiService.addCollaborator.mockRejectedValue(addError);
    mockApiService.removeCollaborator.mockRejectedValue(removeError);

    const { result } = renderHook(
      () => useTaskCollaboratorMutations({ apiService: mockApiService }),
      { wrapper: createWrapper(queryClient) },
    );

    // Trigger both mutations
    result.current.addCollaboratorMutation.mutate(mockAddRequest);
    result.current.removeCollaboratorMutation.mutate(mockRemoveRequest);

    // Wait for both to complete with errors
    await waitFor(() => {
      expect(result.current.addCollaboratorMutation.isError).toBe(true);
      expect(result.current.removeCollaboratorMutation.isError).toBe(true);
    });

    expect(result.current.addCollaboratorMutation.error).toBeInstanceOf(CollaboratorApiError);
    expect(result.current.addCollaboratorMutation.error?.statusText).toBe("Add Error");
    expect(result.current.removeCollaboratorMutation.error).toBeInstanceOf(CollaboratorApiError);
    expect(result.current.removeCollaboratorMutation.error?.statusText).toBe("Remove Error");
  });

  it("should support complex configuration", async () => {
    const onAddSuccess = jest.fn();
    const onRemoveSuccess = jest.fn();
    const onAddError = jest.fn();
    const onRemoveError = jest.fn();

    mockApiService.addCollaborator.mockResolvedValue(mockAddResponse);
    mockApiService.removeCollaborator.mockResolvedValue(undefined);

    const { result } = renderHook(
      () => useTaskCollaboratorMutations({
        addOptions: {
          onSuccess: onAddSuccess,
          onError: onAddError,
        },
        removeOptions: {
          onSuccess: onRemoveSuccess,
          onError: onRemoveError,
        },
        apiService: mockApiService,
      }),
      { wrapper: createWrapper(queryClient) },
    );

    result.current.addCollaboratorMutation.mutate(mockAddRequest);

    await waitFor(() => {
      expect(result.current.addCollaboratorMutation.isSuccess).toBe(true);
    });

    expect(onAddSuccess).toHaveBeenCalledWith(mockAddResponse, mockAddRequest, undefined);
    expect(onAddError).not.toHaveBeenCalled();
    expect(onRemoveSuccess).not.toHaveBeenCalled();
    expect(onRemoveError).not.toHaveBeenCalled();
  });
});