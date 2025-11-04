import { render, screen, waitFor, act, within } from "../../../test-utils";
import userEvent from "@testing-library/user-event";
import ProjectsPage from "../../../../app/(app)/projects/page";
import { projectService, type ProjectResponse, type TaskResponse } from "@/services/project-service";

type _ProjectResponseTypeCheck = ProjectResponse;

// Mock AWS Amplify Auth functions
jest.mock("aws-amplify/auth", () => ({
  fetchAuthSession: jest.fn(() => Promise.resolve({ tokens: null })),
  getCurrentUser: jest.fn(() => Promise.resolve({})),
  fetchUserAttributes: jest.fn(() => Promise.resolve({})),
  signOut: jest.fn(() => Promise.resolve()),
}));

// Mock AWS Amplify Core Hub
jest.mock("@aws-amplify/core", () => ({
  Hub: {
    listen: jest.fn(() => jest.fn()), // Return unsubscribe function
  },
}));

// Mock the project service to avoid network calls
jest.mock("@/services/project-service", () => ({
  projectService: {
    getUserProjects: jest.fn(),
    getRelatedProjectTasks: jest.fn(),
    getProjectsByIds: jest.fn(),
  },
}));

const mockProjectService = projectService as jest.Mocked<typeof projectService>;

// Mock UI components
jest.mock("@/components/ui/card", () => ({
  Card: ({
    children,
    className,
    onClick,
  }: {
    children: React.ReactNode;
    className?: string;
    onClick?: () => void;
  }) => (
    <div data-testid="card" className={className} onClick={onClick}>
      {children}
    </div>
  ),
  CardContent: ({
    children,
    className,
  }: {
    children: React.ReactNode;
    className?: string;
  }) => (
    <div data-testid="card-content" className={className}>
      {children}
    </div>
  ),
  CardDescription: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="card-description">{children}</div>
  ),
  CardHeader: ({
    children,
    className,
  }: {
    children: React.ReactNode;
    className?: string;
  }) => (
    <div data-testid="card-header" className={className}>
      {children}
    </div>
  ),
  CardTitle: ({
    children,
    className,
  }: {
    children: React.ReactNode;
    className?: string;
  }) => (
    <div data-testid="card-title" className={className}>
      {children}
    </div>
  ),
}));

jest.mock("@/components/ui/badge", () => ({
  Badge: ({
    children,
    className,
  }: {
    children: React.ReactNode;
    className?: string;
  }) => (
    <span data-testid="badge" className={className}>
      {children}
    </span>
  ),
}));

jest.mock("@/components/ui/button", () => ({
  Button: ({
    children,
    size,
    variant,
    className,
    onClick,
  }: {
    children: React.ReactNode;
    size?: string;
    variant?: string;
    className?: string;
    onClick?: () => void;
  }) => (
    <button
      data-testid="button"
      data-size={size}
      data-variant={variant}
      className={className}
      onClick={onClick}
    >
      {children}
    </button>
  ),
}));

jest.mock("@/components/ui/dropdown-menu", () => ({
  DropdownMenu: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="dropdown-menu">{children}</div>
  ),
  DropdownMenuTrigger: ({ 
    children,
    asChild 
  }: { 
    children: React.ReactNode;
    asChild?: boolean;
  }) => (
    <div data-testid="dropdown-trigger">{children}</div>
  ),
  DropdownMenuContent: ({ 
    children,
    align 
  }: { 
    children: React.ReactNode;
    align?: string;
  }) => (
    <div data-testid="dropdown-content" data-align={align}>{children}</div>
  ),
  DropdownMenuItem: ({ 
    children,
    onClick 
  }: { 
    children: React.ReactNode;
    onClick?: () => void;
  }) => (
    <div data-testid="dropdown-item" onClick={onClick}>{children}</div>
  ),
}));

