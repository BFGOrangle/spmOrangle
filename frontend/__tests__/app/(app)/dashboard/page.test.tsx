import { render, screen } from "@testing-library/react";
import Dashboard from "../../../../app/(app)/dashboard/page";

// Mock ThemeToggle component
jest.mock("@/components/theme-toggle", () => ({
  ThemeToggle: () => <div data-testid="theme-toggle">Theme Toggle</div>,
}));

// Mock sidebar components
jest.mock("@/components/ui/sidebar", () => ({
  SidebarInset: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="sidebar-inset">{children}</div>
  ),
  SidebarTrigger: ({
    className,
    ...props
  }: {
    className?: string;
    [key: string]: unknown;
  }) => (
    <button data-testid="sidebar-trigger" className={className} {...props}>
      Toggle
    </button>
  ),
  useSidebar: () => ({
    open: true,
    setOpen: jest.fn(),
    openMobile: false,
    setOpenMobile: jest.fn(),
    isMobile: false,
    state: "expanded",
    setState: jest.fn(),
  }),
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
  CardDescription: ({
    children,
    className,
  }: {
    children: React.ReactNode;
    className?: string;
  }) => (
    <div data-testid="card-description" className={className}>
      {children}
    </div>
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
    variant,
  }: {
    children: React.ReactNode;
    className?: string;
    variant?: string;
  }) => (
    <span data-testid="badge" className={className} data-variant={variant}>
      {children}
    </span>
  ),
}));

jest.mock("@/components/ui/button", () => ({
  Button: ({
    children,
    className,
    size,
    variant,
    ...props
  }: {
    children: React.ReactNode;
    className?: string;
    size?: string;
    variant?: string;
    [key: string]: unknown;
  }) => (
    <button
      data-testid="button"
      className={className}
      data-size={size}
      data-variant={variant}
      {...props}
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

describe("Dashboard Page", () => {
  it("renders the dashboard page with main heading", () => {
    render(<Dashboard />);

    expect(screen.getByText("My Analytics")).toBeInTheDocument();
    expect(
      screen.getByText(
        "A single view of delivery health across projects, priorities, and owners.",
      ),
    ).toBeInTheDocument();
  });

  it("renders within SidebarInset", () => {
    render(<Dashboard />);

    expect(screen.getByTestId("sidebar-inset")).toBeInTheDocument();
  });

  it("renders sidebar trigger", () => {
    render(<Dashboard />);

    expect(screen.getByTestId("sidebar-trigger")).toBeInTheDocument();
  });

  it("renders all metric cards", () => {
    render(<Dashboard />);

    // Check for metric card descriptions
    expect(screen.getByText("Active projects")).toBeInTheDocument();
    expect(screen.getByText("Task completion")).toBeInTheDocument();
    expect(screen.getByText("High priority focus")).toBeInTheDocument();
    expect(screen.getByText("Blocked tasks")).toBeInTheDocument();
  });

  it("renders project health section", () => {
    render(<Dashboard />);

    expect(screen.getByText("Project health")).toBeInTheDocument();
    expect(
      screen.getByText(
        "Status, ownership, and completion trends across your portfolio.",
      ),
    ).toBeInTheDocument();
  });

  it("renders upcoming commitments section", () => {
    render(<Dashboard />);

    expect(screen.getByText("Upcoming commitments")).toBeInTheDocument();
    expect(
      screen.getByText(
        "Due dates and owners so you can unblock ahead of time.",
      ),
    ).toBeInTheDocument();
  });

  it("renders team load section", () => {
    render(<Dashboard />);

    expect(screen.getByText("Team load")).toBeInTheDocument();
    expect(
      screen.getByText("Who is carrying the most tasks right now."),
    ).toBeInTheDocument();
  });

  it("renders priority queue section", () => {
    render(<Dashboard />);

    expect(screen.getByText("Priority queue")).toBeInTheDocument();
    expect(
      screen.getByText("High-urgency items that warrant immediate attention."),
    ).toBeInTheDocument();
  });

  it("renders action buttons", () => {
    render(<Dashboard />);

    expect(screen.getByText("Share update")).toBeInTheDocument();
    expect(screen.getByText("View all")).toBeInTheDocument();
    expect(screen.getByText("Add reminder")).toBeInTheDocument();
  });

  it("renders without crashing", () => {
    expect(() => render(<Dashboard />)).not.toThrow();
  });

  it("exports default function correctly", () => {
    // Verify that Dashboard is a function component
    expect(typeof Dashboard).toBe("function");
    expect(Dashboard.name).toBe("Dashboard");
  });

  it("has stable rendering - multiple renders produce same result", () => {
    const { unmount, container: container1 } = render(<Dashboard />);
    const html1 = container1.innerHTML;
    unmount();

    const { container: container2 } = render(<Dashboard />);
    const html2 = container2.innerHTML;

    expect(html1).toBe(html2);
  });

  it("renders with no props required", () => {
    // Verify component doesn't require any props
    expect(() => render(<Dashboard />)).not.toThrow();
  });

  it("displays proper grid layout structure", () => {
    const { container } = render(<Dashboard />);

    // Check for grid layout classes in the container
    const gridElements = container.querySelectorAll('[class*="grid"]');
    expect(gridElements.length).toBeGreaterThan(0);
  });

  it("renders all card components", () => {
    render(<Dashboard />);

    const cards = screen.getAllByTestId("card");
    expect(cards.length).toBeGreaterThan(0);
  });
});
