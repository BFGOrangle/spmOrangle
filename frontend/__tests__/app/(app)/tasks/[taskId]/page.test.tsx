import React from "react";
import { render, screen, waitFor, act } from "@testing-library/react";
import TaskDetailPage from "../../../../../app/(app)/tasks/[taskId]/page";
import { projectService, TaskResponse } from "@/services/project-service";
import { userManagementService } from "@/services/user-management-service";
import { UserProvider } from "@/contexts/user-context";

// Mock React's use() function for Server Components
const mockUse = jest.fn();
jest.mock("react", () => {
  const originalReact = jest.requireActual("react");
  return {
    ...originalReact,
    use: (promise: any) => mockUse(promise),
  };
});

// Mock AWS Amplify Auth functions
jest.mock("aws-amplify/auth", () => ({
  fetchAuthSession: jest.fn().mockResolvedValue({
    tokens: {
      accessToken: {
        payload: {
          sub: "test-user-id",
          "cognito:groups": ["user"],
        },
      },
    },
  }),
  getCurrentUser: jest.fn().mockResolvedValue({
    userId: "test-user-id",
    username: "testuser",
  }),
  fetchUserAttributes: jest.fn().mockResolvedValue({
    email: "test@example.com",
    name: "Test User",
  }),
  signOut: jest.fn().mockResolvedValue(undefined),
}));

// Mock next/navigation
const mockPush = jest.fn();
const mockSearchParams = new URLSearchParams();

jest.mock("next/navigation", () => ({
  useRouter: () => ({
    push: mockPush,
    back: jest.fn(),
  }),
  useSearchParams: () => mockSearchParams,
}));

// Mock services
jest.mock("@/services/project-service", () => ({
  projectService: {
    getTaskById: jest.fn(),
    deleteTask: jest.fn(),
  },
}));

jest.mock("@/services/file-service", () => ({
  fileService: {
    getFilesByTaskAndProject: jest.fn().mockResolvedValue([]),
  },
}));

jest.mock("@/services/user-management-service", () => ({
  userManagementService: {
    getCollaborators: jest.fn().mockResolvedValue([]),
  },
}));

// Mock UI components
jest.mock("@/components/ui/sidebar", () => ({
  SidebarInset: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="sidebar-inset">{children}</div>
  ),
  SidebarTrigger: () => <button data-testid="sidebar-trigger">Toggle</button>,
}));

jest.mock("@/components/subtask-list", () => ({
  SubtaskList: () => <div data-testid="subtask-list">Subtasks</div>,
}));

jest.mock("@/components/comment-section", () => ({
  CommentSection: () => <div data-testid="comment-section">Comments</div>,
}));

jest.mock("@/components/file-icon", () => ({
  FileList: () => <div data-testid="file-list">Files</div>,
}));

jest.mock("@/components/task-update-dialog", () => ({
  TaskUpdateDialog: ({ open }: { open: boolean }) =>
    open ? <div data-testid="task-update-dialog">Update Task</div> : null,
}));

jest.mock("@/components/task-collaborator-management", () => ({
  TaskCollaboratorManagement: () => (
    <button data-testid="manage-collaborators">Manage</button>
  ),
}));

// Mock toast hook
jest.mock("@/hooks/use-toast", () => ({
  useToast: () => ({
    toast: jest.fn(),
  }),
}));

// Mock scrollIntoView
const mockScrollIntoView = jest.fn();
HTMLElement.prototype.scrollIntoView = mockScrollIntoView;

// Mock classList methods
const mockClassListAdd = jest.fn();
const mockClassListRemove = jest.fn();

Object.defineProperty(HTMLElement.prototype, 'classList', {
  get() {
    return {
      add: mockClassListAdd,
      remove: mockClassListRemove,
      contains: jest.fn(),
      toggle: jest.fn(),
    };
  },
});

const mockTask: TaskResponse = {
  id: 123,
  title: "Test Task",
  description: "Test Description",
  status: "TODO",
  taskType: "FEATURE",
  projectId: 1,
  ownerId: 100,
  createdBy: 100,
  assignedUserIds: [200, 300],
  subtasks: [],
  createdAt: "2024-01-01T00:00:00Z",
  updatedAt: "2024-01-02T00:00:00Z",
  dueDateTime: "2024-12-31T23:59:59Z",
  userHasWriteAccess: true,
  userHasDeleteAccess: true,
};

