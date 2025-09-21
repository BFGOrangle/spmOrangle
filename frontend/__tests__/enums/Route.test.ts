import { Route } from "@/enums/Route";

describe("Route Enum", () => {
  test("should contain correct route values", () => {
    expect(Route.SignIn).toBe("/auth/signin");
    expect(Route.Dashboard).toBe("/dashboard");
    expect(Route.Profile).toBe("/profile");
    expect(Route.Unauthorized).toBe("/unauthorized");
    expect(Route.ConfirmSignUp).toBe("/auth/confirm-signup");
    expect(Route.NewPassword).toBe("/auth/new-password");
    expect(Route.ConfirmSMSCode).toBe("/auth/confirm-sms-code");
  });

  test("should have all expected routes", () => {
    const expectedRoutes = [
      "SignIn",
      "Dashboard", 
      "Profile",
      "Unauthorized",
      "ConfirmSignUp",
      "NewPassword",
      "ConfirmSMSCode"
    ];

    expectedRoutes.forEach(routeName => {
      expect(Route).toHaveProperty(routeName);
    });
  });

  test("should have correct number of routes", () => {
    const routeKeys = Object.keys(Route);
    expect(routeKeys).toHaveLength(7);
  });

  test("should contain auth-related routes", () => {
    const authRoutes = [
      Route.SignIn,
      Route.ConfirmSignUp,
      Route.NewPassword,
      Route.ConfirmSMSCode
    ];

    authRoutes.forEach(route => {
      expect(route).toMatch(/^\/auth\//);
    });
  });

  test("should contain app routes", () => {
    expect(Route.Dashboard).toBe("/dashboard");
    expect(Route.Profile).toBe("/profile");
    expect(Route.Unauthorized).toBe("/unauthorized");
  });

  test("should validate route string values", () => {
    // Test that all routes start with forward slash
    Object.values(Route).forEach(route => {
      expect(route).toMatch(/^\//);
    });
  });

  test("should be usable in switch statements", () => {
    const getRouteType = (route: Route): string => {
      switch (route) {
        case Route.SignIn:
        case Route.ConfirmSignUp:
        case Route.NewPassword:
        case Route.ConfirmSMSCode:
          return "auth";
        case Route.Dashboard:
        case Route.Profile:
          return "app";
        case Route.Unauthorized:
          return "error";
        default:
          return "unknown";
      }
    };

    expect(getRouteType(Route.SignIn)).toBe("auth");
    expect(getRouteType(Route.Dashboard)).toBe("app");
    expect(getRouteType(Route.Unauthorized)).toBe("error");
  });

  test("should be usable in arrays and filters", () => {
    const allRoutes = Object.values(Route);
    const authRoutes = allRoutes.filter(route => route.startsWith("/auth"));
    
    expect(authRoutes).toHaveLength(4);
    expect(authRoutes).toContain(Route.SignIn);
    expect(authRoutes).toContain(Route.ConfirmSignUp);
    expect(authRoutes).toContain(Route.NewPassword);
    expect(authRoutes).toContain(Route.ConfirmSMSCode);
  });

  test("should be comparable", () => {
    expect(Route.SignIn).not.toBe(Route.Dashboard);
    expect(Route.SignIn === "/auth/signin").toBe(true);
    expect(Route.Dashboard === "/dashboard").toBe(true);
  });

  test("should support object property access", () => {
    const routeConfig = {
      [Route.SignIn]: { requiresAuth: false, layout: "auth" },
      [Route.Dashboard]: { requiresAuth: true, layout: "app" },
      [Route.Profile]: { requiresAuth: true, layout: "app" },
    };

    expect(routeConfig[Route.SignIn]).toEqual({ 
      requiresAuth: false, 
      layout: "auth" 
    });
    expect(routeConfig[Route.Dashboard]).toEqual({ 
      requiresAuth: true, 
      layout: "app" 
    });
  });
});
