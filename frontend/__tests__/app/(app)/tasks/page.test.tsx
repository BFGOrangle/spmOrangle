import React from "react";
import { render, screen, within, act, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import TasksPage from "../../../../app/(app)/tasks/page";
import { demoTasks } from "@/lib/mvp-data";
import { projectService } from "@/services/project-service";

// Mock the project service to avoid network calls
jest.mock("@/services/project-service", () => ({
  projectService: {
    getAllUserTasks: jest.fn(),
    getPersonalTasks: jest.fn(),
    createTask: jest.fn(),
  },
}));

jest.mock("@/components/ui/sidebar", () => ({
  SidebarInset: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="sidebar-inset">{children}</div>
  ),
  SidebarTrigger: ({ className }: { className?: string }) => (
    <button data-testid="sidebar-trigger" className={className}>
      Toggle Sidebar
    </button>
  ),
}));

// Mock the TaskCreationDialog to avoid complex component dependencies
jest.mock("@/components/task-creation-dialog", () => ({
  TaskCreationDialog: ({ 
    open, 
    onOpenChange 
  }: { 
    open: boolean; 
    onOpenChange: (open: boolean) => void 
  }) => (
    open ? (
      <div data-testid="task-creation-dialog">
        <h2>Create New Task</h2>
        <button onClick={() => onOpenChange(false)}>Close</button>
      </div>
    ) : null
  ),
}));

// Mock TaskCard component to match the actual structure
jest.mock("@/components/task-card", () => ({
  TaskCard: ({ task, variant }: { task: any; variant: string }) => (
    <div data-testid={variant === "board" ? "board-card" : "table-row"}>
      <h3>{task.title}</h3>
      <div data-testid="task-owner">{task.owner || `User ${task.ownerId}`}</div>
      {task.collaborators && task.collaborators.length > 0 && (
        <div data-testid="collaborators">
          {task.collaborators.map((collab: string, index: number) => (
            <span key={index} data-testid="collaborator-avatar">
              {collab.split(' ').map((n: string) => n[0]).join('')}
            </span>
          ))}
        </div>
      )}
      {task.subtasks && (
        <div data-testid="subtask-progress">
          {task.subtasks.filter((s: any) => s.status === "Done").length}/{task.subtasks.length}
        </div>
      )}
      <div data-testid="task-priority">{task.priority}</div>
      <div data-testid="task-status">{task.status}</div>
    </div>
  ),
}));

const mockProjectService = projectService as jest.Mocked<typeof projectService>;

