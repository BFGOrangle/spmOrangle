/**
 * User Management Service
 *
 * Handles user-related API operations including both authenticated and unauthenticated requests.
 * - Signup: Uses unauthenticated client (no bearer token required)
 * - Other operations: Uses authenticated client (bearer token required)
 */

import { AuthenticatedApiClient } from "./authenticated-api-client";
import { UnauthenticatedApiClient } from "./unauthenticated-api-client";
import {
  CreateUserDto,
  UpdateUserDto,
  UpdateUserRoleDto,
  UserResponseDto,
  SignUpRequest,
} from "@/types/user";

export class UserManagementService {
  private authenticatedClient: AuthenticatedApiClient;
  private unauthenticatedClient: UnauthenticatedApiClient;

  constructor() {
    this.authenticatedClient = new AuthenticatedApiClient();
    this.unauthenticatedClient = new UnauthenticatedApiClient();
  }

  /**
   * Sign up a new user (unauthenticated endpoint)
   * This maps the frontend signup form to the backend CreateUserDto format
   */
  async signUp(signUpData: SignUpRequest): Promise<void> {
    // Map the signup request to the backend DTO format
    // Note: The backend expects fullName, email, password, roleType
    // For signup, we'll use username as fullName and default roleType to 'STAFF'
    const createUserDto: CreateUserDto = {
      userName: signUpData.username, // Using username as full name for now
      email: signUpData.email,
      password: signUpData.password,
      roleType: "STAFF", // Default role for self-signup
    };

    return this.unauthenticatedClient.post<void>("/api/user/create", createUserDto);
  }

  /**
   * Create a new user (authenticated endpoint - for admin use)
   */
  async createUser(userData: CreateUserDto): Promise<void> {
    return this.authenticatedClient.post<void>("/api/user", userData);
  }

  /**
   * Get user by ID (authenticated)
   */
  async getUserById(userId: number): Promise<UserResponseDto> {
    return this.authenticatedClient.get<UserResponseDto>(
      `/api/user/user-id/${userId}`,
    );
  }

  /**
   * Get user by Cognito Sub (authenticated)
   */
  async getUserByCognitoSub(cognitoSub: string): Promise<UserResponseDto> {
    return this.authenticatedClient.get<UserResponseDto>(
      `/api/user/sub/${cognitoSub}`,
    );
  }

  /**
   * Update user role (authenticated - requires MANAGER, HR, or DIRECTOR role)
   * Replaces the old updateUser method as the backend now only allows role updates
   */
  async updateUserRole(userData: UpdateUserRoleDto): Promise<void> {
    return this.authenticatedClient.put<void>("/api/user/role", userData);
  }

  /**
   * @deprecated Use updateUserRole instead. The backend no longer supports general user updates.
   * Update user (authenticated)
   */
  async updateUser(userData: UpdateUserDto): Promise<void> {
    console.warn("updateUser is deprecated. Use updateUserRole instead.");
    // This method is kept for backward compatibility but will not work with the current backend
    return this.authenticatedClient.put<void>("/api/user", userData);
  }

  /**
   * Delete user (authenticated)
   */
  async deleteUser(userId: number): Promise<void> {
    return this.authenticatedClient.delete<void>(`/api/user/${userId}`);
  }

  /**
   * Get available user types (authenticated)
   */
  async getUserTypes(): Promise<string[]> {
    return this.authenticatedClient.get<string[]>("/api/user/user-types");
  }

  /**
   * Get project members (authenticated)
   * Uses the projects endpoint: GET /{projectId}/members
   */
  async getProjectMembers(projectId: number): Promise<UserResponseDto[]> {
    return this.authenticatedClient.get<UserResponseDto[]>(`/api/projects/${projectId}/members`);
  }

  /**
   * Get users by IDs (authenticated)
   */
  async getUsersByIds(userIds: number[]): Promise<UserResponseDto[]> {
    return this.authenticatedClient.post<UserResponseDto[]>("/api/user/batch", userIds);
  }
}

// Export a singleton instance
export const userManagementService = new UserManagementService();
