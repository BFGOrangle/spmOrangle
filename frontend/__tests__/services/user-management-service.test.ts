import { UserManagementService, userManagementService } from "../../services/user-management-service";
import { AuthenticatedApiClient } from "../../services/authenticated-api-client";
import { UnauthenticatedApiClient } from "../../services/unauthenticated-api-client";
import {
  CreateUserDto,
  UpdateUserDto,
  UpdateUserRoleDto,
  UserResponseDto,
  SignUpRequest,
} from "@/types/user";

// Mock the API clients
jest.mock("../../services/authenticated-api-client");
jest.mock("../../services/unauthenticated-api-client");

const MockAuthenticatedApiClient = AuthenticatedApiClient as jest.MockedClass<typeof AuthenticatedApiClient>;
const MockUnauthenticatedApiClient = UnauthenticatedApiClient as jest.MockedClass<typeof UnauthenticatedApiClient>;

describe("UserManagementService", () => {
  let service: UserManagementService;
  let mockAuthenticatedClient: jest.Mocked<AuthenticatedApiClient>;
  let mockUnauthenticatedClient: jest.Mocked<UnauthenticatedApiClient>;

  beforeEach(() => {
    // Create mock instances
    mockAuthenticatedClient = {
      get: jest.fn(),
      post: jest.fn(),
      put: jest.fn(),
      delete: jest.fn(),
      patch: jest.fn(),
    } as any;

    mockUnauthenticatedClient = {
      get: jest.fn(),
      post: jest.fn(),
      put: jest.fn(),
      delete: jest.fn(),
      patch: jest.fn(),
    } as any;

    // Mock the constructors
    MockAuthenticatedApiClient.mockImplementation(() => mockAuthenticatedClient);
    MockUnauthenticatedApiClient.mockImplementation(() => mockUnauthenticatedClient);

    service = new UserManagementService();

    // Mock console.warn for deprecated method
    jest.spyOn(console, "warn").mockImplementation(() => {});
  });

  afterEach(() => {
    jest.restoreAllMocks();
  });

  describe("signUp", () => {
    it("successfully signs up a new user", async () => {
      const signUpData: SignUpRequest = {
        username: "johndoe",
        email: "john@example.com",
        password: "password123",
      };

      mockUnauthenticatedClient.post.mockResolvedValueOnce(undefined);

      await service.signUp(signUpData);

      expect(mockUnauthenticatedClient.post).toHaveBeenCalledWith("/api/user/create", {
        userName: "johndoe",
        email: "john@example.com",
        password: "password123",
        roleType: "STAFF",
      });
    });

    it("handles signup errors", async () => {
      const signUpData: SignUpRequest = {
        username: "johndoe",
        email: "john@example.com",
        password: "password123",
      };

      const error = new Error("Email already exists");
      mockUnauthenticatedClient.post.mockRejectedValueOnce(error);

      await expect(service.signUp(signUpData)).rejects.toThrow("Email already exists");
    });
  });

  describe("createUser", () => {
    it("successfully creates a user via authenticated endpoint", async () => {
      const userData: CreateUserDto = {
        userName: "Jane Smith",
        email: "jane@example.com",
        password: "password123",
        roleType: "MANAGER",
      };

      mockAuthenticatedClient.post.mockResolvedValueOnce(undefined);

      await service.createUser(userData);

      expect(mockAuthenticatedClient.post).toHaveBeenCalledWith("/api/user", userData);
    });

    it("handles user creation errors", async () => {
      const userData: CreateUserDto = {
        userName: "Jane Smith",
        email: "jane@example.com",
        password: "password123",
        roleType: "MANAGER",
      };

      const error = new Error("Unauthorized");
      mockAuthenticatedClient.post.mockRejectedValueOnce(error);

      await expect(service.createUser(userData)).rejects.toThrow("Unauthorized");
    });
  });

  describe("adminCreateUser", () => {
    it("successfully creates a user via admin endpoint", async () => {
      const userData: CreateUserDto = {
        userName: "Admin User",
        email: "admin@example.com",
        password: "password123",
        roleType: "HR",
      };

      mockAuthenticatedClient.post.mockResolvedValueOnce(undefined);

      await service.adminCreateUser(userData);

      expect(mockAuthenticatedClient.post).toHaveBeenCalledWith("/api/user/admin-create", userData);
    });

    it("handles admin user creation errors", async () => {
      const userData: CreateUserDto = {
        userName: "Admin User",
        email: "admin@example.com",
        password: "password123",
        roleType: "MANAGER",
      };

      const error = new Error("Insufficient permissions");
      mockAuthenticatedClient.post.mockRejectedValueOnce(error);

      await expect(service.adminCreateUser(userData)).rejects.toThrow("Insufficient permissions");
    });

    it("handles validation errors for admin create", async () => {
      const userData: CreateUserDto = {
        userName: "",
        email: "invalid-email",
        password: "123",
        roleType: "STAFF",
      };

      const error = new Error("Validation failed");
      mockAuthenticatedClient.post.mockRejectedValueOnce(error);

      await expect(service.adminCreateUser(userData)).rejects.toThrow("Validation failed");
    });

    it("creates user with different roles", async () => {
      const roles = ["STAFF", "MANAGER", "HR", "DIRECTOR"];
      
      for (const role of roles) {
        const userData: CreateUserDto = {
          userName: `User ${role}`,
          email: `user.${role.toLowerCase()}@example.com`,
          password: "password123",
          roleType: role,
        };

        mockAuthenticatedClient.post.mockResolvedValueOnce(undefined);
        await service.adminCreateUser(userData);

        expect(mockAuthenticatedClient.post).toHaveBeenCalledWith("/api/user/admin-create", userData);
      }
    });
  });

  describe("deactivateUser", () => {
    it("successfully deactivates a user", async () => {
      mockAuthenticatedClient.post.mockResolvedValueOnce(undefined);

      await service.deactivateUser(1);

      expect(mockAuthenticatedClient.post).toHaveBeenCalledWith("/api/user/deactivate/1", {});
    });

    it("handles deactivate user errors", async () => {
      const error = new Error("User not found");
      mockAuthenticatedClient.post.mockRejectedValueOnce(error);

      await expect(service.deactivateUser(999)).rejects.toThrow("User not found");
    });

    it("handles insufficient permissions error", async () => {
      const error = new Error("Insufficient permissions");
      mockAuthenticatedClient.post.mockRejectedValueOnce(error);

      await expect(service.deactivateUser(1)).rejects.toThrow("Insufficient permissions");
    });

    it("deactivates user with different IDs", async () => {
      const userIds = [1, 5, 10, 100];
      
      for (const userId of userIds) {
        mockAuthenticatedClient.post.mockResolvedValueOnce(undefined);
        await service.deactivateUser(userId);

        expect(mockAuthenticatedClient.post).toHaveBeenCalledWith(`/api/user/deactivate/${userId}`, {});
      }
    });

    it("handles network errors during deactivation", async () => {
      const networkError = new Error("Network error");
      mockAuthenticatedClient.post.mockRejectedValueOnce(networkError);

      await expect(service.deactivateUser(1)).rejects.toThrow("Network error");
    });

    it("sends empty object in request body", async () => {
      mockAuthenticatedClient.post.mockResolvedValueOnce(undefined);

      await service.deactivateUser(5);

      expect(mockAuthenticatedClient.post).toHaveBeenCalledWith("/api/user/deactivate/5", {});
      const callArgs = mockAuthenticatedClient.post.mock.calls[0];
      expect(callArgs[1]).toEqual({});
    });
  });

  describe("reactivateUser", () => {
    it("successfully reactivates a user", async () => {
      mockAuthenticatedClient.post.mockResolvedValueOnce(undefined);

      await service.reactivateUser(1);

      expect(mockAuthenticatedClient.post).toHaveBeenCalledWith("/api/user/reactivate/1", {});
    });

    it("handles reactivate user errors", async () => {
      const error = new Error("User not found");
      mockAuthenticatedClient.post.mockRejectedValueOnce(error);

      await expect(service.reactivateUser(999)).rejects.toThrow("User not found");
    });

    it("handles insufficient permissions error", async () => {
      const error = new Error("Insufficient permissions");
      mockAuthenticatedClient.post.mockRejectedValueOnce(error);

      await expect(service.reactivateUser(1)).rejects.toThrow("Insufficient permissions");
    });

    it("reactivates user with different IDs", async () => {
      const userIds = [1, 5, 10, 100];
      
      for (const userId of userIds) {
        mockAuthenticatedClient.post.mockResolvedValueOnce(undefined);
        await service.reactivateUser(userId);

        expect(mockAuthenticatedClient.post).toHaveBeenCalledWith(`/api/user/reactivate/${userId}`, {});
      }
    });

    it("handles network errors during reactivation", async () => {
      const networkError = new Error("Network error");
      mockAuthenticatedClient.post.mockRejectedValueOnce(networkError);

      await expect(service.reactivateUser(1)).rejects.toThrow("Network error");
    });

    it("sends empty object in request body", async () => {
      mockAuthenticatedClient.post.mockResolvedValueOnce(undefined);

      await service.reactivateUser(5);

      expect(mockAuthenticatedClient.post).toHaveBeenCalledWith("/api/user/reactivate/5", {});
      const callArgs = mockAuthenticatedClient.post.mock.calls[0];
      expect(callArgs[1]).toEqual({});
    });
  });

  describe("getAllUsers", () => {
    it("successfully retrieves all users", async () => {
      const mockUsers: UserResponseDto[] = [
        {
          id: 1,
          username: "John Doe",
          email: "john@example.com",
          roleType: "STAFF",
          cognitoSub: "cognito-sub-123",
        },
        {
          id: 2,
          username: "Jane Smith",
          email: "jane@example.com",
          roleType: "MANAGER",
          cognitoSub: "cognito-sub-456",
        },
        {
          id: 3,
          username: "Bob Johnson",
          email: "bob@example.com",
          roleType: "HR",
          cognitoSub: "cognito-sub-789",
        },
      ];

      mockAuthenticatedClient.get.mockResolvedValueOnce(mockUsers);

      const result = await service.getAllUsers();

      expect(mockAuthenticatedClient.get).toHaveBeenCalledWith("/api/user/");
      expect(result).toEqual(mockUsers);
      expect(result).toHaveLength(3);
    });

    it("handles empty user list", async () => {
      mockAuthenticatedClient.get.mockResolvedValueOnce([]);

      const result = await service.getAllUsers();

      expect(mockAuthenticatedClient.get).toHaveBeenCalledWith("/api/user/");
      expect(result).toEqual([]);
      expect(result).toHaveLength(0);
    });

    it("handles get all users errors", async () => {
      const error = new Error("Failed to fetch users");
      mockAuthenticatedClient.get.mockRejectedValueOnce(error);

      await expect(service.getAllUsers()).rejects.toThrow("Failed to fetch users");
    });

    it("handles authorization errors", async () => {
      const error = new Error("Unauthorized");
      mockAuthenticatedClient.get.mockRejectedValueOnce(error);

      await expect(service.getAllUsers()).rejects.toThrow("Unauthorized");
    });
  });

  describe("getUserById", () => {
    it("successfully retrieves user by ID", async () => {
      const mockUser: UserResponseDto = {
        id: 1,
        username: "John Doe",
        email: "john@example.com",
        roleType: "STAFF",
        cognitoSub: "cognito-sub-123",
      };

      mockAuthenticatedClient.get.mockResolvedValueOnce(mockUser);

      const result = await service.getUserById(1);

      expect(mockAuthenticatedClient.get).toHaveBeenCalledWith("/api/user/user-id/1");
      expect(result).toEqual(mockUser);
    });

    it("handles get user by ID errors", async () => {
      const error = new Error("User not found");
      mockAuthenticatedClient.get.mockRejectedValueOnce(error);

      await expect(service.getUserById(999)).rejects.toThrow("User not found");
    });
  });

  describe("getUserByCognitoSub", () => {
    it("successfully retrieves user by Cognito Sub", async () => {
      const mockUser: UserResponseDto = {
        id: 1,
        username: "John Doe",
        email: "john@example.com",
        roleType: "STAFF",
        cognitoSub: "cognito-sub-123",
      };

      mockAuthenticatedClient.get.mockResolvedValueOnce(mockUser);

      const result = await service.getUserByCognitoSub("cognito-sub-123");

      expect(mockAuthenticatedClient.get).toHaveBeenCalledWith("/api/user/sub/cognito-sub-123");
      expect(result).toEqual(mockUser);
    });

    it("handles get user by Cognito Sub errors", async () => {
      const error = new Error("User not found");
      mockAuthenticatedClient.get.mockRejectedValueOnce(error);

      await expect(service.getUserByCognitoSub("invalid-sub")).rejects.toThrow("User not found");
    });
  });

  describe("updateUserRole", () => {
    it("successfully updates user role", async () => {
      const userData: UpdateUserRoleDto = {
        userId: 1,
        email: "john@example.com",
        roleType: "MANAGER",
      };

      mockAuthenticatedClient.put.mockResolvedValueOnce(undefined);

      await service.updateUserRole(userData);

      expect(mockAuthenticatedClient.put).toHaveBeenCalledWith("/api/user/role", userData);
    });

    it("handles update user role errors", async () => {
      const userData: UpdateUserRoleDto = {
        userId: 1,
        email: "john@example.com",
        roleType: "MANAGER",
      };

      const error = new Error("Insufficient permissions");
      mockAuthenticatedClient.put.mockRejectedValueOnce(error);

      await expect(service.updateUserRole(userData)).rejects.toThrow("Insufficient permissions");
    });
  });

  describe("updateUser (deprecated)", () => {
    it("calls deprecated updateUser method and shows warning", async () => {
      const consoleSpy = jest.spyOn(console, "warn");
      const userData: UpdateUserDto = {
        id: 1,
        fullName: "John Doe Updated",
        email: "john.updated@example.com",
        roleType: "STAFF",
      };

      mockAuthenticatedClient.put.mockResolvedValueOnce(undefined);

      await service.updateUser(userData);

      expect(consoleSpy).toHaveBeenCalledWith(
        "updateUser is deprecated. Use updateUserRole instead."
      );
      expect(mockAuthenticatedClient.put).toHaveBeenCalledWith("/api/user", userData);
    });
  });

  describe("deleteUser", () => {
    it("successfully deletes user", async () => {
      mockAuthenticatedClient.delete.mockResolvedValueOnce(undefined);

      await service.deleteUser(1);

      expect(mockAuthenticatedClient.delete).toHaveBeenCalledWith("/api/user/1");
    });

    it("handles delete user errors", async () => {
      const error = new Error("Cannot delete user");
      mockAuthenticatedClient.delete.mockRejectedValueOnce(error);

      await expect(service.deleteUser(1)).rejects.toThrow("Cannot delete user");
    });
  });

  describe("getUserTypes", () => {
    it("successfully retrieves user types", async () => {
      const mockTypes = ["STAFF", "MANAGER", "DIRECTOR", "HR"];

      mockAuthenticatedClient.get.mockResolvedValueOnce(mockTypes);

      const result = await service.getUserTypes();

      expect(mockAuthenticatedClient.get).toHaveBeenCalledWith("/api/user/user-types");
      expect(result).toEqual(mockTypes);
    });

    it("handles get user types errors", async () => {
      const error = new Error("Unauthorized");
      mockAuthenticatedClient.get.mockRejectedValueOnce(error);

      await expect(service.getUserTypes()).rejects.toThrow("Unauthorized");
    });
  });

  describe("getProjectMembers", () => {
    const mockProjectMembers = [
      { id: 1, fullName: 'John Manager', email: 'john@test.com' },
      { id: 2, fullName: 'Jane Staff', email: 'jane@test.com' },
      { id: 3, fullName: 'Bob Developer', email: 'bob@test.com' },
    ];

    it("successfully fetches project members", async () => {
      mockAuthenticatedClient.get.mockResolvedValueOnce(mockProjectMembers);

      const result = await service.getProjectMembers(1);

      expect(mockAuthenticatedClient.get).toHaveBeenCalledWith('/api/projects/1/members');
      expect(result).toEqual(mockProjectMembers);
    });

    it("handles API errors gracefully", async () => {
      const mockError = new Error('API Error');
      mockAuthenticatedClient.get.mockRejectedValueOnce(mockError);

      await expect(service.getProjectMembers(1))
        .rejects.toThrow('API Error');

      expect(mockAuthenticatedClient.get).toHaveBeenCalledWith('/api/projects/1/members');
    });

    it("handles invalid project ID", async () => {
      mockAuthenticatedClient.get.mockResolvedValueOnce([]);

      const result = await service.getProjectMembers(999);

      expect(mockAuthenticatedClient.get).toHaveBeenCalledWith('/api/projects/999/members');
      expect(result).toEqual([]);
    });

    it("handles empty project members response", async () => {
      mockAuthenticatedClient.get.mockResolvedValueOnce([]);

      const result = await service.getProjectMembers(1);

      expect(result).toEqual([]);
    });

    it("uses correct endpoint format", async () => {
      mockAuthenticatedClient.get.mockResolvedValueOnce([]);

      await service.getProjectMembers(123);

      expect(mockAuthenticatedClient.get).toHaveBeenCalledWith('/api/projects/123/members');
    });

    it("handles network errors", async () => {
      const networkError = new Error('Network Error');
      networkError.name = 'NetworkError';
      mockAuthenticatedClient.get.mockRejectedValueOnce(networkError);

      await expect(service.getProjectMembers(1))
        .rejects.toThrow('Network Error');
    });

    it("handles timeout errors", async () => {
      const timeoutError = new Error('Request timeout');
      mockAuthenticatedClient.get.mockRejectedValueOnce(timeoutError);

      await expect(service.getProjectMembers(1))
        .rejects.toThrow('Request timeout');
    });

    it("handles malformed response data", async () => {
      const malformedData = { invalid: 'structure' };
      mockAuthenticatedClient.get.mockResolvedValueOnce(malformedData);

      const result = await service.getProjectMembers(1);

      expect(result).toEqual(malformedData);
    });

    it("handles null response", async () => {
      mockAuthenticatedClient.get.mockResolvedValueOnce(null);

      const result = await service.getProjectMembers(1);

      expect(result).toBeNull();
    });

    it("handles response with partial member data", async () => {
      const partialData = [
        { id: 1, fullName: 'John Manager' }, // Missing email
        { id: 2, email: 'jane@test.com' }, // Missing fullName
      ];
      mockAuthenticatedClient.get.mockResolvedValueOnce(partialData);

      const result = await service.getProjectMembers(1);

      expect(result).toEqual(partialData);
    });
  });

  describe("Singleton instance", () => {
    it("exports a singleton instance", () => {
      expect(userManagementService).toBeInstanceOf(UserManagementService);
    });

    it("singleton instance has all methods", () => {
      expect(typeof userManagementService.signUp).toBe("function");
      expect(typeof userManagementService.createUser).toBe("function");
      expect(typeof userManagementService.adminCreateUser).toBe("function");
      expect(typeof userManagementService.deactivateUser).toBe("function");
      expect(typeof userManagementService.getUserById).toBe("function");
      expect(typeof userManagementService.getUserByCognitoSub).toBe("function");
      expect(typeof userManagementService.updateUserRole).toBe("function");
      expect(typeof userManagementService.updateUser).toBe("function");
      expect(typeof userManagementService.deleteUser).toBe("function");
      expect(typeof userManagementService.getUserTypes).toBe("function");
    });
  });

  describe("Edge cases", () => {
    it("handles empty response from createUser", async () => {
      const userData: CreateUserDto = {
        userName: "Test User",
        email: "test@example.com",
        password: "password123",
        roleType: "STAFF",
      };

      mockAuthenticatedClient.post.mockResolvedValueOnce(undefined);

      const result = await service.createUser(userData);

      expect(result).toBeUndefined();
    });

    it("handles null response from getUserById", async () => {
      mockAuthenticatedClient.get.mockResolvedValueOnce(null);

      const result = await service.getUserById(1);

      expect(result).toBeNull();
    });

    it("handles empty array from getUserTypes", async () => {
      mockAuthenticatedClient.get.mockResolvedValueOnce([]);

      const result = await service.getUserTypes();

      expect(result).toEqual([]);
    });

    it("correctly maps signup data to createUser DTO format", async () => {
      const signUpData: SignUpRequest = {
        username: "testuser123",
        email: "test@domain.com",
        password: "securepass",
      };

      mockUnauthenticatedClient.post.mockResolvedValueOnce(undefined);

      await service.signUp(signUpData);

      const expectedDto: CreateUserDto = {
        userName: "testuser123",
        email: "test@domain.com",
        password: "securepass",
        roleType: "STAFF",
      };

      expect(mockUnauthenticatedClient.post).toHaveBeenCalledWith(
        "/api/user/create",
        expectedDto
      );
    });
  });

  describe("Constructor", () => {
    it("initializes with new instances of API clients", () => {
      const newService = new UserManagementService();
      
      expect(MockAuthenticatedApiClient).toHaveBeenCalled();
      expect(MockUnauthenticatedApiClient).toHaveBeenCalled();
    });
  });
});
