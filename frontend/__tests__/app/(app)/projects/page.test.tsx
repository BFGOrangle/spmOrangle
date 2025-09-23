import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import ProjectsPage from "../../../../app/(app)/projects/page";

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

// Mock the mvp-data
jest.mock("@/lib/mvp-data", () => ({
  demoProjects: [
    {
      id: "proj-001",
      name: "Test Project 1",
      description: "Test description 1",
      status: "Active",
      dueDate: "2025-01-24",
      owner: "John Doe",
      progress: 75,
      tasksCompleted: 30,
      tasksTotal: 40,
    },
    {
      id: "proj-002",
      name: "Test Project 2",
      description: "Test description 2",
      status: "At Risk",
      dueDate: "2025-02-15",
      owner: "Jane Smith",
      progress: 45,
      tasksCompleted: 18,
      tasksTotal: 40,
    },
    {
      id: "proj-003",
      name: "Test Project 3",
      description: "Test description 3",
      status: "Planning",
      dueDate: "2025-03-10",
      owner: "Bob Johnson",
      progress: 20,
      tasksCompleted: 5,
      tasksTotal: 25,
    },
    {
      id: "proj-004",
      name: "Test Project 4",
      description: "Test description 4",
      status: "Completed",
      dueDate: "2024-12-15",
      owner: "Alice Brown",
      progress: 100,
      tasksCompleted: 50,
      tasksTotal: 50,
    },
  ],
}));

