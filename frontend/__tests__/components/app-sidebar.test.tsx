import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { AppSidebar } from "../../components/app-sidebar";
import { handleSignOut } from "@/lib/cognito-actions";
import { render } from "../test-utils";

// Mock the user context directly in this test file to ensure proper precedence
jest.mock("@/contexts/user-context", () => ({
  useCurrentUser: jest.fn(),
  UserProvider: ({ children }: { children: React.ReactNode }) => children,
}));

// Import the mocked function after the mock is defined
import { useCurrentUser } from "@/contexts/user-context";
const mockUseCurrentUser = useCurrentUser as jest.MockedFunction<typeof useCurrentUser>;

// Mock the cognito-actions module
jest.mock("@/lib/cognito-actions", () => ({
  handleSignOut: jest.fn(),
}));

// Mock NotificationBell component
jest.mock("@/components/notification-bell", () => ({
  NotificationBell: () => <div data-testid="notification-bell">Notification Bell</div>,
}));

// Mock Next.js Link
jest.mock("next/link", () => {
  return function MockLink({
    children,
    href,
    ...props
  }: {
    children: React.ReactNode;
    href: string;
    [key: string]: unknown;
  }) {
    return (
      <a href={href} {...props}>
        {children}
      </a>
    );
  };
});

// Mock ThemeToggle component
jest.mock("../../components/theme-toggle", () => ({
  ThemeToggle: () => <div data-testid="theme-toggle">Theme Toggle</div>,
}));

// Mock Button component
jest.mock("../../components/ui/button", () => ({
  Button: ({ 
    children, 
    onClick, 
    variant, 
    size, 
    className, 
    title,
    ...props 
  }: { 
    children: React.ReactNode; 
    onClick?: () => void;
    variant?: string;
    size?: string;
    className?: string;
    title?: string;
    [key: string]: unknown;
  }) => (
    <button 
      data-testid="button" 
      onClick={onClick} 
      className={className}
      title={title}
      {...props}
    >
      {children}
    </button>
  ),
}));

// Mock SidebarProvider
jest.mock("@/components/ui/sidebar", () => ({
  SidebarProvider: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="sidebar-provider">{children}</div>
  ),
  Sidebar: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="sidebar">{children}</div>
  ),
  SidebarContent: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="sidebar-content">{children}</div>
  ),
  SidebarFooter: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="sidebar-footer">{children}</div>
  ),
  SidebarGroup: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="sidebar-group">{children}</div>
  ),
  SidebarGroupContent: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="sidebar-group-content">{children}</div>
  ),
  SidebarGroupLabel: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="sidebar-group-label">{children}</div>
  ),
  SidebarHeader: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="sidebar-header">{children}</div>
  ),
  SidebarMenu: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="sidebar-menu">{children}</div>
  ),
  SidebarMenuButton: ({
    children,
    asChild,
    ...props
  }: {
    children: React.ReactNode;
    asChild?: boolean;
    [key: string]: unknown;
  }) => {
    if (asChild) {
      return <div {...props}>{children}</div>;
    }
    return (
      <button data-testid="sidebar-menu-button" {...props}>
        {children}
      </button>
    );
  },
  SidebarMenuItem: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="sidebar-menu-item">{children}</div>
  ),
  SidebarTrigger: () => <button data-testid="sidebar-trigger">Toggle</button>,
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

// Mock Lucide icons
jest.mock("lucide-react", () => ({
  Briefcase: () => <div data-testid="briefcase-icon" />,
  CheckSquare: () => <div data-testid="check-square-icon" />,
  Layout: () => <div data-testid="layout-icon" />,
  LogOut: () => <div data-testid="logout-icon" />,
  RefreshCcwDot: () => <div data-testid="refresh-icon" />,
  User: () => <div data-testid="user-icon" />,
  Calendar: () => <div data-testid="calendar-icon" />,
  BarChart3: () => <div data-testid="barchart3-icon" />,
  Users: () => <div data-testid="users-icon" />,
}));

// Mock useCurrentUser hook
jest.mock("@/contexts/user-context", () => ({
  useCurrentUser: jest.fn(() => ({
    currentUser: {
      id: "1",
      firstName: "John",
      lastName: "Doe",
      role: "MANAGER",
      email: "john@example.com",
      fullName: "John Doe",
    },
    isLoading: false,
    isAdmin: true,
    isStaff: false,
    signOut: jest.fn(),
  })),
}));