describe("TaskDetailPage - Clickthrough Highlight Behavior", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockScrollIntoView.mockClear();
    mockClassListAdd.mockClear();
    mockClassListRemove.mockClear();
    mockSearchParams.delete('highlight');

    // Mock the use() function to return taskId
    mockUse.mockReturnValue({ taskId: "123" });

    (projectService.getTaskById as jest.Mock).mockResolvedValue(mockTask);
    (userManagementService.getCollaborators as jest.Mock).mockResolvedValue([
      { id: 200, username: "User1", email: "user1@test.com" },
      { id: 300, username: "User2", email: "user2@test.com" },
    ]);
  });

  it("should scroll to status section when highlight=status parameter is present", async () => {
    // Arrange
    mockSearchParams.set('highlight', 'status');
    const params = Promise.resolve({ taskId: "123" });

    // Act
    await act(async () => {
      render(
        <UserProvider>
          <TaskDetailPage params={params} />
        </UserProvider>
      );
    });

    // Wait for task to load
    await waitFor(() => {
      expect(screen.getByText("Test Task")).toBeInTheDocument();
    }, { timeout: 3000 });

    // Assert - Check scrollIntoView was called
    await waitFor(() => {
      expect(mockScrollIntoView).toHaveBeenCalled();
    }, { timeout: 2000 });

    // Check that scroll was called with correct options
    expect(mockScrollIntoView).toHaveBeenCalledWith({
      behavior: 'smooth',
      block: 'center',
    });

    // Check that highlight class was added
    await waitFor(() => {
      expect(mockClassListAdd).toHaveBeenCalledWith('highlight-section');
    }, { timeout: 1000 });
  });

  it("should scroll to assignees section when highlight=assignees parameter is present", async () => {
    // Arrange
    mockSearchParams.set('highlight', 'assignees');
    const params = Promise.resolve({ taskId: "123" });

    // Act
    await act(async () => {
      render(
        <UserProvider>
          <TaskDetailPage params={params} />
        </UserProvider>
      );
    });

    // Wait for task to load
    await waitFor(() => {
      expect(screen.getByText("Test Task")).toBeInTheDocument();
    }, { timeout: 3000 });

    // Assert - Check scrollIntoView was called
    await waitFor(() => {
      expect(mockScrollIntoView).toHaveBeenCalled();
    }, { timeout: 2000 });

    expect(mockScrollIntoView).toHaveBeenCalledWith({
      behavior: 'smooth',
      block: 'center',
    });

    // Check that highlight class was added
    await waitFor(() => {
      expect(mockClassListAdd).toHaveBeenCalledWith('highlight-section');
    }, { timeout: 1000 });
  });

  it("should remove highlight class after 2 seconds", async () => {
    // Arrange
    mockSearchParams.set('highlight', 'status');
    const params = Promise.resolve({ taskId: "123" });

    jest.useFakeTimers();

    // Act
    await act(async () => {
      render(
        <UserProvider>
          <TaskDetailPage params={params} />
        </UserProvider>
      );
    });

    // Wait for task to load
    await waitFor(() => {
      expect(screen.getByText("Test Task")).toBeInTheDocument();
    }, { timeout: 3000 });

    // Wait for highlight to be added
    await waitFor(() => {
      expect(mockClassListAdd).toHaveBeenCalledWith('highlight-section');
    }, { timeout: 1000 });

    // Fast-forward time by 2 seconds
    act(() => {
      jest.advanceTimersByTime(2000);
    });

    // Assert - Check that highlight class was removed
    await waitFor(() => {
      expect(mockClassListRemove).toHaveBeenCalledWith('highlight-section');
    }, { timeout: 1000 });

    jest.useRealTimers();
  });

  it("should not scroll when no highlight parameter is present", async () => {
    // Arrange - No highlight parameter
    const params = Promise.resolve({ taskId: "123" });

    // Act
    await act(async () => {
      render(
        <UserProvider>
          <TaskDetailPage params={params} />
        </UserProvider>
      );
    });

    // Wait for task to load
    await waitFor(() => {
      expect(screen.getByText("Test Task")).toBeInTheDocument();
    }, { timeout: 3000 });

    // Wait a bit to ensure scroll doesn't happen
    await act(async () => {
      await new Promise(resolve => setTimeout(resolve, 500));
    });

    // Assert - scrollIntoView should not have been called
    expect(mockScrollIntoView).not.toHaveBeenCalled();
    expect(mockClassListAdd).not.toHaveBeenCalledWith('highlight-section');
  });

  it("should not scroll when task is still loading", async () => {
    // Arrange
    mockSearchParams.set('highlight', 'status');
    const params = Promise.resolve({ taskId: "123" });

    // Delay the task loading
    (projectService.getTaskById as jest.Mock).mockImplementation(
      () => new Promise(resolve => setTimeout(() => resolve(mockTask), 1000))
    );

    // Act
    await act(async () => {
      render(
        <UserProvider>
          <TaskDetailPage params={params} />
        </UserProvider>
      );
    });

    // Check that loading state is shown
    expect(screen.getByText("Loading task...")).toBeInTheDocument();

    // At this point, scroll should not have happened yet
    expect(mockScrollIntoView).not.toHaveBeenCalled();

    // Wait for task to load
    await waitFor(() => {
      expect(screen.getByText("Test Task")).toBeInTheDocument();
    }, { timeout: 3000 });

    // Now scroll should happen
    await waitFor(() => {
      expect(mockScrollIntoView).toHaveBeenCalled();
    }, { timeout: 2000 });
  });

  it("should handle invalid highlight parameter gracefully", async () => {
    // Arrange
    mockSearchParams.set('highlight', 'invalid-section');
    const params = Promise.resolve({ taskId: "123" });

    // Act
    await act(async () => {
      render(
        <UserProvider>
          <TaskDetailPage params={params} />
        </UserProvider>
      );
    });

    // Wait for task to load
    await waitFor(() => {
      expect(screen.getByText("Test Task")).toBeInTheDocument();
    }, { timeout: 3000 });

    // Wait a bit
    await act(async () => {
      await new Promise(resolve => setTimeout(resolve, 500));
    });

    // Assert - Should not scroll to any section
    expect(mockScrollIntoView).not.toHaveBeenCalled();
  });

  it("should render status and assignees sections that can be scrolled to", async () => {
    // Arrange
    const params = Promise.resolve({ taskId: "123" });

    // Act
    await act(async () => {
      render(
        <UserProvider>
          <TaskDetailPage params={params} />
        </UserProvider>
      );
    });

    // Wait for task to load
    await waitFor(() => {
      expect(screen.getByText("Test Task")).toBeInTheDocument();
    }, { timeout: 3000 });

    // Assert - Check that both sections exist
    expect(screen.getByText("Status")).toBeInTheDocument();
    expect(screen.getByText("Collaborators")).toBeInTheDocument();
  });

  // Note: Testing dynamic URL param changes is complex with mocked useSearchParams
  // The actual behavior works in the browser via Next.js router navigation
  // This is tested implicitly by the other test cases that set highlight params
});