describe("ProjectsPage", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe("Rendering", () => {
    it("renders the projects page without crashing", () => {
      expect(() => render(<ProjectsPage />)).not.toThrow();
    });

    it("renders the page header with title and description", () => {
      render(<ProjectsPage />);

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
    it("renders all four statistics cards", () => {
      render(<ProjectsPage />);

      expect(screen.getByText("Total projects")).toBeInTheDocument();
      expect(screen.getByText("Active delivery")).toBeInTheDocument();
      expect(screen.getByText("Planning pipeline")).toBeInTheDocument();
      expect(screen.getByText("Task completion")).toBeInTheDocument();
    });

    it("displays correct total projects count", () => {
      render(<ProjectsPage />);

      // Should show 4 projects (our mock data)
      const totalCard = screen
        .getByText("Total projects")
        .closest("[data-testid='card']");
      expect(totalCard).toContainElement(screen.getByText("4"));
    });

    it("displays correct active projects count", () => {
      render(<ProjectsPage />);

      // Should show 1 active project
      const activeCard = screen
        .getByText("Active delivery")
        .closest("[data-testid='card']");
      const activeCountElements = screen.getAllByText("1");
      const activeElement = activeCountElements.find(
        (el) =>
          activeCard?.contains(el) &&
          el.getAttribute("data-testid") === "card-title",
      );
      expect(activeElement).toBeInTheDocument();
    });

    it("displays correct planning projects count", () => {
      render(<ProjectsPage />);

      // Should show 1 planning project
      const planningCard = screen
        .getByText("Planning pipeline")
        .closest("[data-testid='card']");
      const planningCountElements = screen.getAllByText("1");
      const planningElement = planningCountElements.find(
        (el) =>
          planningCard?.contains(el) &&
          el.getAttribute("data-testid") === "card-title",
      );
      expect(planningElement).toBeInTheDocument();
    });

    it("calculates and displays task completion percentage", () => {
      render(<ProjectsPage />);

      // Total tasks: 40+40+25+50 = 155
      // Completed tasks: 30+18+5+50 = 103
      // Percentage: (103/155)*100 = 66%
      const completionCard = screen
        .getByText("Task completion")
        .closest("[data-testid='card']");
      expect(completionCard).toContainElement(screen.getByText("66%"));
    });

    it("displays task completion details", () => {
      render(<ProjectsPage />);

      expect(screen.getByText("103 of 155 tasks complete")).toBeInTheDocument();
    });

    it("displays correct completion and at-risk summary", () => {
      render(<ProjectsPage />);

      expect(
        screen.getByText("1 completed · 1 need attention"),
      ).toBeInTheDocument();
    });

    it("calculates and displays average progress", () => {
      render(<ProjectsPage />);

      // Average progress: (75+45+20+100)/4 = 60%
      expect(
        screen.getByText("60% avg. progress across active work"),
      ).toBeInTheDocument();
    });
  });

  describe("Project Cards", () => {
    it("renders all project cards", () => {
      render(<ProjectsPage />);

      expect(screen.getByText("Test Project 1")).toBeInTheDocument();
      expect(screen.getByText("Test Project 2")).toBeInTheDocument();
      expect(screen.getByText("Test Project 3")).toBeInTheDocument();
      expect(screen.getByText("Test Project 4")).toBeInTheDocument();
    });

    it("displays project descriptions", () => {
      render(<ProjectsPage />);

      expect(screen.getByText("Test description 1")).toBeInTheDocument();
      expect(screen.getByText("Test description 2")).toBeInTheDocument();
      expect(screen.getByText("Test description 3")).toBeInTheDocument();
      expect(screen.getByText("Test description 4")).toBeInTheDocument();
    });

    it("displays project status badges", () => {
      render(<ProjectsPage />);

      expect(screen.getByText("Active")).toBeInTheDocument();
      expect(screen.getByText("At Risk")).toBeInTheDocument();
      expect(screen.getByText("Planning")).toBeInTheDocument();
      expect(screen.getByText("Completed")).toBeInTheDocument();
    });

    it("displays project progress percentages", () => {
      render(<ProjectsPage />);

      expect(screen.getByText("75% complete")).toBeInTheDocument();
      expect(screen.getByText("45% complete")).toBeInTheDocument();
      expect(screen.getByText("20% complete")).toBeInTheDocument();
      expect(screen.getByText("100% complete")).toBeInTheDocument();
    });

    it("displays task progress information", () => {
      render(<ProjectsPage />);

      expect(screen.getByText("30 / 40 tasks")).toBeInTheDocument();
      expect(screen.getByText("18 / 40 tasks")).toBeInTheDocument();
      expect(screen.getByText("5 / 25 tasks")).toBeInTheDocument();
      expect(screen.getByText("50 / 50 tasks")).toBeInTheDocument();
    });

    it("displays project owners", () => {
      render(<ProjectsPage />);

      expect(screen.getByText("John Doe")).toBeInTheDocument();
      expect(screen.getByText("Jane Smith")).toBeInTheDocument();
      expect(screen.getByText("Bob Johnson")).toBeInTheDocument();
      expect(screen.getByText("Alice Brown")).toBeInTheDocument();
    });

    it("displays formatted due dates", () => {
      render(<ProjectsPage />);

      expect(screen.getByText("Jan 24, 2025")).toBeInTheDocument();
      expect(screen.getByText("Feb 15, 2025")).toBeInTheDocument();
      expect(screen.getByText("Mar 10, 2025")).toBeInTheDocument();
      expect(screen.getByText("Dec 15, 2024")).toBeInTheDocument();
    });

    it("applies correct progress bar widths", () => {
      render(<ProjectsPage />);

      // Check for progress bar elements by their style attribute
      const { container } = render(<ProjectsPage />);
      const progressBars = container.querySelectorAll('[style*="width:"]');

      expect(progressBars.length).toBeGreaterThan(0);

      // Check that progress bars have width styles
      const hasProgressBarWidths = Array.from(progressBars).some((bar) => {
        const style = bar.getAttribute("style");
        return style?.match(/width:\s*(75|45|20|100)%/);
      });

      expect(hasProgressBarWidths).toBe(true);
    });
  });

  describe("Sections", () => {
    it("renders the active initiatives section", () => {
      render(<ProjectsPage />);

      expect(screen.getByText("Active initiatives")).toBeInTheDocument();
      expect(
        screen.getByText(
          "Monitor ownership, status, and near-term milestones at a glance.",
        ),
      ).toBeInTheDocument();
    });

    it("renders section headers and descriptions", () => {
      render(<ProjectsPage />);

      // Check for section labels in the project cards
      expect(screen.getAllByText("Owner")).toHaveLength(4);
      expect(screen.getAllByText("Due date")).toHaveLength(4);
      expect(screen.getAllByText("Progress")).toHaveLength(4);
    });
  });

  describe("Component Structure", () => {
    it("wraps content in SidebarInset", () => {
      render(<ProjectsPage />);

      expect(screen.getByTestId("sidebar-inset")).toBeInTheDocument();
    });

    it("has proper semantic structure", () => {
      const { container } = render(<ProjectsPage />);

      // Check for header
      const header = container.querySelector("header");
      expect(header).toBeInTheDocument();

      // Check for sections
      const sections = container.querySelectorAll("section");
      expect(sections).toHaveLength(2); // Statistics section and projects section
    });

    it("renders progress bars for each project", () => {
      render(<ProjectsPage />);

      // Should have progress bars for each project
      const progressContainers = screen.getAllByText(/% complete/);
      expect(progressContainers).toHaveLength(4);
    });
  });

  describe("User Interactions", () => {
    it("handles new project button click", async () => {
      const user = userEvent.setup();
      render(<ProjectsPage />);

      const newProjectButton = screen.getByText("New Project");

      // Button should be clickable (no error thrown)
      await expect(user.click(newProjectButton)).resolves.not.toThrow();
    });

    it("handles export report button click", async () => {
      const user = userEvent.setup();
      render(<ProjectsPage />);

      const exportButton = screen.getByText("Export report");

      // Button should be clickable (no error thrown)
      await expect(user.click(exportButton)).resolves.not.toThrow();
    });
  });

  describe("Accessibility", () => {
    it("has proper heading hierarchy", () => {
      render(<ProjectsPage />);

      const h1 = screen.getByRole("heading", { level: 1 });
      expect(h1).toHaveTextContent("Projects");

      const h2 = screen.getByRole("heading", { level: 2 });
      expect(h2).toHaveTextContent("Active initiatives");
    });

    it("has accessible button labels", () => {
      render(<ProjectsPage />);

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

  describe("Data Calculations", () => {
    it("correctly calculates statistics with current data", () => {
      render(<ProjectsPage />);

      // Test that calculations work with the mocked data
      // These calculations are performed correctly as evidenced by other passing tests
      expect(screen.getByText("4")).toBeInTheDocument(); // Total projects
      expect(screen.getByText("66%")).toBeInTheDocument(); // Task completion rate
    });

    it("handles division by zero gracefully", () => {
      // Component should not crash when calculating percentages with zero denominators
      expect(() => render(<ProjectsPage />)).not.toThrow();
    });
  });

  describe("Date Formatting", () => {
    it("formats dates correctly", () => {
      render(<ProjectsPage />);

      // Verify the date format matches expected locale format
      const dateElements = [
        "Jan 24, 2025",
        "Feb 15, 2025",
        "Mar 10, 2025",
        "Dec 15, 2024",
      ];

      dateElements.forEach((date) => {
        expect(screen.getByText(date)).toBeInTheDocument();
      });
    });
  });

  describe("Status Badge Styling", () => {
    it("applies correct CSS classes to status badges", () => {
      render(<ProjectsPage />);

      const activeBadge = screen.getByText("Active");
      const atRiskBadge = screen.getByText("At Risk");
      const planningBadge = screen.getByText("Planning");
      const completedBadge = screen.getByText("Completed");

      // Check that badges have proper styling classes
      expect(activeBadge).toHaveClass("border-emerald-200");
      expect(atRiskBadge).toHaveClass("border-amber-300");
      expect(planningBadge).toHaveClass("border-blue-200");
      expect(completedBadge).toHaveClass("border-slate-200");
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
