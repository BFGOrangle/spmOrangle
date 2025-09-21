package com.spmorangle.crm.usermanagement.service;

import com.spmorangle.crm.usermanagement.dto.CreateUserDto;
import com.spmorangle.crm.usermanagement.dto.UpdateUserDto;
import com.spmorangle.crm.usermanagement.dto.UserResponseDto;

import java.util.List;
import java.util.UUID;

public interface UserManagementService {
    void createUser(CreateUserDto createStaffDto);
    UserResponseDto getUserById(Long staffId);
    UserResponseDto getUserByCognitoSub(UUID cognitoSub);
    void updateUser(UpdateUserDto updateStaffDto);
    void deleteUser(Long staffId);
    void toggleUserStatus(Long staffId, boolean isActive);
    boolean isUserExistsByEmail(String email);
    List<String> getUserTypes();
}