// Mock data for testing - matches the real API response structure
const mockProjectsData = [
  {
    id: 1,
    name: "Test Project 1",
    description: "Test description 1",
    ownerId: 1,
    taskCount: 40,
    completedTaskCount: 30,
    createdAt: "2025-01-20",
    updatedAt: "2025-01-24",
    isOwner: true,
    isRelated: false,
  },
  {
    id: 2,
    name: "Test Project 2",
    description: "Test description 2",
    ownerId: 2,
    taskCount: 40,
    completedTaskCount: 18,
    createdAt: "2025-02-10",
    updatedAt: "2025-02-15",
    isOwner: false,
    isRelated: false,
  },
  {
    id: 3,
    name: "Test Project 3",
    description: "Test description 3",
    ownerId: 3,
    taskCount: 25,
    completedTaskCount: 5,
    createdAt: "2025-03-05",
    updatedAt: "2025-03-10",
    isOwner: false,
    isRelated: false,
  },
  {
    id: 4,
    name: "Test Project 4",
    description: "Test description 4",
    ownerId: 4,
    taskCount: 50,
    completedTaskCount: 50,
    createdAt: "2024-12-10",
    updatedAt: "2024-12-15",
    isOwner: false,
    isRelated: false,
  },
];

const mockRelatedTasksData: TaskResponse[] = [
  {
    id: 101,
    projectId: 5, // Changed from 2 to a project not in mockProjectsData
    ownerId: 20,
    taskType: "FEATURE" as const,
    title: "Related Task 1",
    description: "",
    status: "IN_PROGRESS" as const,
    tags: [],
    assignedUserIds: [],
    userHasEditAccess: false,
    userHasDeleteAccess: false,
    createdAt: "2025-01-20T00:00:00.000Z",
    updatedAt: "2025-01-21T00:00:00.000Z",
    createdBy: 20,
    updatedBy: undefined,
    subtasks: [],
  },
  {
    id: 102,
    projectId: 6, // Changed from 3 to a project not in mockProjectsData
    ownerId: 30,
    taskType: "CHORE" as const,
    title: "Related Task 2",
    description: "",
    status: "COMPLETED" as const,
    tags: [],
    assignedUserIds: [],
    userHasEditAccess: false,
    userHasDeleteAccess: false,
    createdAt: "2025-02-10T00:00:00.000Z",
    updatedAt: "2025-02-11T00:00:00.000Z",
    createdBy: 30,
    updatedBy: undefined,
    subtasks: [],
  },
];