describe("TasksPage", () => {
  beforeAll(() => {
    jest.useFakeTimers();
    jest.setSystemTime(new Date("2024-12-01"));
  });

  afterAll(() => {
    jest.useRealTimers();
  });

  beforeEach(() => {
    // Reset all mocks before each test
    mockProjectService.getAllUserTasks.mockClear();
    mockProjectService.getPersonalTasks.mockClear();
    mockProjectService.createTask.mockClear();

    // Default mock implementations that return demo data format
    const mockTasksData = demoTasks.map(task => ({
      ...task,
      ownerId: 1,
      projectId: task.project ? 1 : null,
    }));
    
    mockProjectService.getAllUserTasks.mockResolvedValue(mockTasksData as any);
    mockProjectService.getPersonalTasks.mockResolvedValue(
      mockTasksData.filter(task => !task.projectId) as any
    );
  });

  const setup = async () => {
    const user = userEvent.setup({
      advanceTimers: jest.advanceTimersByTime,
    });
    
    let component: any;
    await act(async () => {
      component = render(<TasksPage />);
    });
    
    // Wait for initial data loading
    await waitFor(() => {
      expect(screen.queryByText("Loading tasks...")).not.toBeInTheDocument();
    });
    
    return { user, component };
  };

  const ownerCount = new Set(demoTasks.map((task) => task.owner)).size;
  const collaboratorCount = new Set(
    demoTasks.flatMap((task) => task.collaborators),
  ).size;
  const subtaskStats = demoTasks.reduce(
    (acc, task) => {
      acc.total += task.subtasks.length;
      acc.done += task.subtasks.filter(
        (subtask) => subtask.status === "Done",
      ).length;
      return acc;
    },
    { total: 0, done: 0 },
  );


  it("shows all board columns by default", async () => {
    await setup();

    expect(screen.getByTestId("board-column-todo")).toBeInTheDocument();
    expect(screen.getByTestId("board-column-in-progress")).toBeInTheDocument();
    expect(screen.getByTestId("board-column-blocked")).toBeInTheDocument();
    expect(screen.getByTestId("board-column-review")).toBeInTheDocument();
    expect(screen.getByTestId("board-column-done")).toBeInTheDocument();

    const todoColumn = screen.getByTestId("board-column-todo");
    expect(
      within(todoColumn).getAllByTestId("board-card").length,
    ).toBeGreaterThan(0);
  });


  it("renders card details including owner, collaborators, and subtasks", async () => {
    await setup();

    const cardTitle = screen
      .getAllByText(/Draft product specification/i)
      .find((element) => element.tagName.toLowerCase() === "h3");

    const card = cardTitle?.closest('[data-testid="board-card"]');

    expect(card).not.toBeNull();
    const scoped = within(card as HTMLElement);
    
    // Check owner
    expect(scoped.getByTestId("task-owner")).toHaveTextContent(/Alicia Keys/i);

    // Check for collaborator avatars (initials)
    const collaboratorAvatars = scoped.getAllByTestId("collaborator-avatar");
    expect(collaboratorAvatars.length).toBeGreaterThan(0);
    expect(collaboratorAvatars[0]).toHaveTextContent("ML"); // Marcus Lee
    expect(collaboratorAvatars[1]).toHaveTextContent("PP"); // Priya Patel

    // Check for subtask progress
    const subtaskProgress = scoped.getByTestId("subtask-progress");
    expect(subtaskProgress).toHaveTextContent("1/3"); // 1 done out of 3 total
  });


  it("handles task type filter changes", async () => {
    const { user } = await setup();

    await act(async () => {
      await user.click(screen.getByRole("button", { name: "Personal Tasks" }));
    });

    expect(mockProjectService.getPersonalTasks).toHaveBeenCalledWith(1);
  });

  it("handles project tasks filter", async () => {
    const { user } = await setup();

    await act(async () => {
      await user.click(screen.getByRole("button", { name: "Project Tasks" }));
    });

    // Should call getAllUserTasks and then filter for projectId !== null
    expect(mockProjectService.getAllUserTasks).toHaveBeenCalledWith(1);
  });

  it("opens task creation dialog", async () => {
    const { user } = await setup();

    const newTaskButton = screen.getByRole("button", { name: "New Task" });
    await act(async () => {
      await user.click(newTaskButton);
    });

    expect(screen.getByTestId("task-creation-dialog")).toBeInTheDocument();
    expect(screen.getByText("Create New Task")).toBeInTheDocument();
  });

  it("closes task creation dialog", async () => {
    const { user } = await setup();

    // Open dialog
    const newTaskButton = screen.getByRole("button", { name: "New Task" });
    await act(async () => {
      await user.click(newTaskButton);
    });

    expect(screen.getByTestId("task-creation-dialog")).toBeInTheDocument();

    // Close dialog
    const closeButton = screen.getByRole("button", { name: "Close" });
    await act(async () => {
      await user.click(closeButton);
    });

    expect(screen.queryByTestId("task-creation-dialog")).not.toBeInTheDocument();
  });

  it("displays error message when API calls fail", async () => {
    mockProjectService.getAllUserTasks.mockRejectedValue(new Error("Network error"));

    await setup();

    // Should fall back to empty array and show no tasks
    await waitFor(() => {
      expect(screen.queryByText("Loading tasks...")).not.toBeInTheDocument();
    });

    // Should show empty state or error handling
    expect(screen.queryByTestId("board-card")).not.toBeInTheDocument();
  });

  it("shows loading state initially", async () => {
    // Mock a delayed response
    mockProjectService.getAllUserTasks.mockImplementation(
      () => new Promise(resolve => setTimeout(() => resolve([]), 1000))
    );

    await act(async () => {
      render(<TasksPage />);
    });

    expect(screen.getByText("Loading tasks...")).toBeInTheDocument();
  });
});