// Helper function to render with SidebarProvider
const renderWithSidebarProvider = (component: React.ReactElement, options: any = {}) => {
  // Set up the mock before rendering
  const defaultUserContext = {
    currentUser: {
      id: "test-user-id",
      firstName: "Test",
      lastName: "User",
      role: "STAFF",
      jobTitle: "Developer",
      email: "test@example.com",
      fullName: "Test User",
      cognitoSub: "cognito-test-sub",
      backendStaffId: 1,
      ...options.currentUser,
    },
    setCurrentUser: jest.fn(),
    isLoading: options.isLoading || false,
    isAdmin: options.isAdmin || false,
    isStaff: options.isStaff !== undefined ? options.isStaff : true,
    signOut: jest.fn(),
  };
  
  mockUseCurrentUser.mockReturnValue(defaultUserContext);
  
  // eslint-disable-next-line @typescript-eslint/no-require-imports
  const { SidebarProvider } = require("@/components/ui/sidebar");
  return render(<SidebarProvider>{component}</SidebarProvider>);
};

describe("AppSidebar", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("renders navigation links correctly", () => {
    renderWithSidebarProvider(<AppSidebar />, {
      currentUser: {
        id: "1",
        role: "MANAGER",
        username: "Manager User",
        email: "manager@example.com",
      },
      isAdmin: false,
      isStaff: false,
    });

    // Check for main navigation elements
    expect(screen.getByText("Application")).toBeInTheDocument();
    expect(screen.getByText("My Analytics")).toBeInTheDocument();
    expect(screen.getByText("Projects")).toBeInTheDocument();
    expect(screen.getByText("Tasks")).toBeInTheDocument();
    expect(screen.getByText("Reports")).toBeInTheDocument();
    expect(screen.getByText("My Profile")).toBeInTheDocument();

    // Check for links
    expect(screen.getByRole("link", { name: /my analytics/i })).toHaveAttribute(
      "href",
      "/dashboard",
    );
    expect(screen.getByRole("link", { name: /projects/i })).toHaveAttribute(
      "href",
      "/projects",
    );
    expect(screen.getByRole("link", { name: /tasks/i })).toHaveAttribute(
      "href",
      "/tasks",
    );
    expect(screen.getByRole("link", { name: /reports/i })).toHaveAttribute(
      "href",
      "/reports",
    );
    expect(screen.getByRole("link", { name: /my profile/i })).toHaveAttribute(
      "href",
      "/profile",
    );
  });

  it("renders sign out button", () => {
    renderWithSidebarProvider(<AppSidebar />, {
      currentUser: {
        id: "1",
        role: "STAFF",
        username: "Staff User",
        email: "staff@example.com",
      },
      isAdmin: false,
      isStaff: true,
    });

    const signOutButton = screen.getByRole("button", { name: /sign out/i });
    expect(signOutButton).toBeInTheDocument();
  });

  it("calls handleSignOut when sign out button is clicked", async () => {
    renderWithSidebarProvider(<AppSidebar />, {
      currentUser: {
        id: "1",
        role: "STAFF",
        username: "Staff User",
        email: "staff@example.com",
      },
      isAdmin: false,
      isStaff: true,
    });

    const signOutButton = screen.getByRole("button", { name: /sign out/i });
    await userEvent.click(signOutButton);

    expect(handleSignOut).toHaveBeenCalledTimes(1);
  });

  it("renders all required icons", () => {
    renderWithSidebarProvider(<AppSidebar />, {
      currentUser: {
        id: "1",
        role: "STAFF",
        username: "Staff User",
        email: "staff@example.com",
      },
      isAdmin: false,
      isStaff: true,
    });

    // Check for icons using their test IDs
    expect(screen.getByTestId("layout-icon")).toBeInTheDocument();
    expect(screen.getByTestId("logout-icon")).toBeInTheDocument();
    expect(screen.getByTestId("refresh-icon")).toBeInTheDocument();
    expect(screen.getByTestId("briefcase-icon")).toBeInTheDocument();
    expect(screen.getByTestId("check-square-icon")).toBeInTheDocument();
  });

  it("has correct structure for sidebar sections", () => {
    renderWithSidebarProvider(<AppSidebar />, {
      currentUser: {
        id: "1",
        role: "STAFF",
        username: "Staff User",
        email: "staff@example.com",
      },
      isAdmin: false,
      isStaff: true,
    });

    // Check for sidebar structural elements
    expect(screen.getByTestId("sidebar")).toBeInTheDocument();
    expect(screen.getByTestId("sidebar-header")).toBeInTheDocument();
    expect(screen.getByTestId("sidebar-content")).toBeInTheDocument();
    expect(screen.getByTestId("sidebar-footer")).toBeInTheDocument();
  });

  it("includes theme toggle component", () => {
    renderWithSidebarProvider(<AppSidebar />, {
      currentUser: {
        id: "1",
        role: "STAFF",
        username: "Staff User",
        email: "staff@example.com",
      },
      isAdmin: false,
      isStaff: true,
    });

    expect(screen.getByTestId("theme-toggle")).toBeInTheDocument();
  });

  it("renders the SyncUp brand name correctly", () => {
    renderWithSidebarProvider(<AppSidebar />, {
      currentUser: {
        id: "1",
        role: "STAFF",
        username: "Staff User",
        email: "staff@example.com",
      },
      isAdmin: false,
      isStaff: true,
    });

    expect(screen.getByText("SyncUp")).toBeInTheDocument();
  });

  it("has correct sign out button attributes", () => {
    renderWithSidebarProvider(<AppSidebar />, {
      currentUser: {
        id: "1",
        role: "STAFF",
        username: "Staff User",
        email: "staff@example.com",
      },
      isAdmin: false,
      isStaff: true,
    });

    const signOutButton = screen.getByRole("button", { name: /sign out/i });
    expect(signOutButton).toHaveAttribute("title", "Sign Out");
    expect(signOutButton).toHaveClass("w-full", "p-2");
  });

  it("renders navigation links in correct order", () => {
    renderWithSidebarProvider(<AppSidebar />, {
      currentUser: {
        id: "1",
        role: "MANAGER",
        username: "Manager User",
        email: "manager@example.com",
      },
      isAdmin: false,
      isStaff: false,
    });

    const links = screen.getAllByRole("link");
    const linkTexts = links.map((link) => link.textContent);

    expect(linkTexts).toContain("My Analytics");
    expect(linkTexts).toContain("Projects");
    expect(linkTexts).toContain("Tasks");
    expect(linkTexts).toContain("Reports");
    expect(linkTexts).toContain("My Profile");
  });

  it("renders all required navigation items from items array", () => {
    renderWithSidebarProvider(<AppSidebar />, {
      currentUser: {
        id: "1",
        role: "MANAGER",
        username: "Manager User",
        email: "manager@example.com",
      },
      isAdmin: false,
      isStaff: false,
    });

    // Check that each menu item from the items array is rendered
    expect(
      screen.getByRole("link", { name: /my analytics/i }),
    ).toBeInTheDocument();
    expect(screen.getByRole("link", { name: /projects/i })).toBeInTheDocument();
    expect(screen.getByRole("link", { name: /tasks/i })).toBeInTheDocument();
    expect(screen.getByRole("link", { name: /reports/i })).toBeInTheDocument();
    expect(
      screen.getByRole("link", { name: /my profile/i }),
    ).toBeInTheDocument();
  });

  it("calls handleSignOut correctly when button is clicked", async () => {
    renderWithSidebarProvider(<AppSidebar />, {
      currentUser: {
        id: "1",
        role: "STAFF",
        username: "Staff User",
        email: "staff@example.com",
      },
      isAdmin: false,
      isStaff: true,
    });

    const signOutButton = screen.getByRole("button", { name: /sign out/i });

    await userEvent.click(signOutButton);

    expect(handleSignOut).toHaveBeenCalledTimes(1);
  });

  it("renders without props (no required props)", () => {
    expect(() => renderWithSidebarProvider(<AppSidebar />, {
      currentUser: {
        id: "1",
        role: "STAFF",
        username: "Staff User",
        email: "staff@example.com",
      },
      isAdmin: false,
      isStaff: true,
    })).not.toThrow();
  });

  it("has proper semantic structure for accessibility", () => {
    renderWithSidebarProvider(<AppSidebar />, {
      currentUser: {
        id: "1",
        role: "MANAGER",
        username: "Manager User",
        email: "manager@example.com",
      },
      isAdmin: false,
      isStaff: false,
    });

    // Check for proper semantic elements
    expect(
      screen.getByRole("button", { name: /sign out/i }),
    ).toBeInTheDocument();
    expect(screen.getAllByRole("link")).toHaveLength(6);
  });

  it("applies correct CSS classes to sign out button", () => {
    renderWithSidebarProvider(<AppSidebar />, {
      currentUser: {
        id: "1",
        role: "STAFF",
        username: "Staff User",
        email: "staff@example.com",
      },
      isAdmin: false,
      isStaff: true,
    });

    const signOutButton = screen.getByRole("button", { name: /sign out/i });

    // Check for expected utility classes
    expect(signOutButton).toHaveClass("w-full");
    expect(signOutButton).toHaveClass("p-2");
  });

  it("renders brand logo with correct styling", () => {
    renderWithSidebarProvider(<AppSidebar />, {
      currentUser: {
        id: "1",
        role: "STAFF",
        username: "Staff User",
        email: "staff@example.com",
      },
      isAdmin: false,
      isStaff: true,
    });

    const refreshIcon = screen.getByTestId("refresh-icon");
    expect(refreshIcon).toBeInTheDocument();

    const brandText = screen.getByText("SyncUp");
    expect(brandText).toBeInTheDocument();
  });

  it("hides Reports link for STAFF users", () => {
    // Mock useCurrentUser for STAFF role
    const { useCurrentUser } = require("@/contexts/user-context");
    useCurrentUser.mockReturnValueOnce({
      currentUser: {
        id: "2",
        firstName: "Jane",
        lastName: "Doe",
        role: "STAFF",
        email: "jane@example.com",
        fullName: "Jane Doe",
      },
      isLoading: false,
      isAdmin: false,
      isStaff: true,
      signOut: jest.fn(),
    });

    renderWithSidebarProvider(<AppSidebar />);

    // Reports should not be visible
    expect(screen.queryByText("Reports")).not.toBeInTheDocument();

    // Other navigation items should still be visible
    expect(screen.getByText("My Analytics")).toBeInTheDocument();
    expect(screen.getByText("Projects")).toBeInTheDocument();
    expect(screen.getByText("Tasks")).toBeInTheDocument();
    expect(screen.getByText("My Profile")).toBeInTheDocument();
  });

  it("shows Reports link for MANAGER users", () => {
    renderWithSidebarProvider(<AppSidebar />, {
      currentUser: {
        id: "1",
        role: "MANAGER",
        username: "Manager User",
        email: "manager@example.com",
      },
      isAdmin: false,
      isStaff: false,
    });

    // Reports should be visible
    expect(screen.getByText("Reports")).toBeInTheDocument();
    expect(screen.getByRole("link", { name: /reports/i })).toHaveAttribute(
      "href",
      "/reports",
    );
  });
  describe("Admin menu items", () => {
    it("shows Administration section and User Management link for admin users", () => {
      renderWithSidebarProvider(<AppSidebar />, {
        currentUser: {
          id: "1",
          role: "MANAGER",
          username: "Admin User",
          email: "admin@example.com",
        },
        isAdmin: true,
        isStaff: false,
      });

      expect(screen.getByText("Administration")).toBeInTheDocument();
      expect(screen.getByText("User Management")).toBeInTheDocument();
      expect(screen.getByRole("link", { name: /user management/i })).toHaveAttribute(
        "href",
        "/user-management",
      );
    });

    it("shows Administration section for HR users", () => {
      renderWithSidebarProvider(<AppSidebar />, {
        currentUser: {
          id: "1",
          role: "HR",
          username: "HR User",
          email: "hr@example.com",
        },
        isAdmin: true,
        isStaff: false,
      });

      expect(screen.getByText("Administration")).toBeInTheDocument();
      expect(screen.getByText("User Management")).toBeInTheDocument();
      expect(screen.getByRole("link", { name: /user management/i })).toHaveAttribute(
        "href",
        "/user-management",
      );
    });

    it("does not show Administration section for staff users", () => {
      renderWithSidebarProvider(<AppSidebar />, {
        currentUser: {
          id: "1",
          role: "STAFF",
          username: "Staff User",
          email: "staff@example.com",
        },
        isAdmin: false,
        isStaff: true,
      });

      expect(screen.queryByText("Administration")).not.toBeInTheDocument();
      expect(screen.queryByText("User Management")).not.toBeInTheDocument();
    });

    it("does not show Administration section for director users", () => {
      renderWithSidebarProvider(<AppSidebar />, {
        currentUser: {
          id: "1",
          role: "DIRECTOR",
          username: "Director User",
          email: "director@example.com",
        },
        isAdmin: false,
        isStaff: false,
      });

      expect(screen.queryByText("Administration")).not.toBeInTheDocument();
      expect(screen.queryByText("User Management")).not.toBeInTheDocument();
    });

    it("renders Users icon for User Management link", () => {
      renderWithSidebarProvider(<AppSidebar />, {
        currentUser: {
          id: "1",
          role: "MANAGER",
          username: "Admin User",
          email: "admin@example.com",
        },
        isAdmin: true,
        isStaff: false,
      });

      expect(screen.getByTestId("users-icon")).toBeInTheDocument();
    });

    it("renders both Application and Administration sections for admin", () => {
      renderWithSidebarProvider(<AppSidebar />, {
        currentUser: {
          id: "1",
          role: "MANAGER",
          username: "Admin User",
          email: "admin@example.com",
        },
        isAdmin: true,
        isStaff: false,
      });

      const sectionLabels = screen.getAllByTestId("sidebar-group-label");
      expect(sectionLabels).toHaveLength(2);
      expect(screen.getByText("Application")).toBeInTheDocument();
      expect(screen.getByText("Administration")).toBeInTheDocument();
    });

    it("renders only Application section for non-admin", () => {
      renderWithSidebarProvider(<AppSidebar />, {
        currentUser: {
          id: "1",
          role: "STAFF",
          username: "Staff User",
          email: "staff@example.com",
        },
        isAdmin: false,
        isStaff: true,
      });

      const sectionLabels = screen.getAllByTestId("sidebar-group-label");
      expect(sectionLabels).toHaveLength(1);
      expect(screen.getByText("Application")).toBeInTheDocument();
    });
  });
});