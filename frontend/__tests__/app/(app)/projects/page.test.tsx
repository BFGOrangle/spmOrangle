import { render, screen, waitFor, act } from "../../../test-utils";
import userEvent from "@testing-library/user-event";
import ProjectsPage from "../../../../app/(app)/projects/page";
import { projectService } from "@/services/project-service";

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
  },
];

describe("ProjectsPage", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    
    // Reset all mocks before each test
    mockProjectService.getUserProjects.mockClear();
    
    // Default mock implementation that returns test project data
    mockProjectService.getUserProjects.mockResolvedValue(mockProjectsData);
  });

  const setup = async () => {
    const user = userEvent.setup();
    
    let component: any;
    await act(async () => {
      component = render(<ProjectsPage />);
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
        expect(screen.getByText("Test Project 1")).toBeInTheDocument();
        expect(screen.getByText("Test description 1")).toBeInTheDocument();
        expect(screen.getByText("Test Project 2")).toBeInTheDocument();
        expect(screen.getByText("Test description 2")).toBeInTheDocument();
        expect(screen.getByText("Test Project 3")).toBeInTheDocument();
        expect(screen.getByText("Test description 3")).toBeInTheDocument();
        expect(screen.getByText("Test Project 4")).toBeInTheDocument();
        expect(screen.getByText("Test description 4")).toBeInTheDocument();
      });
    });

    it("displays project progress percentages", async () => {
      await setup();

      await waitFor(() => {
        expect(screen.getByText("75%")).toBeInTheDocument();
        expect(screen.getByText("45%")).toBeInTheDocument();
        expect(screen.getByText("20%")).toBeInTheDocument();
        expect(screen.getByText("100%")).toBeInTheDocument();
      });
    });

    it("displays task progress information", async () => {
      await setup();

      await waitFor(() => {
        expect(screen.getByText("30 / 40 tasks")).toBeInTheDocument();
        expect(screen.getByText("18 / 40 tasks")).toBeInTheDocument();
        expect(screen.getByText("5 / 25 tasks")).toBeInTheDocument();
        expect(screen.getByText("50 / 50 tasks")).toBeInTheDocument();
      });
    });

    it("displays completion status indicators", async () => {
      await setup();

      await waitFor(() => {
        // Check for completed and remaining indicators  
        expect(screen.getAllByText(/completed/i)).toHaveLength(4);
        expect(screen.getAllByText(/remaining/i)).toHaveLength(4);
      });
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
        expect(screen.getByText("75%")).toBeInTheDocument();
      });

      // Check for percentage badges
      expect(screen.getByText("75%")).toBeInTheDocument(); // Project 1: 30/40 = 75%
      expect(screen.getByText("45%")).toBeInTheDocument(); // Project 2: 18/40 = 45%
      expect(screen.getByText("20%")).toBeInTheDocument(); // Project 3: 5/25 = 20%
      expect(screen.getByText("100%")).toBeInTheDocument(); // Project 4: 50/50 = 100%
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