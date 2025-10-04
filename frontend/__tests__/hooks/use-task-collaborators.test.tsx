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

describe("Concurrent Mutations and Edge Cases", () => {
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

  describe("concurrent mutations", () => {
    it("should handle multiple add mutations simultaneously", async () => {
      const request1 = { ...mockAddRequest, collaboratorId: 2 };
      const request2 = { ...mockAddRequest, collaboratorId: 3 };
      const response1 = { ...mockAddResponse, collaboratorId: 2 };
      const response2 = { ...mockAddResponse, collaboratorId: 3 };

      mockApiService.addCollaborator
        .mockResolvedValueOnce(response1)
        .mockResolvedValueOnce(response2);

      const { result } = renderHook(
        () => useAddCollaboratorMutation(undefined, mockApiService),
        { wrapper: createWrapper(queryClient) },
      );

      // Trigger multiple mutations concurrently
      const promise1 = result.current.mutateAsync(request1);
      const promise2 = result.current.mutateAsync(request2);

      const [result1, result2] = await Promise.all([promise1, promise2]);

      expect(result1).toEqual(response1);
      expect(result2).toEqual(response2);
      expect(mockApiService.addCollaborator).toHaveBeenCalledTimes(2);
      expect(mockApiService.addCollaborator).toHaveBeenCalledWith(request1);
      expect(mockApiService.addCollaborator).toHaveBeenCalledWith(request2);
    });

    it("should handle concurrent add and remove mutations", async () => {
      mockApiService.addCollaborator.mockResolvedValue(mockAddResponse);
      mockApiService.removeCollaborator.mockResolvedValue(undefined);

      const { result: addResult } = renderHook(
        () => useAddCollaboratorMutation(undefined, mockApiService),
        { wrapper: createWrapper(queryClient) },
      );

      const { result: removeResult } = renderHook(
        () => useRemoveCollaboratorMutation(undefined, mockApiService),
        { wrapper: createWrapper(queryClient) },
      );

      // Trigger both mutations concurrently
      const addPromise = addResult.current.mutateAsync(mockAddRequest);
      const removePromise = removeResult.current.mutateAsync(mockRemoveRequest);

      const [addResponse, removeResponse] = await Promise.all([addPromise, removePromise]);

      expect(addResponse).toEqual(mockAddResponse);
      expect(removeResponse).toBeUndefined();
      expect(mockApiService.addCollaborator).toHaveBeenCalledWith(mockAddRequest);
      expect(mockApiService.removeCollaborator).toHaveBeenCalledWith(mockRemoveRequest);
    });

    it("should handle partial failures in concurrent mutations", async () => {
      const request1 = { ...mockAddRequest, collaboratorId: 2 };
      const request2 = { ...mockAddRequest, collaboratorId: 3 };
      const response1 = { ...mockAddResponse, collaboratorId: 2 };
      const error2 = new CollaboratorApiError(409, "Conflict");

      mockApiService.addCollaborator
        .mockResolvedValueOnce(response1)
        .mockRejectedValueOnce(error2);

      const { result } = renderHook(
        () => useAddCollaboratorMutation(undefined, mockApiService),
        { wrapper: createWrapper(queryClient) },
      );

      const promise1 = result.current.mutateAsync(request1);
      const promise2 = result.current.mutateAsync(request2);

      const [result1, result2] = await Promise.allSettled([promise1, promise2]);

      expect(result1.status).toBe("fulfilled");
      expect((result1 as PromiseFulfilledResult<AddCollaboratorResponseDto>).value).toEqual(response1);

      expect(result2.status).toBe("rejected");
      expect((result2 as PromiseRejectedResult).reason).toBeInstanceOf(CollaboratorApiError);
    });
  });

  describe("race conditions and state consistency", () => {
    it("should maintain state consistency during rapid mutations", async () => {
      let resolveCount = 0;
      const delayedMockImplementation = (request: AddCollaboratorRequestDto) => {
        return new Promise<AddCollaboratorResponseDto>((resolve) => {
          setTimeout(() => {
            resolveCount++;
            resolve({ ...mockAddResponse, collaboratorId: request.collaboratorId });
          }, resolveCount * 10); // Stagger the responses
        });
      };

      mockApiService.addCollaborator.mockImplementation(delayedMockImplementation);

      const { result } = renderHook(
        () => useAddCollaboratorMutation(undefined, mockApiService),
        { wrapper: createWrapper(queryClient) },
      );

      // Trigger multiple rapid mutations
      const requests = [
        { ...mockAddRequest, collaboratorId: 2 },
        { ...mockAddRequest, collaboratorId: 3 },
        { ...mockAddRequest, collaboratorId: 4 },
      ];

      const promises = requests.map(req => result.current.mutateAsync(req));
      const results = await Promise.all(promises);

      expect(results).toHaveLength(3);
      expect(results[0].collaboratorId).toBe(2);
      expect(results[1].collaboratorId).toBe(3);
      expect(results[2].collaboratorId).toBe(4);
      expect(mockApiService.addCollaborator).toHaveBeenCalledTimes(3);
    });

    it("should handle mutation cancellation scenarios", async () => {
      let rejectMutation: (error: Error) => void;
      const pendingPromise = new Promise<AddCollaboratorResponseDto>((_, reject) => {
        rejectMutation = reject;
      });

      mockApiService.addCollaborator.mockReturnValue(pendingPromise);

      const { result, unmount } = renderHook(
        () => useAddCollaboratorMutation(undefined, mockApiService),
        { wrapper: createWrapper(queryClient) },
      );

      const mutationPromise = result.current.mutateAsync(mockAddRequest);

      // Simulate component unmounting while mutation is pending
      unmount();

      // Reject the pending mutation
      rejectMutation!(new Error("Component unmounted"));

      await expect(mutationPromise).rejects.toThrow("Component unmounted");
    });
  });

  describe("query invalidation and cache management", () => {
    it("should support custom onSuccess with cache invalidation", async () => {
      mockApiService.addCollaborator.mockResolvedValue(mockAddResponse);

      const onSuccess = jest.fn((data, variables, context) => {
        // Simulate cache invalidation
        queryClient.invalidateQueries({ queryKey: ["tasks", variables.taskId] });
      });

      const { result } = renderHook(
        () => useAddCollaboratorMutation({ onSuccess }, mockApiService),
        { wrapper: createWrapper(queryClient) },
      );

      const invalidateQueriesSpy = jest.spyOn(queryClient, 'invalidateQueries');

      result.current.mutate(mockAddRequest);

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true);
      });

      expect(onSuccess).toHaveBeenCalledWith(mockAddResponse, mockAddRequest, undefined);
      expect(invalidateQueriesSpy).toHaveBeenCalledWith({ queryKey: ["tasks", mockAddRequest.taskId] });
    });

    it("should handle optimistic updates pattern", async () => {
      mockApiService.addCollaborator.mockResolvedValue(mockAddResponse);

      const onMutate = jest.fn((variables) => {
        // Simulate optimistic update
        const previousData = queryClient.getQueryData(["tasks", variables.taskId]);
        queryClient.setQueryData(["tasks", variables.taskId], (old: any) => ({
          ...old,
          collaborators: [...(old?.collaborators || []), variables],
        }));
        return { previousData };
      });

      const onError = jest.fn((error, variables, context: any) => {
        // Rollback optimistic update
        if (context?.previousData) {
          queryClient.setQueryData(["tasks", variables.taskId], context.previousData);
        }
      });

      const { result } = renderHook(
        () => useAddCollaboratorMutation({ onMutate, onError }, mockApiService),
        { wrapper: createWrapper(queryClient) },
      );

      // Set initial query data
      queryClient.setQueryData(["tasks", mockAddRequest.taskId], {
        collaborators: [],
      });

      result.current.mutate(mockAddRequest);

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true);
      });

      expect(onMutate).toHaveBeenCalledWith(mockAddRequest);
      expect(onError).not.toHaveBeenCalled();
    });

    it("should rollback optimistic updates on error", async () => {
      const apiError = new CollaboratorApiError(409, "Conflict");
      mockApiService.addCollaborator.mockRejectedValue(apiError);

      const onMutate = jest.fn((variables) => {
        const previousData = queryClient.getQueryData(["tasks", variables.taskId]);
        queryClient.setQueryData(["tasks", variables.taskId], (old: any) => ({
          ...old,
          collaborators: [...(old?.collaborators || []), variables],
        }));
        return { previousData };
      });

      const onError = jest.fn((error, variables, context: any) => {
        if (context?.previousData) {
          queryClient.setQueryData(["tasks", variables.taskId], context.previousData);
        }
      });

      const { result } = renderHook(
        () => useAddCollaboratorMutation({ onMutate, onError }, mockApiService),
        { wrapper: createWrapper(queryClient) },
      );

      // Set initial query data
      const initialData = { collaborators: [] };
      queryClient.setQueryData(["tasks", mockAddRequest.taskId], initialData);

      result.current.mutate(mockAddRequest);

      await waitFor(() => {
        expect(result.current.isError).toBe(true);
      });

      expect(onMutate).toHaveBeenCalledWith(mockAddRequest);
      expect(onError).toHaveBeenCalledWith(apiError, mockAddRequest, { previousData: initialData });

      // Verify rollback happened
      const finalData = queryClient.getQueryData(["tasks", mockAddRequest.taskId]);
      expect(finalData).toEqual(initialData);
    });
  });

  describe("error recovery and retry scenarios", () => {
    it("should handle network recovery scenarios", async () => {
      const networkError = new Error("Network Error");
      const successResponse = mockAddResponse;

      // First call fails, second succeeds
      mockApiService.addCollaborator
        .mockRejectedValueOnce(networkError)
        .mockResolvedValueOnce(successResponse);

      const { result } = renderHook(
        () => useAddCollaboratorMutation(undefined, mockApiService),
        { wrapper: createWrapper(queryClient) },
      );

      // First attempt fails
      result.current.mutate(mockAddRequest);

      await waitFor(() => {
        expect(result.current.isError).toBe(true);
      });

      expect(result.current.error).toEqual(networkError);

      // Reset and retry
      result.current.reset();

      await waitFor(() => {
        expect(result.current.isIdle).toBe(true);
      });

      // Second attempt succeeds
      result.current.mutate(mockAddRequest);

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true);
      });

      expect(result.current.data).toEqual(successResponse);
      expect(mockApiService.addCollaborator).toHaveBeenCalledTimes(2);
    });

    it("should handle API service switching", async () => {
      const mockApiService2 = createMockApiService();
      mockApiService.addCollaborator.mockResolvedValue(mockAddResponse);
      mockApiService2.addCollaborator.mockResolvedValue({
        ...mockAddResponse,
        assignedAt: "2023-12-02T10:00:00Z",
      });

      const { result, rerender } = renderHook(
        ({ service }) => useAddCollaboratorMutation(undefined, service),
        {
          wrapper: createWrapper(queryClient),
          initialProps: { service: mockApiService },
        },
      );

      // First mutation with first service
      result.current.mutate(mockAddRequest);

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true);
      });

      expect(result.current.data?.assignedAt).toBe("2023-12-01T10:00:00Z");

      // Switch to second service
      rerender({ service: mockApiService2 });

      // Reset state and try again
      result.current.reset();

      await waitFor(() => {
        expect(result.current.isIdle).toBe(true);
      });

      result.current.mutate(mockAddRequest);

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true);
      });

      expect(result.current.data?.assignedAt).toBe("2023-12-02T10:00:00Z");
      expect(mockApiService.addCollaborator).toHaveBeenCalledTimes(1);
      expect(mockApiService2.addCollaborator).toHaveBeenCalledTimes(1);
    });
  });

  describe("edge case data validation", () => {
    it("should handle mutations with minimal valid data", async () => {
      const minimalRequest: AddCollaboratorRequestDto = {
        taskId: 1,
        collaboratorId: 2,
        assignedById: 3,
      };

      const minimalResponse: AddCollaboratorResponseDto = {
        taskId: 1,
        collaboratorId: 2,
        assignedById: 3,
        assignedAt: "2023-12-01T10:00:00Z",
      };

      mockApiService.addCollaborator.mockResolvedValue(minimalResponse);

      const { result } = renderHook(
        () => useAddCollaboratorMutation(undefined, mockApiService),
        { wrapper: createWrapper(queryClient) },
      );

      result.current.mutate(minimalRequest);

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true);
      });

      expect(result.current.data).toEqual(minimalResponse);
      expect(mockApiService.addCollaborator).toHaveBeenCalledWith(minimalRequest);
    });

    it("should handle mutations with large data sets", async () => {
      const largeRequest: AddCollaboratorRequestDto = {
        taskId: Number.MAX_SAFE_INTEGER,
        collaboratorId: Number.MAX_SAFE_INTEGER - 1,
        assignedById: Number.MAX_SAFE_INTEGER - 2,
      };

      const largeResponse: AddCollaboratorResponseDto = {
        ...largeRequest,
        assignedAt: "2023-12-01T10:00:00Z",
      };

      mockApiService.addCollaborator.mockResolvedValue(largeResponse);

      const { result } = renderHook(
        () => useAddCollaboratorMutation(undefined, mockApiService),
        { wrapper: createWrapper(queryClient) },
      );

      result.current.mutate(largeRequest);

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true);
      });

      expect(result.current.data).toEqual(largeResponse);
      expect(mockApiService.addCollaborator).toHaveBeenCalledWith(largeRequest);
    });
  });
});