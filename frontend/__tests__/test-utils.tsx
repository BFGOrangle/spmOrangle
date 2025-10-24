import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { render as rtlRender, RenderOptions } from "@testing-library/react";
import { ReactElement, ReactNode } from "react";
import { SidebarProvider } from "@/components/ui/sidebar";

// Default mock user context values
const defaultMockUserContext = {
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
  },
  setCurrentUser: jest.fn(),
  isLoading: false,
  isAdmin: false,
  isStaff: true,
  signOut: jest.fn(),
};

// Mock the entire user context module to avoid authentication issues in tests
const mockUseCurrentUser = jest.fn().mockReturnValue(defaultMockUserContext);

jest.mock("@/contexts/user-context", () => ({
  useCurrentUser: mockUseCurrentUser,
  UserProvider: ({ children }: { children: ReactNode }) => children,
}));

// Export the mock for use in tests
export { mockUseCurrentUser };

// Simple wrapper that just returns children since we're mocking the context
const MockUserProvider = ({ children }: { children: ReactNode }) => {
  return <>{children}</>;
};

// Create a custom render function that includes QueryClient
const createTestQueryClient = () =>
  new QueryClient({
    defaultOptions: {
      queries: {
        retry: false, // Don't retry in tests
        staleTime: Infinity, // Prevent automatic refetching
      },
      mutations: {
        retry: false,
      },
    },
  });

interface CustomRenderOptions extends Omit<RenderOptions, "wrapper"> {
  queryClient?: QueryClient;
  currentUser?: Partial<typeof defaultMockUserContext.currentUser> | null;
  isAdmin?: boolean;
  isStaff?: boolean;
  isLoading?: boolean;
}

const AllTheProviders = ({
  children,
  queryClient = createTestQueryClient(),
}: {
  children: ReactNode;
  queryClient?: QueryClient;
}) => {
  return (
    <MockUserProvider>
      <QueryClientProvider client={queryClient}>
        <SidebarProvider>
          {children}
        </SidebarProvider>
      </QueryClientProvider>
    </MockUserProvider>
  );
};

const customRender = (
  ui: ReactElement,
  { 
    queryClient, 
    currentUser, 
    isAdmin, 
    isStaff, 
    isLoading,
    ...options 
  }: CustomRenderOptions = {},
) => {
  // Override mock values if provided
  if (currentUser !== undefined || isAdmin !== undefined || isStaff !== undefined || isLoading !== undefined) {
    mockUseCurrentUser.mockReturnValue({
      ...defaultMockUserContext,
      ...(currentUser !== undefined && { 
        currentUser: currentUser ? { ...defaultMockUserContext.currentUser, ...currentUser } : null 
      }),
      ...(isAdmin !== undefined && { isAdmin }),
      ...(isStaff !== undefined && { isStaff }),
      ...(isLoading !== undefined && { isLoading }),
    });
  } else {
    // Reset to default
    mockUseCurrentUser.mockReturnValue(defaultMockUserContext);
  }

  const Wrapper = ({ children }: { children: ReactNode }) => (
    <AllTheProviders queryClient={queryClient}>{children}</AllTheProviders>
  );

  return rtlRender(ui, { wrapper: Wrapper, ...options });
};

// Re-export everything from testing library
export * from "@testing-library/react";

// Override render with our custom render and export additional utilities
export { customRender as render, createTestQueryClient };
