import { render, screen } from "@testing-library/react";
import Dashboard from "../../../../app/(app)/dashboard/page";
import { UserProvider } from "@/contexts/user-context";

// Mock AWS Amplify
jest.mock("@aws-amplify/core", () => ({
  Hub: {
    listen: jest.fn(() => jest.fn()),
  },
}));

jest.mock("aws-amplify/auth", () => ({
  fetchAuthSession: jest.fn().mockResolvedValue({ tokens: null }),
  getCurrentUser: jest.fn().mockResolvedValue({ username: "testuser" }),
  fetchUserAttributes: jest.fn().mockResolvedValue({
    email: "test@example.com",
    given_name: "Test",
    family_name: "User",
    name: "Test User",
  }),
  signOut: jest.fn().mockResolvedValue(undefined),
}));

// Mock user management service
jest.mock("@/services/user-management-service", () => ({
  userManagementService: {
    getUserByCognitoSub: jest.fn().mockResolvedValue({
      id: 1,
      firstName: "Test",
      lastName: "User",
      email: "test@example.com",
    }),
  },
}));

// Mock project service
jest.mock("@/services/project-service", () => ({
  projectService: {
    getUserProjects: jest.fn().mockResolvedValue([]),
    getAllUserTasks: jest.fn().mockResolvedValue([]),
  },
  ProjectResponse: {},
  TaskResponse: {},
}));

jest.mock("@/services/dashboard-service", () => ({
  dashboardService: {
    getDepartmentDashboard: jest.fn().mockResolvedValue({
      department: "Marketing",
      includedDepartments: ["Marketing"],
      metrics: {
        activeProjects: 0,
        totalTasks: 0,
        completedTasks: 0,
        blockedTasks: 0,
        highPriorityTasks: 0,
        completionRate: 0,
      },
      projects: [],
      upcomingCommitments: [],
      priorityQueue: [],
      teamLoad: [],
    }),
  },
}));

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

describe("Dashboard", () => {
  it("renders within SidebarInset", () => {
    render(
      <UserProvider>
        <Dashboard />
      </UserProvider>
    );

    expect(screen.getByTestId("sidebar-inset")).toBeInTheDocument();
  });

  it("renders without crashing", () => {
    expect(() => render(
      <UserProvider>
        <Dashboard />
      </UserProvider>
    )).not.toThrow();
  });

  it("exports default function correctly", () => {
    // Verify that Dashboard is a function component
    expect(typeof Dashboard).toBe("function");
    expect(Dashboard.name).toBe("Dashboard");
  });

  it("has stable rendering - multiple renders produce same result", () => {
    const { unmount, container: container1 } = render(
      <UserProvider>
        <Dashboard />
      </UserProvider>
    );
    const html1 = container1.innerHTML;
    unmount();

    const { container: container2 } = render(
      <UserProvider>
        <Dashboard />
      </UserProvider>
    );
    const html2 = container2.innerHTML;

    expect(html1).toBe(html2);
  });

  it("renders with no props required", () => {
    // Verify component doesn't require any props
    expect(() => render(
      <UserProvider>
        <Dashboard />
      </UserProvider>
    )).not.toThrow();
  });
});
