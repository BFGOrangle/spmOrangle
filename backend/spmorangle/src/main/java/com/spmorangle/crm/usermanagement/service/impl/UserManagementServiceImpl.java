package com.spmorangle.crm.usermanagement.service.impl;

import com.spmorangle.common.converter.UserConverter;
import com.spmorangle.common.enums.UserType;
import com.spmorangle.common.model.User;
import com.spmorangle.common.repository.UserRepository;
import com.spmorangle.crm.departmentmgmt.service.DepartmentQueryService;
import com.spmorangle.crm.taskmanagement.model.TaskAssignee;
import com.spmorangle.crm.usermanagement.dto.CreateUserDto;
import com.spmorangle.crm.usermanagement.dto.UpdateUserRoleDto;
import com.spmorangle.crm.usermanagement.dto.UserResponseDto;
import com.spmorangle.crm.usermanagement.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserManagementServiceImpl implements UserManagementService {

    private final UserRepository userRepository;
    private final CognitoServiceImpl cognitoService;
    private final DepartmentQueryService departmentQueryService;

    @Override
    public void createUser(CreateUserDto createStaffDto, String roleType, boolean isSetAsTemporaryPassword) {
        String cognitoUsername = (createStaffDto.userName().toLowerCase())
                .replaceAll("\\s+", "_")
                .replaceAll("[^\\p{L}\\p{M}\\p{S}\\p{N}\\p{P}]", "");


        var cognitoResponse = cognitoService.createUser(
                cognitoUsername,
                createStaffDto.email(),
                createStaffDto.password(),
                isSetAsTemporaryPassword
        );

        if (cognitoResponse.isEmpty()) {
            log.error("Cognito user creation returned empty response for email: {}", createStaffDto.email());
            throw new RuntimeException("Failed to create user in Cognito - empty response. Check configuration.");
        }

        String cognitoSubString = cognitoResponse.get().user().attributes().stream()
                .filter(attr -> "sub".equals(attr.name()))
                .findFirst()
                .map(AttributeType::value)
                .orElseThrow(() -> new RuntimeException("Cognito user created but sub attribute not found"));

        UUID cognitoSub = UUID.fromString(cognitoSubString);

        log.info("Adding user to Cognito group: {}", roleType);
        boolean groupAdded = cognitoService.addUserToGroup(cognitoUsername, roleType);
        if (!groupAdded) {
            log.warn("Failed to add user to Cognito group: {}", roleType);
        }

        // Create user record in database
        User user = new User();
        user.setCognitoSub(cognitoSub); // Store Cognito sub
        user.setUserName(createStaffDto.userName());
        user.setEmail(createStaffDto.email());
        user.setRoleType(createStaffDto.roleType());

        userRepository.save(user);
    }

    @Override
    public void createUser(CreateUserDto createStaffDto, boolean isSetAsTemporaryPassword) {
        createUser(createStaffDto, createStaffDto.roleType(), isSetAsTemporaryPassword);
    }

    @Override
    public UserResponseDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElse(null);
        return UserConverter.convert(user);

    }

    @Override
    public UserResponseDto getUserByCognitoSub(UUID cognitoSub) {
        User user = userRepository.findByCognitoSub(cognitoSub)
                .orElse(null);
        return UserConverter.convert(user);
    }

    @Transactional
    @Override
    public void updateUserRole(UpdateUserRoleDto updateUserDto) {
        boolean groupRemoved = cognitoService.removeUserFromGroup(updateUserDto.email(), updateUserDto.roleType());
        if (!groupRemoved) {
            log.warn("Failed to remove user from Cognito group: {}", updateUserDto.roleType());
            throw new RuntimeException("Failed to update user role - Cognito group removal failed");
        }

        // Add user to new Cognito group
        boolean groupAdded = cognitoService.addUserToGroup(updateUserDto.email(), updateUserDto.roleType());
        if (!groupAdded) {
            log.warn("Failed to add user to Cognito group: {}", updateUserDto.roleType());
            // Try to revert the removal
            boolean isReverted = cognitoService.addUserToGroup(updateUserDto.email(), updateUserDto.roleType());
            if (!isReverted) {
                log.error("Failed to revert Cognito group addition for user: {}", updateUserDto.email());
            }
            throw new RuntimeException("Failed to update user role - Cognito group addition failed");
        }
        log.info("Updated user role type from {} to {}", updateUserDto.roleType(), updateUserDto.roleType());

        userRepository.updateUserTypeById(updateUserDto.userId(), updateUserDto.roleType());
    }

    @Transactional
    @Override
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Staff member not found with ID: " + userId));
        cognitoService.disableUser(user.getEmail());
        cognitoService.deleteUser(user.getEmail());
        userRepository.delete(user);
    }

    @Transactional
    @Override
    public void toggleUserStatus(Long userId, boolean isActive) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Staff member not found with ID: " + userId));

        // Toggle status in Cognito
        boolean isToggleSuccess;
        if (!isActive) {
            log.info("Deactivating staff member in Cognito: {}", user.getEmail());
            isToggleSuccess = cognitoService.disableUser(user.getEmail());
        } else {
            log.info("Activating staff member in Cognito: {}", user.getEmail());
            isToggleSuccess = cognitoService.enableUser(user.getEmail());
        }

        if (!isToggleSuccess) {
            log.error("Failed to toggle staff status in Cognito for email: {}", user.getEmail());
            throw new RuntimeException("Failed to toggle staff status in Cognito");
        }

        if (!isActive) {
            userRepository.updateUserIsActiveById(userId, false);
        } else {
            userRepository.updateUserIsActiveById(userId, true);
        }
        log.info("Successfully toggled staff status for ID: {}", userId);
    }

    public boolean isUserExistsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public List<String> getUserTypes() {
        return Arrays.stream(UserType.values())
                .map(UserType::getCode)
                .toList();
    }

    @Override
    public List<UserResponseDto> getProjectMembers(Long projectId) {
        log.info("Getting project members for project: {}", projectId);
        List<User> users = userRepository.findProjectMembers(projectId);
        return users.stream()
                .map(UserConverter::convert)
                .toList();
    }

    @Override
    public List<UserResponseDto> getUsersByIds(List<Long> userIds) {
        log.info("Getting users by IDs: {}", userIds);
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<User> users = userRepository.findByIdIn(userIds);
        return users.stream()
                .map(UserConverter::convert)
                .toList();
    }

    @Override
    public List<UserResponseDto> getAllUsers(){
        log.info("Getting users to display collaborators");
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(UserConverter::convert)
                .toList();
    }

    @Override
    public String getAssigneeEmail(TaskAssignee assignee) {
        long assigneeId = assignee.getUserId();
        var assigneeProfile = getUserById(assigneeId);
        return assigneeProfile.email();
    }

    @Override
    public String getAssigneeName(TaskAssignee assignee) {
        long assigneeId = assignee.getUserId();
        var assigneeProfile = getUserById(assigneeId);
        return assigneeProfile.username();
    }

    @Override
    public List<UserResponseDto> getUsersByDepartmentId(Long departmentId) {
        log.info("Getting users by Department ID: {}", departmentId);
        List<User> users = userRepository.findByDepartmentId(departmentId);
        return users.stream()
                .map(UserConverter::convert)
                .toList();
    }



    @Override
    public List<UserResponseDto> getAssignableManagers(Long currentUserId) {
        log.info("Getting all active managers for cross-department assignment (excluding user {})", currentUserId);
        List<User> managers = userRepository.findAllActiveManagers();
        log.info("Found {} active managers before filtering", managers.size());

        List<UserResponseDto> filteredManagers = managers.stream()
                .filter(user -> !user.getId().equals(currentUserId))
                .map(user -> UserConverter.convert(user, departmentQueryService))
                .toList();

        log.info("Returning {} managers after excluding current user", filteredManagers.size());
        return filteredManagers;
    }
}
