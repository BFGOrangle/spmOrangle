import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { AppSidebar } from "../../components/app-sidebar";
import { handleSignOut } from "@/lib/cognito-actions";

// Mock the cognito-actions module
jest.mock("@/lib/cognito-actions", () => ({
  handleSignOut: jest.fn(),
}));

// Mock Next.js Link
jest.mock("next/link", () => {
  return function MockLink({ children, href, ...props }: any) {
    return (
      <a href={href} {...props}>
        {children}
      </a>
    );
  };
});

// Mock ThemeToggle component
jest.mock("@/components/theme-toggle", () => ({
  ThemeToggle: () => <div data-testid="theme-toggle">Theme Toggle</div>,
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
  SidebarMenuButton: ({ children, asChild, ...props }: any) => {
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
  Layout: () => <div data-testid="layout-icon" />,
  LogOut: () => <div data-testid="logout-icon" />,
  RefreshCcwDot: () => <div data-testid="refresh-icon" />,
  User: () => <div data-testid="user-icon" />,
}));

const mockHandleSignOut = handleSignOut as jest.MockedFunction<typeof handleSignOut>;

// Helper function to render with SidebarProvider
const renderWithSidebarProvider = (component: React.ReactElement) => {
  const { SidebarProvider } = require("@/components/ui/sidebar");
  return render(<SidebarProvider>{component}</SidebarProvider>);
};

describe("AppSidebar", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("renders navigation links correctly", () => {
    renderWithSidebarProvider(<AppSidebar />);

    // Check for main navigation elements
    expect(screen.getByText("Application")).toBeInTheDocument();
    expect(screen.getByText("My Analytics")).toBeInTheDocument();
    expect(screen.getByText("My Profile")).toBeInTheDocument();

    // Check for links
    expect(screen.getByRole("link", { name: /my analytics/i })).toHaveAttribute(
      "href",
      "/dashboard",
    );
    expect(screen.getByRole("link", { name: /my profile/i })).toHaveAttribute(
      "href",
      "/profile",
    );
  });

  it("renders sign out button", () => {
    renderWithSidebarProvider(<AppSidebar />);

    const signOutButton = screen.getByRole("button", { name: /sign out/i });
    expect(signOutButton).toBeInTheDocument();
  });

  it("calls handleSignOut when sign out button is clicked", async () => {
    renderWithSidebarProvider(<AppSidebar />);

    const signOutButton = screen.getByRole("button", { name: /sign out/i });
    await userEvent.click(signOutButton);

    expect(handleSignOut).toHaveBeenCalledTimes(1);
  });

  it("renders all required icons", () => {
    renderWithSidebarProvider(<AppSidebar />);

    // Check for icons using their test IDs
    expect(screen.getByTestId("layout-icon")).toBeInTheDocument();
    expect(screen.getByTestId("logout-icon")).toBeInTheDocument();
    expect(screen.getByTestId("refresh-icon")).toBeInTheDocument();
  });

  it("has correct structure for sidebar sections", () => {
    renderWithSidebarProvider(<AppSidebar />);

    // Check for sidebar structural elements
    expect(screen.getByTestId("sidebar")).toBeInTheDocument();
    expect(screen.getByTestId("sidebar-header")).toBeInTheDocument();
    expect(screen.getByTestId("sidebar-content")).toBeInTheDocument();
    expect(screen.getByTestId("sidebar-footer")).toBeInTheDocument();
  });

  it("includes theme toggle component", () => {
    renderWithSidebarProvider(<AppSidebar />);

    expect(screen.getByTestId("theme-toggle")).toBeInTheDocument();
  });
});
