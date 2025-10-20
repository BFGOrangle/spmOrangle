// Backend API types for user management

export interface CreateUserDto {
  userName: string;
  email: string;
  password: string;
  roleType: string;
}

export interface UpdateUserDto {
  id: number;
  fullName?: string;
  email?: string;
  roleType?: string;
}

export interface UpdateUserRoleDto {
  userId: number;
  email: string;
  roleType: string;
}

export interface UserResponseDto {
  id: number;
  username: string;
  email: string;
  roleType: string;
  cognitoSub: string;
  isActive?: boolean; // Indicates if user account is active or deactivated
}

export interface SignUpRequest {
  username: string;
  email: string;
  password: string;
}