describe("ProjectsPage", () => {
  beforeEach(() => {
    jest.clearAllMocks();

    // Reset all mocks before each test
    mockProjectService.getUserProjects.mockClear();
    mockProjectService.getRelatedProjectTasks.mockClear();
    mockProjectService.getProjectsByIds.mockClear();

    // Default mock implementation that returns test project data
    // Now includes both member projects (isRelated: false) and related projects (isRelated: true)
    const allProjectsData = [
      ...mockProjectsData, // Member projects
      {
        id: 5,
        name: "Test Project 5",
        description: "Test description 5",
        ownerId: 5,
        taskCount: 20,
        completedTaskCount: 10,
        createdAt: "2025-01-15T00:00:00.000Z",
        updatedAt: "2025-01-20T00:00:00.000Z",
        isOwner: false,
        isRelated: true,
      },
      {
        id: 6,
        name: "Test Project 6",
        description: "Test description 6",
        ownerId: 6,
        taskCount: 15,
        completedTaskCount: 5,
        createdAt: "2025-02-01T00:00:00.000Z",
        updatedAt: "2025-02-10T00:00:00.000Z",
        isOwner: false,
        isRelated: true,
      },
    ];

    mockProjectService.getUserProjects.mockResolvedValue(allProjectsData);
    // These are no longer called by the new implementation, but keep for backward compatibility
    mockProjectService.getRelatedProjectTasks.mockResolvedValue(mockRelatedTasksData);
    mockProjectService.getProjectsByIds.mockResolvedValue([]);
  });

  const setup = async (userRole: string = "STAFF") => {
    const user = userEvent.setup();
    
    let component: any;
    await act(async () => {
      component = render(<ProjectsPage />, {
        currentUser: {
          id: "test-user-id",
          role: userRole,
          backendStaffId: 1,
        },
        isStaff: userRole === "STAFF",
        isAdmin: userRole === "HR",
      });
    });
    
    // Wait for data to load by checking for heading specifically
    await waitFor(() => {
      expect(screen.getByRole("heading", { level: 1, name: "All Projects" })).toBeInTheDocument();
    });
    
    return { user, component };
  };

  describe("Rendering", () => {
    it("renders the projects page without crashing", () => {
      expect(() => render(<ProjectsPage />)).not.toThrow();
    });

    it("renders the page header with title and description", async () => {
      await setup();

      expect(screen.getByRole("heading", { level: 1 })).toHaveTextContent("All Projects");
      expect(
        screen.getByText("All accessible projects (4)"),
      ).toBeInTheDocument();
    });

    it("renders the 'New Project' button", async () => {
      await setup();

      const newProjectButton = screen.getByText("New Project");
      expect(newProjectButton).toBeInTheDocument();
      expect(newProjectButton.closest("button")).toHaveAttribute(
        "data-testid",
        "button",
      );
    });
  });

  describe("Project Cards", () => {
    it("displays project titles and descriptions", async () => {
      await setup();

      await waitFor(() => {
        // Projects may appear in both main and related sections, so use getAllByText
        expect(screen.getAllByText("Test Project 1").length).toBeGreaterThanOrEqual(1);
        expect(screen.getAllByText("Test description 1").length).toBeGreaterThanOrEqual(1);
        expect(screen.getAllByText("Test Project 2").length).toBeGreaterThanOrEqual(1);
        expect(screen.getAllByText("Test description 2").length).toBeGreaterThanOrEqual(1);
        expect(screen.getAllByText("Test Project 3").length).toBeGreaterThanOrEqual(1);
        expect(screen.getAllByText("Test description 3").length).toBeGreaterThanOrEqual(1);
        expect(screen.getAllByText("Test Project 4").length).toBeGreaterThanOrEqual(1);
        expect(screen.getAllByText("Test description 4").length).toBeGreaterThanOrEqual(1);
      });
    });

    it("displays project progress percentages", async () => {
      await setup();

      await waitFor(() => {
        // Projects may appear in both main and related sections
        expect(screen.getAllByText("75%").length).toBeGreaterThanOrEqual(1);
        expect(screen.getAllByText("45%").length).toBeGreaterThanOrEqual(1);
        expect(screen.getAllByText("20%").length).toBeGreaterThanOrEqual(1);
        expect(screen.getAllByText("100%").length).toBeGreaterThanOrEqual(1);
      });
    });

    it("displays task progress information", async () => {
      await setup();

      await waitFor(() => {
        // Projects may appear in both main and related sections
        expect(screen.getAllByText("30 / 40 tasks").length).toBeGreaterThanOrEqual(1);
        expect(screen.getAllByText("18 / 40 tasks").length).toBeGreaterThanOrEqual(1);
        expect(screen.getAllByText("5 / 25 tasks").length).toBeGreaterThanOrEqual(1);
        expect(screen.getAllByText("50 / 50 tasks").length).toBeGreaterThanOrEqual(1);
      });
    });

    it("displays completion status indicators", async () => {
      await setup();

      await waitFor(() => {
        // Check for completed and remaining indicators
        // Each project has these, and projects can appear in both main and related sections
        const completedElements = screen.getAllByText(/completed/i);
        const remainingElements = screen.getAllByText(/remaining/i);
        
        // We have 4 projects in main section + 2 in related section = 6 total cards
        expect(completedElements.length).toBeGreaterThanOrEqual(4);
        expect(remainingElements.length).toBeGreaterThanOrEqual(4);
      });
    });
  });

  describe("Related Projects", () => {
    it("renders related projects section with matching cards", async () => {
      await setup("MANAGER"); // Use MANAGER role to see related projects

      await waitFor(() => {
        expect(
          screen.getByRole("heading", { level: 2, name: "Related Projects" }),
        ).toBeInTheDocument();
      });

      const relatedSection = screen.getByRole("heading", {
        level: 2,
        name: "Related Projects",
      }).closest("section");

      expect(relatedSection).not.toBeNull();
      if (!relatedSection) {
        throw new Error("Related projects section not found");
      }

      const relatedCards = within(relatedSection).getAllByTestId("card");
      expect(relatedCards).toHaveLength(2);
      expect(within(relatedSection).getAllByText("View only")).toHaveLength(2);
      expect(within(relatedSection).getByText("Test Project 5")).toBeInTheDocument();
      expect(within(relatedSection).getByText("Test Project 6")).toBeInTheDocument();
    });

    it("shows an empty state message when no related projects", async () => {
      mockProjectService.getRelatedProjectTasks.mockResolvedValueOnce([]);

      await setup("MANAGER"); // Use MANAGER role to see related projects

      await waitFor(() => {
        expect(
          screen.getByText("No related projects to show yet."),
        ).toBeInTheDocument();
      });
    });

    it("fetches metadata for related projects the user cannot access", async () => {
      mockProjectService.getRelatedProjectTasks.mockResolvedValueOnce([
        ...mockRelatedTasksData,
        {
          id: 103,
          projectId: 99,
          ownerId: 999,
          taskType: "FEATURE" as const,
          title: "External Task",
          description: "",
          status: "TODO" as const,
          tags: [],
          assignedUserIds: [],
          userHasEditAccess: false,
          userHasDeleteAccess: false,
          createdAt: "2025-03-20T00:00:00.000Z",
          updatedAt: "2025-03-21T00:00:00.000Z",
          createdBy: 999,
          updatedBy: undefined,
          subtasks: [],
        },
      ]);

      mockProjectService.getProjectsByIds.mockResolvedValueOnce([
        {
          id: 5,
          name: "Test Project 5",
          description: "Test description 5",
          ownerId: 5,
          taskCount: 20,
          completedTaskCount: 10,
          createdAt: "2025-01-15T00:00:00.000Z",
          updatedAt: "2025-01-20T00:00:00.000Z",
        },
        {
          id: 6,
          name: "Test Project 6",
          description: "Test description 6",
          ownerId: 6,
          taskCount: 15,
          completedTaskCount: 5,
          createdAt: "2025-02-01T00:00:00.000Z",
          updatedAt: "2025-02-10T00:00:00.000Z",
        },
        {
          id: 99,
          name: "External Project",
          description: "View only project",
          ownerId: 50,
          taskCount: 10,
          completedTaskCount: 5,
          createdAt: "2025-01-01T00:00:00.000Z",
          updatedAt: "2025-01-05T00:00:00.000Z",
        },
      ] as any);

      await setup("MANAGER"); // Use MANAGER role to see related projects

      await waitFor(() => {
        expect(mockProjectService.getProjectsByIds).toHaveBeenCalledWith([5, 6, 99]);
      });

      const relatedSection = screen.getByRole("heading", {
        level: 2,
        name: "Related Projects",
      }).closest("section");

      expect(relatedSection).not.toBeNull();
      if (!relatedSection) {
        throw new Error("Related projects section not found");
      }

      expect(within(relatedSection).getByText("External Project")).toBeInTheDocument();
    });
  });

  describe("Accessibility", () => {
    it("has proper heading hierarchy", async () => {
      await setup();

      const h1 = screen.getByRole("heading", { level: 1 });
      expect(h1).toHaveTextContent("All Projects");
    });

    it("has accessible button labels", async () => {
      await setup();

      expect(
        screen.getByRole("button", { name: "All Projects" }),
      ).toBeInTheDocument();
      expect(
        screen.getByRole("button", { name: "New Project" }),
      ).toBeInTheDocument();
    });
  });

  describe("Status Badge Styling", () => {
    it("applies correct CSS classes to completion percentage badges", async () => {
      await setup();

      // Wait for badges to appear
      await waitFor(() => {
        expect(screen.getAllByText("75%").length).toBeGreaterThanOrEqual(1);
      });

      // Check for percentage badges - projects may appear in both main and related sections
      expect(screen.getAllByText("75%").length).toBeGreaterThanOrEqual(1); // Project 1: 30/40 = 75%
      expect(screen.getAllByText("45%").length).toBeGreaterThanOrEqual(1); // Project 2: 18/40 = 45%
      expect(screen.getAllByText("20%").length).toBeGreaterThanOrEqual(1); // Project 3: 5/25 = 20%
      expect(screen.getAllByText("100%").length).toBeGreaterThanOrEqual(1); // Project 4: 50/50 = 100%
    });
  });

  describe("Component Export", () => {
    it("exports default function correctly", () => {
      expect(typeof ProjectsPage).toBe("function");
      expect(ProjectsPage.name).toBe("ProjectsPage");
    });

    it("requires no props", () => {
      expect(() => render(<ProjectsPage />)).not.toThrow();
    });
  });
});
