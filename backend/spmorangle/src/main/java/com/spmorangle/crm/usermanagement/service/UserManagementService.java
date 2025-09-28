package com.spmorangle.crm.usermanagement.service;

import com.spmorangle.crm.usermanagement.dto.CreateUserDto;
import com.spmorangle.crm.usermanagement.dto.UpdateUserRoleDto;
import com.spmorangle.crm.usermanagement.dto.UserResponseDto;

import java.util.List;
import java.util.UUID;

public interface UserManagementService {
    void createUser(CreateUserDto createStaffDto);
    UserResponseDto getUserById(Long staffId);
    UserResponseDto getUserByCognitoSub(UUID cognitoSub);
    void updateUserRole(UpdateUserRoleDto updateStaffDto);
    void deleteUser(Long staffId);
    void toggleUserStatus(Long staffId, boolean isActive);
    boolean isUserExistsByEmail(String email);
    List<String> getUserTypes();
    List<UserResponseDto> getProjectMembers(Long projectId);
    List<UserResponseDto> getUsersByIds(List<Long> userIds);
}
