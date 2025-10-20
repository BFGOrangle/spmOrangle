import React from "react";
import { render, screen, waitFor, act } from "@testing-library/react";

// Mock AWS Amplify Auth
const mockFetchAuthSession = jest.fn();
const mockSignIn = jest.fn();
const mockSignOut = jest.fn();
const mockGetCurrentUser = jest.fn();
const mockFetchUserAttributes = jest.fn();

jest.mock("aws-amplify/auth", () => ({
  fetchAuthSession: mockFetchAuthSession,
  signIn: mockSignIn,
  signOut: mockSignOut,
  getCurrentUser: mockGetCurrentUser,
  fetchUserAttributes: mockFetchUserAttributes,
}));

// Mock AWS Hub
const mockHubListen = jest.fn();
const mockHubRemove = jest.fn();

jest.mock("@aws-amplify/core", () => ({
  Hub: {
    listen: mockHubListen,
    remove: mockHubRemove,
  },
}));

// Import after mocking
import { UserProvider, useCurrentUser, CurrentUser } from "../../contexts/user-context";

// Test component that uses the user context
const TestComponent = () => {
  const {
    currentUser,
    setCurrentUser,
    isLoading,
    isAdmin,
    isStaff,
    signOut,
  } = useCurrentUser();

  return (
    <div>
      <div data-testid="loading">{isLoading.toString()}</div>
      <div data-testid="is-admin">{isAdmin.toString()}</div>
      <div data-testid="is-staff">{isStaff.toString()}</div>
      <div data-testid="user-id">{currentUser?.id || "null"}</div>
      <div data-testid="user-role">{currentUser?.role || "null"}</div>
      <div data-testid="user-email">{currentUser?.email || "null"}</div>
      <div data-testid="full-name">{currentUser?.fullName || "null"}</div>
      <button onClick={() => signOut()} data-testid="sign-out-btn">
        Sign Out
      </button>
      <button
        onClick={() =>
          setCurrentUser({
            id: "test-id",
            role: "STAFF",
            fullName: "Test User",
          })
        }
        data-testid="set-user-btn"
      >
        Set User
      </button>
    </div>
  );
};

