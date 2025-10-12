import { ProjectService, projectService } from "../../services/project-service";
import { AuthenticatedApiClient } from "../../services/authenticated-api-client";

// Mock the authenticated API client
jest.mock("../../services/authenticated-api-client");

const MockAuthenticatedApiClient = AuthenticatedApiClient as jest.MockedClass<typeof AuthenticatedApiClient>;

describe("ProjectService", () => {
  let service: ProjectService;
  let mockAuthenticatedClient: jest.Mocked<AuthenticatedApiClient>;

  beforeEach(() => {
    // Create mock instance
    mockAuthenticatedClient = {
      get: jest.fn(),
      post: jest.fn(),
      put: jest.fn(),
      delete: jest.fn(),
      patch: jest.fn(),
      postMultipart: jest.fn(),
    } as any;

    // Mock the constructor
    MockAuthenticatedApiClient.mockImplementation(() => mockAuthenticatedClient);

    service = new ProjectService();
  });

  afterEach(() => {
    jest.restoreAllMocks();
  });

  describe("createTask", () => {
    it("successfully creates a task", async () => {
      const mockTask = {
        id: 1,
        title: 'Test Task',
        description: 'Test Description',
        projectId: 1,
        ownerId: 1,
        taskType: 'FEATURE' as const,
        status: 'TODO' as const,
        userHasEditAccess: true,
        createdAt: '2023-01-01T00:00:00Z',
        createdBy: 1,
      };

      const taskData = {
        title: 'Test Task',
        description: 'Test Description',
        projectId: 1,
        ownerId: 1,
        taskType: 'FEATURE' as const,
        status: 'TODO' as const,
        tags: ['frontend', 'react'],
      };

      mockAuthenticatedClient.post.mockResolvedValueOnce(mockTask);

      const result = await service.createTask(taskData);

      expect(mockAuthenticatedClient.post).toHaveBeenCalledWith('/api/tasks', taskData);
      expect(result).toEqual(mockTask);
    });

    it("handles task creation errors", async () => {
      const taskData = {
        title: 'Test Task',
        description: 'Test Description',
        projectId: 1,
        ownerId: 1,
        taskType: 'FEATURE' as const,
        status: 'TODO' as const,
        tags: [],
      };

      const error = new Error('Creation failed');
      mockAuthenticatedClient.post.mockRejectedValueOnce(error);

      await expect(service.createTask(taskData)).rejects.toThrow('Creation failed');
      expect(mockAuthenticatedClient.post).toHaveBeenCalledWith('/api/tasks', taskData);
    });

    it("creates personal task with undefined projectId", async () => {
      const personalTaskData = {
        title: 'Personal Task',
        description: 'Personal Description',
        projectId: undefined, // Personal task
        ownerId: 1,
        taskType: 'FEATURE' as const,
        status: 'TODO' as const,
        tags: [],
      };

      const mockPersonalTask = {
        id: 2,
        title: 'Personal Task',
        description: 'Personal Description',
        ownerId: 1,
        taskType: 'FEATURE' as const,
        status: 'TODO' as const,
        userHasEditAccess: true,
        createdAt: '2023-01-01T00:00:00Z',
        createdBy: 1,
      };

      mockAuthenticatedClient.post.mockResolvedValueOnce(mockPersonalTask);

      const result = await service.createTask(personalTaskData);

      expect(mockAuthenticatedClient.post).toHaveBeenCalledWith('/api/tasks', personalTaskData);
      expect(result).toEqual(mockPersonalTask);
    });

    it("handles missing required fields", async () => {
      const incompleteTaskData = {
        title: '', // Empty title
        description: 'Test Description',
        projectId: 1,
        ownerId: 1,
        taskType: 'FEATURE' as const,
        status: 'TODO' as const,
        tags: [],
      };

      const validationError = new Error('Title is required');
      mockAuthenticatedClient.post.mockRejectedValueOnce(validationError);

      await expect(service.createTask(incompleteTaskData)).rejects.toThrow('Title is required');
    });
  });

  describe("createTaskWithSpecifiedOwner", () => {
    it("successfully creates a task with specified owner", async () => {
      const mockTask = {
        id: 1,
        title: 'Assigned Task',
        description: 'Assigned Description',
        projectId: 1,
        ownerId: 2, // Different owner
        taskType: 'FEATURE' as const,
        status: 'TODO' as const,
        userHasEditAccess: true,
        createdAt: '2023-01-01T00:00:00Z',
        createdBy: 1,
      };

      const taskData = {
        title: 'Assigned Task',
        description: 'Assigned Description',
        projectId: 1,
        ownerId: 2,
        taskType: 'FEATURE' as const,
        status: 'TODO' as const,
        tags: ['backend'],
      };

      mockAuthenticatedClient.post.mockResolvedValueOnce(mockTask);

      const result = await service.createTaskWithSpecifiedOwner(taskData);

      expect(mockAuthenticatedClient.post).toHaveBeenCalledWith('/api/tasks/with-owner-id', taskData);
      expect(result).toEqual(mockTask);
    });

    it("handles task creation with owner assignment errors", async () => {
      const taskData = {
        title: 'Assigned Task',
        description: 'Assigned Description',
        projectId: 1,
        ownerId: 999, // Invalid owner ID
        taskType: 'FEATURE' as const,
        status: 'TODO' as const,
        tags: [],
      };

      const error = new Error('Invalid owner ID');
      mockAuthenticatedClient.post.mockRejectedValueOnce(error);

      await expect(service.createTaskWithSpecifiedOwner(taskData)).rejects.toThrow('Invalid owner ID');
      expect(mockAuthenticatedClient.post).toHaveBeenCalledWith('/api/tasks/with-owner-id', taskData);
    });

    it("creates task assigned to team member", async () => {
      const teamTaskData = {
        title: 'Team Task',
        description: 'Task for team member',
        projectId: 1,
        ownerId: 3,
        taskType: 'BUG' as const,
        status: 'TODO' as const,
        tags: ['urgent', 'backend'],
      };

      const mockTeamTask = {
        id: 3,
        title: 'Team Task',
        description: 'Task for team member',
        projectId: 1,
        ownerId: 3,
        taskType: 'BUG' as const,
        status: 'TODO' as const,
        userHasEditAccess: true,
        createdAt: '2023-01-01T00:00:00Z',
        createdBy: 1,
      };

      mockAuthenticatedClient.post.mockResolvedValueOnce(mockTeamTask);

      const result = await service.createTaskWithSpecifiedOwner(teamTaskData);

      expect(mockAuthenticatedClient.post).toHaveBeenCalledWith('/api/tasks/with-owner-id', teamTaskData);
      expect(result).toEqual(mockTeamTask);
    });

    it("handles authorization errors for manager-only endpoint", async () => {
      const taskData = {
        title: 'Unauthorized Task',
        description: 'Should fail for non-managers',
        projectId: 1,
        ownerId: 2,
        taskType: 'FEATURE' as const,
        status: 'TODO' as const,
        tags: [],
      };

      const authError = new Error('Insufficient permissions');
      mockAuthenticatedClient.post.mockRejectedValueOnce(authError);

      await expect(service.createTaskWithSpecifiedOwner(taskData)).rejects.toThrow('Insufficient permissions');
    });

    it("validates ownerId is provided", async () => {
      const taskDataWithoutOwner = {
        title: 'Task without owner',
        description: 'Missing owner ID',
        projectId: 1,
        taskType: 'FEATURE' as const,
        status: 'TODO' as const,
        tags: [],
        // ownerId missing
      };

      const validationError = new Error('Owner ID is required');
      mockAuthenticatedClient.post.mockRejectedValueOnce(validationError);

      await expect(service.createTaskWithSpecifiedOwner(taskDataWithoutOwner as any)).rejects.toThrow('Owner ID is required');
    });
  });

  describe("getProjectTasks", () => {
    it("successfully retrieves project tasks", async () => {
      const mockTasks = [
        { 
          id: 1, 
          title: 'Task 1', 
          projectId: 1, 
          ownerId: 1, 
          taskType: 'FEATURE' as const, 
          status: 'TODO' as const,
          userHasEditAccess: true,
          createdAt: '2023-01-01T00:00:00Z',
          createdBy: 1,
        },
        { 
          id: 2, 
          title: 'Task 2', 
          projectId: 1, 
          ownerId: 1, 
          taskType: 'BUG' as const, 
          status: 'IN_PROGRESS' as const,
          userHasEditAccess: true,
          createdAt: '2023-01-02T00:00:00Z',
          createdBy: 1,
        },
      ];

      mockAuthenticatedClient.get.mockResolvedValueOnce(mockTasks);

      const result = await service.getProjectTasks(1);

      expect(mockAuthenticatedClient.get).toHaveBeenCalledWith('/api/tasks/project/1');
      expect(result).toEqual(mockTasks);
    });

    it("includes tag filters when provided", async () => {
      mockAuthenticatedClient.get.mockResolvedValueOnce([]);

      await service.getProjectTasks(1, ['Urgent', ' Backend ']);

      expect(mockAuthenticatedClient.get).toHaveBeenCalledWith('/api/tasks/project/1?tags=Urgent&tags=Backend');
    });

    it("handles project tasks retrieval errors", async () => {
      const error = new Error('Project not found');
      mockAuthenticatedClient.get.mockRejectedValueOnce(error);

      await expect(service.getProjectTasks(999)).rejects.toThrow('Project not found');
    });

    it("returns empty array for project with no tasks", async () => {
      mockAuthenticatedClient.get.mockResolvedValueOnce([]);

      const result = await service.getProjectTasks(1);

      expect(result).toEqual([]);
    });
  });

  describe("getUserProjects", () => {
    it("successfully retrieves user projects", async () => {
      const mockProjects = [
        { 
          id: 1, 
          name: 'Project Alpha', 
          ownerId: 1,
          createdAt: '2023-01-01T00:00:00Z',
          updatedAt: '2023-01-01T00:00:00Z',
          taskCount: 5,
          completedTaskCount: 2,
        },
        { 
          id: 2, 
          name: 'Project Beta', 
          ownerId: 2,
          createdAt: '2023-01-02T00:00:00Z',
          updatedAt: '2023-01-02T00:00:00Z',
          taskCount: 3,
          completedTaskCount: 1,
        },
      ];

      mockAuthenticatedClient.get.mockResolvedValueOnce(mockProjects);

      const result = await service.getUserProjects(1);

      expect(mockAuthenticatedClient.get).toHaveBeenCalledWith('/api/projects');
      expect(result).toEqual(mockProjects);
    });

    it("handles user projects retrieval errors", async () => {
      const error = new Error('Unauthorized');
      mockAuthenticatedClient.get.mockRejectedValueOnce(error);

      await expect(service.getUserProjects(1)).rejects.toThrow('Unauthorized');
    });

    it("returns empty array when user has no projects", async () => {
      mockAuthenticatedClient.get.mockResolvedValueOnce([]);

      const result = await service.getUserProjects(1);

      expect(result).toEqual([]);
    });
  });

  describe("getPersonalTasks", () => {
    it("successfully retrieves personal tasks", async () => {
      const mockPersonalTasks = [
        { 
          id: 1, 
          title: 'Personal Task 1', 
          ownerId: 1, 
          taskType: 'FEATURE' as const, 
          status: 'TODO' as const,
          userHasEditAccess: true,
          createdAt: '2023-01-01T00:00:00Z',
          createdBy: 1,
        },
      ];

      mockAuthenticatedClient.get.mockResolvedValueOnce(mockPersonalTasks);

      const result = await service.getPersonalTasks(1);

      expect(mockAuthenticatedClient.get).toHaveBeenCalledWith('/api/tasks/personal');
      expect(result).toEqual(mockPersonalTasks);
    });

    it("includes tag filters when provided", async () => {
      mockAuthenticatedClient.get.mockResolvedValueOnce([]);

      await service.getPersonalTasks(1, ['focus', 'deep work']);

      expect(mockAuthenticatedClient.get).toHaveBeenCalledWith('/api/tasks/personal?tags=focus&tags=deep+work');
    });

    it("handles personal tasks retrieval errors", async () => {
      const error = new Error('Unauthorized');
      mockAuthenticatedClient.get.mockRejectedValueOnce(error);

      await expect(service.getPersonalTasks(1)).rejects.toThrow('Unauthorized');
    });
  });

  describe("getAllUserTasks", () => {
    it("successfully retrieves all user tasks", async () => {
      const mockAllTasks = [
        { 
          id: 1, 
          title: 'Personal Task', 
          ownerId: 1, 
          taskType: 'FEATURE' as const, 
          status: 'TODO' as const,
          userHasEditAccess: true,
          createdAt: '2023-01-01T00:00:00Z',
          createdBy: 1,
        },
        { 
          id: 2, 
          title: 'Project Task', 
          projectId: 1,
          ownerId: 1, 
          taskType: 'BUG' as const, 
          status: 'IN_PROGRESS' as const,
          userHasEditAccess: true,
          createdAt: '2023-01-02T00:00:00Z',
          createdBy: 1,
        },
      ];

      mockAuthenticatedClient.get.mockResolvedValueOnce(mockAllTasks);

      const result = await service.getAllUserTasks(1);

      expect(mockAuthenticatedClient.get).toHaveBeenCalledWith('/api/tasks/user');
      expect(result).toEqual(mockAllTasks);
    });

    it("includes tag filters when provided", async () => {
      mockAuthenticatedClient.get.mockResolvedValueOnce([]);

      await service.getAllUserTasks(1, ['backend']);

      expect(mockAuthenticatedClient.get).toHaveBeenCalledWith('/api/tasks/user?tags=backend');
    });

    it("handles all user tasks retrieval errors", async () => {
      const error = new Error('Unauthorized');
      mockAuthenticatedClient.get.mockRejectedValueOnce(error);

      await expect(service.getAllUserTasks(1)).rejects.toThrow('Unauthorized');
    });
  });

  describe("getRelatedProjectTasks", () => {
    it("includes tag filters when provided", async () => {
      mockAuthenticatedClient.get.mockResolvedValueOnce([]);

      await service.getRelatedProjectTasks(['Research']);

      expect(mockAuthenticatedClient.get).toHaveBeenCalledWith('/api/tasks/user/related?tags=Research');
    });
  });

  describe("Singleton instance", () => {
    it("exports a singleton instance", () => {
      expect(projectService).toBeInstanceOf(ProjectService);
    });

    it("singleton instance has all methods", () => {
      expect(typeof projectService.createTask).toBe("function");
      expect(typeof projectService.createTaskWithSpecifiedOwner).toBe("function");
      expect(typeof projectService.getProjectTasks).toBe("function");
      expect(typeof projectService.getUserProjects).toBe("function");
      expect(typeof projectService.getPersonalTasks).toBe("function");
      expect(typeof projectService.getAllUserTasks).toBe("function");
    });
  });

  describe("Edge cases", () => {
    it("handles null response from createTask", async () => {
      const taskData = {
        title: 'Test Task',
        description: 'Test Description',
        projectId: 1,
        ownerId: 1,
        taskType: 'FEATURE' as const,
        status: 'TODO' as const,
        tags: [],
      };

      mockAuthenticatedClient.post.mockResolvedValueOnce(null);

      const result = await service.createTask(taskData);

      expect(result).toBeNull();
    });

    it("handles network errors gracefully", async () => {
      const taskData = {
        title: 'Test Task',
        description: 'Test Description',
        projectId: 1,
        ownerId: 1,
        taskType: 'FEATURE' as const,
        status: 'TODO' as const,
        tags: [],
      };

      const networkError = new Error('Network Error');
      networkError.name = 'NetworkError';
      mockAuthenticatedClient.post.mockRejectedValueOnce(networkError);

      await expect(service.createTask(taskData)).rejects.toThrow('Network Error');
    });

    it("handles timeout errors", async () => {
      const taskData = {
        title: 'Test Task',
        description: 'Test Description',
        projectId: 1,
        ownerId: 1,
        taskType: 'FEATURE' as const,
        status: 'TODO' as const,
        tags: [],
      };

      const timeoutError = new Error('Request timeout');
      mockAuthenticatedClient.post.mockRejectedValueOnce(timeoutError);

      await expect(service.createTask(taskData)).rejects.toThrow('Request timeout');
    });

    it("validates task data structure", async () => {
      const invalidTaskData = {
        // Missing required fields
        description: 'Invalid task data',
      };

      const validationError = new Error('Invalid task data structure');
      mockAuthenticatedClient.post.mockRejectedValueOnce(validationError);

      await expect(service.createTask(invalidTaskData as any)).rejects.toThrow('Invalid task data structure');
    });
  });

  describe("Constructor", () => {
    it("initializes with new instance of authenticated API client", () => {
      const newService = new ProjectService();
      
      expect(MockAuthenticatedApiClient).toHaveBeenCalled();
    });
  });
});
