import { render, screen, waitFor, act } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import ProjectsPage from "../../../../app/(app)/projects/page";
import { projectService } from "@/services/project-service";

// Mock the project service to avoid network calls
jest.mock("@/services/project-service", () => ({
  projectService: {
    getUserProjects: jest.fn(),
  },
}));

const mockProjectService = projectService as jest.Mocked<typeof projectService>;

// Mock SidebarInset and SidebarTrigger components
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

// Mock UI components
jest.mock("@/components/ui/card", () => ({
  Card: ({
    children,
    className,
  }: {
    children: React.ReactNode;
    className?: string;
  }) => (
    <div data-testid="card" className={className}>
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

jest.mock("@/components/ui/separator", () => ({
  Separator: ({ className }: { className?: string }) => (
    <hr data-testid="separator" className={className} />
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
    
    // Wait for loading to complete - loading skeletons should be gone
    await waitFor(() => {
      // Check that loading skeleton is not present by looking for the skeleton divs
      const loadingSkeletons = component.container.querySelectorAll('.animate-pulse');
      expect(loadingSkeletons).toHaveLength(0);
    });
    
    return { user, component };
  };

  describe("Rendering", () => {
    it("renders the projects page without crashing", () => {
      expect(() => render(<ProjectsPage />)).not.toThrow();
    });

    it("renders the page header with title and description", async () => {
      await setup();

      expect(screen.getByText("Projects")).toBeInTheDocument();
      expect(
        screen.getByText(
          "A snapshot of where every initiative stands so your team can stay in sync.",
        ),
      ).toBeInTheDocument();
    });

    it("renders the sidebar trigger", () => {
      render(<ProjectsPage />);

      expect(screen.getByTestId("sidebar-trigger")).toBeInTheDocument();
    });

    it("renders the 'New Project' button", () => {
      render(<ProjectsPage />);

      const newProjectButton = screen.getByText("New Project");
      expect(newProjectButton).toBeInTheDocument();
      expect(newProjectButton.closest("button")).toHaveAttribute(
        "data-size",
        "sm",
      );
    });

    it("renders the 'Export report' button", () => {
      render(<ProjectsPage />);

      const exportButton = screen.getByText("Export report");
      expect(exportButton).toBeInTheDocument();
      expect(exportButton.closest("button")).toHaveAttribute(
        "data-variant",
        "ghost",
      );
      expect(exportButton.closest("button")).toHaveAttribute("data-size", "sm");
    });
  });

  describe("Statistics Cards", () => {
    it("renders all four statistics cards", async () => {
      await setup();

      expect(screen.getByText("Total projects")).toBeInTheDocument();
      expect(screen.getByText("Active delivery")).toBeInTheDocument();
      expect(screen.getByText("Planning pipeline")).toBeInTheDocument();
      expect(screen.getByText("Task completion")).toBeInTheDocument();
    });

    it("displays correct planning projects count", () => {
      render(<ProjectsPage />);

      // Real projects don't have status, so planning should be 0
      const planningCard = screen
        .getByText("Planning pipeline")
        .closest("[data-testid='card']");
      const planningCountElements = screen.getAllByText("0");
      const planningElement = planningCountElements.find(
        (el) =>
          planningCard?.contains(el) &&
          el.getAttribute("data-testid") === "card-title",
      );
      expect(planningElement).toBeInTheDocument();
    });

    it("displays correct completion and at-risk summary", () => {
      render(<ProjectsPage />);

      // Real projects don't have status tracking, so should be "0 completed · 0 need attention"
      expect(
        screen.getByText("0 completed · 0 need attention"),
      ).toBeInTheDocument();
    });

  describe("Project Cards", () => {

    it("displays project status badges", async () => {
      await setup();

      await waitFor(() => {
        // All real projects default to "Active" status
        const activeBadges = screen.getAllByText("Active");
        expect(activeBadges).toHaveLength(4);
      });
    });

    it("displays project progress percentages", async () => {
      await setup();

      await waitFor(() => {
        expect(screen.getByText("75% complete")).toBeInTheDocument();
        expect(screen.getByText("45% complete")).toBeInTheDocument();
        expect(screen.getByText("20% complete")).toBeInTheDocument();
        expect(screen.getByText("100% complete")).toBeInTheDocument();
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

    it("displays project owners", async () => {
      await setup();

      await waitFor(() => {
        // Real projects show "User {ownerId}" format
        expect(screen.getByText("User 1")).toBeInTheDocument();
        expect(screen.getByText("User 2")).toBeInTheDocument();
        expect(screen.getByText("User 3")).toBeInTheDocument();
        expect(screen.getByText("User 4")).toBeInTheDocument();
      });
    });


    it("applies correct progress bar widths", async () => {
      const { component } = await setup();

      // Check for progress bar elements by their style attribute
      const progressBars = component.container.querySelectorAll('[style*="width:"]');

      await waitFor(() => {
        expect(progressBars.length).toBeGreaterThan(0);
      });

      // Check that progress bars have width styles
      const hasProgressBarWidths = Array.from(progressBars as NodeListOf<Element>).some((bar) => {
        const style = bar.getAttribute("style");
        return style?.match(/width:\s*(75|45|20|100)%/);
      });

      expect(hasProgressBarWidths).toBe(true);
    });
  });

  describe("Sections", () => {
    it("renders the active initiatives section", async () => {
      await setup();

      expect(screen.getByText("Active initiatives")).toBeInTheDocument();
      expect(
        screen.getByText(
          "Monitor ownership, status, and near-term milestones at a glance.",
        ),
      ).toBeInTheDocument();
    });

    it("renders section headers and descriptions", async () => {
      await setup();

      await waitFor(() => {
        // Check for section labels in the project cards
        expect(screen.getAllByText("Owner")).toHaveLength(4);
        expect(screen.getAllByText("Progress")).toHaveLength(4);
        // Real projects show "Updated" instead of "Due date"
        expect(screen.getAllByText("Updated")).toHaveLength(4);
      });
    });
  });

  describe("Component Structure", () => {
    it("wraps content in SidebarInset", async () => {
      await setup();

      expect(screen.getByTestId("sidebar-inset")).toBeInTheDocument();
    });

    it("has proper semantic structure", async () => {
      const { component } = await setup();

      // Check for header
      const header = component.container.querySelector("header");
      expect(header).toBeInTheDocument();

      // Check for sections
      const sections = component.container.querySelectorAll("section");
      expect(sections).toHaveLength(2); // Statistics section and projects section
    });

    it("renders progress bars for each project", async () => {
      await setup();

      await waitFor(() => {
        // Should have progress bars for each project
        const progressContainers = screen.getAllByText(/% complete/);
        expect(progressContainers).toHaveLength(4);
      });
    });
  });

  describe("User Interactions", () => {
    it("handles new project button click", async () => {
      const { user } = await setup();

      const newProjectButton = screen.getByText("New Project");

      // Button should be clickable (no error thrown)
      await expect(user.click(newProjectButton)).resolves.not.toThrow();
    });

    it("handles export report button click", async () => {
      const { user } = await setup();

      const exportButton = screen.getByText("Export report");

      // Button should be clickable (no error thrown)
      await expect(user.click(exportButton)).resolves.not.toThrow();
    });
  });

  describe("Accessibility", () => {
    it("has proper heading hierarchy", async () => {
      await setup();

      const h1 = screen.getByRole("heading", { level: 1 });
      expect(h1).toHaveTextContent("Projects");

      const h2 = screen.getByRole("heading", { level: 2 });
      expect(h2).toHaveTextContent("Active initiatives");
    });

    it("has accessible button labels", async () => {
      await setup();

      expect(
        screen.getByRole("button", { name: "Toggle Sidebar" }),
      ).toBeInTheDocument();
      expect(
        screen.getByRole("button", { name: "New Project" }),
      ).toBeInTheDocument();
      expect(
        screen.getByRole("button", { name: "Export report" }),
      ).toBeInTheDocument();
    });
  });




  describe("Status Badge Styling", () => {
    it("applies correct CSS classes to status badges", async () => {
      await setup();

      // Wait for badges to appear
      await waitFor(() => {
        expect(screen.getAllByText("Active")).toHaveLength(4);
      });

      // All badges should be "Active" for real projects
      const activeBadges = screen.getAllByText("Active");
      activeBadges.forEach(badge => {
        expect(badge).toHaveClass("border-emerald-200");
      });
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
});