describe("UserProvider", () => {
  const mockUser = {
    username: "testuser",
    userId: "test-cognito-sub",
  };

  const mockAttributes = {
    email: "test@example.com",
    given_name: "John",
    family_name: "Doe",
    name: "John Doe",
    "custom:job_title": "Developer",
  };

  const mockTokens = {
    idToken: {
      toString: () => {
        // Create a mock JWT with base64 encoded payload
        const header = btoa(JSON.stringify({ typ: "JWT", alg: "HS256" }));
        const payload = btoa(
          JSON.stringify({
            sub: "test-cognito-sub",
            "cognito:groups": ["MANAGER"],
            "custom:role": "STAFF",
          })
        );
        const signature = "mock-signature";
        return `${header}.${payload}.${signature}`;
      },
    },
    accessToken: "mock-access-token",
  };

  const mockSession = {
    tokens: mockTokens,
  };

  beforeEach(() => {
    // Clear all mocks
    mockFetchAuthSession.mockClear();
    mockGetCurrentUser.mockClear();
    mockFetchUserAttributes.mockClear();
    mockSignOut.mockClear();
    mockHubListen.mockClear();

    // Setup default mocks
    mockHubListen.mockReturnValue(() => {}); // Mock unsubscribe function
    
    // Mock console methods to avoid noise in tests
    jest.spyOn(console, "error").mockImplementation(() => {});
    jest.spyOn(console, "warn").mockImplementation(() => {});
    jest.spyOn(console, "log").mockImplementation(() => {});
  });

  afterEach(() => {
    jest.restoreAllMocks();
  });

  it("initializes with loading state", () => {
    mockFetchAuthSession.mockImplementation(() => new Promise(() => {})); // Never resolves

    render(
      <UserProvider>
        <TestComponent />
      </UserProvider>
    );

    expect(screen.getByTestId("loading")).toHaveTextContent("true");
    expect(screen.getByTestId("user-id")).toHaveTextContent("null");
  });

  it("loads user successfully with manager role", async () => {
    mockFetchAuthSession.mockResolvedValue(mockSession);
    mockGetCurrentUser.mockResolvedValue(mockUser);
    mockFetchUserAttributes.mockResolvedValue(mockAttributes);

    render(
      <UserProvider>
        <TestComponent />
      </UserProvider>
    );

    await waitFor(() => {
      expect(screen.getByTestId("loading")).toHaveTextContent("false");
    });

    expect(screen.getByTestId("user-id")).toHaveTextContent("test-cognito-sub");
    expect(screen.getByTestId("user-role")).toHaveTextContent("MANAGER");
    expect(screen.getByTestId("user-email")).toHaveTextContent("test@example.com");
    expect(screen.getByTestId("full-name")).toHaveTextContent("John Doe");
    expect(screen.getByTestId("is-admin")).toHaveTextContent("true");
    expect(screen.getByTestId("is-staff")).toHaveTextContent("false");
  });

  it("loads user with staff role", async () => {
    const staffTokens = {
      idToken: {
        toString: () => {
          const header = btoa(JSON.stringify({ typ: "JWT", alg: "HS256" }));
          const payload = btoa(
            JSON.stringify({
              sub: "test-cognito-sub",
              "cognito:groups": ["STAFF"],
            })
          );
          const signature = "mock-signature";
          return `${header}.${payload}.${signature}`;
        },
      },
      accessToken: "mock-access-token",
    };

    mockFetchAuthSession.mockResolvedValue({ tokens: staffTokens });
    mockGetCurrentUser.mockResolvedValue(mockUser);
    mockFetchUserAttributes.mockResolvedValue(mockAttributes);

    render(
      <UserProvider>
        <TestComponent />
      </UserProvider>
    );

    await waitFor(() => {
      expect(screen.getByTestId("loading")).toHaveTextContent("false");
    });

    expect(screen.getByTestId("user-role")).toHaveTextContent("STAFF");
    expect(screen.getByTestId("is-admin")).toHaveTextContent("false");
    expect(screen.getByTestId("is-staff")).toHaveTextContent("true");
  });

  it("loads user with HR role and sets isAdmin to true", async () => {
    const hrTokens = {
      idToken: {
        toString: () => {
          const header = btoa(JSON.stringify({ typ: "JWT", alg: "HS256" }));
          const payload = btoa(
            JSON.stringify({
              sub: "test-cognito-sub",
              "cognito:groups": ["HR"],
            })
          );
          const signature = "mock-signature";
          return `${header}.${payload}.${signature}`;
        },
      },
      accessToken: "mock-access-token",
    };

    mockFetchAuthSession.mockResolvedValue({ tokens: hrTokens });
    mockGetCurrentUser.mockResolvedValue(mockUser);
    mockFetchUserAttributes.mockResolvedValue(mockAttributes);

    render(
      <UserProvider>
        <TestComponent />
      </UserProvider>
    );

    await waitFor(() => {
      expect(screen.getByTestId("loading")).toHaveTextContent("false");
    });

    expect(screen.getByTestId("user-role")).toHaveTextContent("HR");
    expect(screen.getByTestId("is-admin")).toHaveTextContent("true");
    expect(screen.getByTestId("is-staff")).toHaveTextContent("false");
  });

  it("sets isAdmin to true for manager role", async () => {
    const managerTokens = {
      idToken: {
        toString: () => {
          const header = btoa(JSON.stringify({ typ: "JWT", alg: "HS256" }));
          const payload = btoa(
            JSON.stringify({
              sub: "test-cognito-sub",
              "cognito:groups": ["MANAGER"],
            })
          );
          const signature = "mock-signature";
          return `${header}.${payload}.${signature}`;
        },
      },
      accessToken: "mock-access-token",
    };

    mockFetchAuthSession.mockResolvedValue({ tokens: managerTokens });
    mockGetCurrentUser.mockResolvedValue(mockUser);
    mockFetchUserAttributes.mockResolvedValue(mockAttributes);

    render(
      <UserProvider>
        <TestComponent />
      </UserProvider>
    );

    await waitFor(() => {
      expect(screen.getByTestId("loading")).toHaveTextContent("false");
    });

    expect(screen.getByTestId("user-role")).toHaveTextContent("MANAGER");
    expect(screen.getByTestId("is-admin")).toHaveTextContent("true");
    expect(screen.getByTestId("is-staff")).toHaveTextContent("false");
  });

  it("sets isAdmin to false for director role", async () => {
    const directorTokens = {
      idToken: {
        toString: () => {
          const header = btoa(JSON.stringify({ typ: "JWT", alg: "HS256" }));
          const payload = btoa(
            JSON.stringify({
              sub: "test-cognito-sub",
              "cognito:groups": ["DIRECTOR"],
            })
          );
          const signature = "mock-signature";
          return `${header}.${payload}.${signature}`;
        },
      },
      accessToken: "mock-access-token",
    };

    mockFetchAuthSession.mockResolvedValue({ tokens: directorTokens });
    mockGetCurrentUser.mockResolvedValue(mockUser);
    mockFetchUserAttributes.mockResolvedValue(mockAttributes);

    render(
      <UserProvider>
        <TestComponent />
      </UserProvider>
    );

    await waitFor(() => {
      expect(screen.getByTestId("loading")).toHaveTextContent("false");
    });

    expect(screen.getByTestId("user-role")).toHaveTextContent("DIRECTOR");
    expect(screen.getByTestId("is-admin")).toHaveTextContent("false");
    expect(screen.getByTestId("is-staff")).toHaveTextContent("false");
  });

  it("handles no session tokens", async () => {
    mockFetchAuthSession.mockResolvedValue({ tokens: null });

    render(
      <UserProvider>
        <TestComponent />
      </UserProvider>
    );

    await waitFor(() => {
      expect(screen.getByTestId("loading")).toHaveTextContent("false");
    });

    expect(screen.getByTestId("user-id")).toHaveTextContent("null");
    expect(screen.getByTestId("is-admin")).toHaveTextContent("false");
    expect(screen.getByTestId("is-staff")).toHaveTextContent("false");
  });

  it("handles auth errors gracefully", async () => {
    mockFetchAuthSession.mockRejectedValue(new Error("Auth failed"));

    render(
      <UserProvider>
        <TestComponent />
      </UserProvider>
    );

    await waitFor(() => {
      expect(screen.getByTestId("loading")).toHaveTextContent("false");
    });

    expect(screen.getByTestId("user-id")).toHaveTextContent("null");
  });

  it("handles token decoding errors", async () => {
    const invalidTokens = {
      idToken: {
        toString: () => "invalid.token.format",
      },
      accessToken: "mock-access-token",
    };

    mockFetchAuthSession.mockResolvedValue({ tokens: invalidTokens });
    mockGetCurrentUser.mockResolvedValue(mockUser);
    mockFetchUserAttributes.mockResolvedValue(mockAttributes);

    render(
      <UserProvider>
        <TestComponent />
      </UserProvider>
    );

    await waitFor(() => {
      expect(screen.getByTestId("loading")).toHaveTextContent("false");
    });

    // Should still work with default role
    expect(screen.getByTestId("user-role")).toHaveTextContent("STAFF");
  });

  it("handles missing user attributes gracefully", async () => {
    mockFetchAuthSession.mockResolvedValue(mockSession);
    mockGetCurrentUser.mockResolvedValue(mockUser);
    mockFetchUserAttributes.mockResolvedValue({});

    render(
      <UserProvider>
        <TestComponent />
      </UserProvider>
    );

    await waitFor(() => {
      expect(screen.getByTestId("loading")).toHaveTextContent("false");
    });

    expect(screen.getByTestId("user-id")).toHaveTextContent("test-cognito-sub");
    expect(screen.getByTestId("full-name")).toHaveTextContent("null");
    expect(screen.getByTestId("user-email")).toHaveTextContent("null");
  });

  it("constructs full name from given and family names when name not provided", async () => {
    const attributesWithoutName = {
      ...mockAttributes,
      name: undefined,
      given_name: "Jane",
      family_name: "Smith",
    };

    mockFetchAuthSession.mockResolvedValue(mockSession);
    mockGetCurrentUser.mockResolvedValue(mockUser);
    mockFetchUserAttributes.mockResolvedValue(attributesWithoutName);

    render(
      <UserProvider>
        <TestComponent />
      </UserProvider>
    );

    await waitFor(() => {
      expect(screen.getByTestId("loading")).toHaveTextContent("false");
    });

    expect(screen.getByTestId("full-name")).toHaveTextContent("Jane Smith");
  });

  it("calls signOut correctly", async () => {
    mockFetchAuthSession.mockResolvedValue(mockSession);
    mockGetCurrentUser.mockResolvedValue(mockUser);
    mockFetchUserAttributes.mockResolvedValue(mockAttributes);
    mockSignOut.mockResolvedValue(undefined);

    render(
      <UserProvider>
        <TestComponent />
      </UserProvider>
    );

    await waitFor(() => {
      expect(screen.getByTestId("loading")).toHaveTextContent("false");
    });

    const signOutBtn = screen.getByTestId("sign-out-btn");
    await act(async () => {
      signOutBtn.click();
    });

    expect(mockSignOut).toHaveBeenCalledTimes(1);
  });

  it("handles signOut errors gracefully", async () => {
    mockFetchAuthSession.mockResolvedValue(mockSession);
    mockGetCurrentUser.mockResolvedValue(mockUser);
    mockFetchUserAttributes.mockResolvedValue(mockAttributes);
    mockSignOut.mockRejectedValue(new Error("Sign out failed"));

    render(
      <UserProvider>
        <TestComponent />
      </UserProvider>
    );

    await waitFor(() => {
      expect(screen.getByTestId("loading")).toHaveTextContent("false");
    });

    const signOutBtn = screen.getByTestId("sign-out-btn");
    await act(async () => {
      signOutBtn.click();
    });

    expect(mockSignOut).toHaveBeenCalledTimes(1);
  });

  it("setCurrentUser shows warning", async () => {
    const consoleSpy = jest.spyOn(console, "warn");
    
    mockFetchAuthSession.mockResolvedValue(mockSession);
    mockGetCurrentUser.mockResolvedValue(mockUser);
    mockFetchUserAttributes.mockResolvedValue(mockAttributes);

    render(
      <UserProvider>
        <TestComponent />
      </UserProvider>
    );

    await waitFor(() => {
      expect(screen.getByTestId("loading")).toHaveTextContent("false");
    });

    const setUserBtn = screen.getByTestId("set-user-btn");
    act(() => {
      setUserBtn.click();
    });

    expect(consoleSpy).toHaveBeenCalledWith(
      "setCurrentUser called but user state is managed by Amplify"
    );
  });

  it("listens to auth hub events", () => {
    render(
      <UserProvider>
        <TestComponent />
      </UserProvider>
    );

    expect(mockHubListen).toHaveBeenCalledWith("auth", expect.any(Function));
  });

  it("throws error when used outside provider", () => {
    const originalError = console.error;
    console.error = jest.fn();

    expect(() => {
      render(<TestComponent />);
    }).toThrow("useCurrentUser must be used within a UserProvider");

    console.error = originalError;
  });

  it("handles role extraction from custom role attribute", async () => {
    const tokensWithCustomRole = {
      idToken: {
        toString: () => {
          const header = btoa(JSON.stringify({ typ: "JWT", alg: "HS256" }));
          const payload = btoa(
            JSON.stringify({
              sub: "test-cognito-sub",
              "custom:role": "DIRECTOR",
            })
          );
          const signature = "mock-signature";
          return `${header}.${payload}.${signature}`;
        },
      },
      accessToken: "mock-access-token",
    };

    mockFetchAuthSession.mockResolvedValue({ tokens: tokensWithCustomRole });
    mockGetCurrentUser.mockResolvedValue(mockUser);
    mockFetchUserAttributes.mockResolvedValue(mockAttributes);

    render(
      <UserProvider>
        <TestComponent />
      </UserProvider>
    );

    await waitFor(() => {
      expect(screen.getByTestId("loading")).toHaveTextContent("false");
    });

    expect(screen.getByTestId("user-role")).toHaveTextContent("DIRECTOR");
  });
});
