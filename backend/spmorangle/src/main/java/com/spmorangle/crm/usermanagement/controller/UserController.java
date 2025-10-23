package com.spmorangle.crm.usermanagement.controller;

import com.spmorangle.common.service.UserContextService;
import com.spmorangle.crm.usermanagement.dto.CreateUserDto;
import com.spmorangle.crm.usermanagement.dto.UpdateUserRoleDto;
import com.spmorangle.crm.usermanagement.dto.UserResponseDto;
import com.spmorangle.crm.usermanagement.service.UserManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/user")
@PreAuthorize("hasAnyRole('MANAGER', 'STAFF', 'HR', 'DIRECTOR')")
@RequiredArgsConstructor
public class UserController {

    private final UserManagementService userManagementService;
    private final UserContextService userContextService;

    @PreAuthorize("permitAll()")
    @PostMapping("/create")
    public ResponseEntity<Void> createUser(@Valid @RequestBody CreateUserDto createUserDto) {
        log.info("Creating user with email: {}", createUserDto.email());
        userManagementService.createUser(createUserDto, "STAFF", false);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('HR')")
    @PostMapping("admin-create")
    public ResponseEntity<Void> adminCreateUser(@Valid @RequestBody CreateUserDto createUserDto) {
        log.info("Admin creating user with email: {}", createUserDto.email());
        userManagementService.createUser(createUserDto, true);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('HR')")
    @PostMapping("deactivate/{userId}")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long userId) {
        log.info("Deactivating user with ID: {}", userId);
        userManagementService.toggleUserStatus(userId, false);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('HR')")
    @PostMapping("reactivate/{userId}")
    public ResponseEntity<Void> reactivateUser(@PathVariable Long userId) {
        log.info("Reactivating user with ID: {}", userId);
        userManagementService.toggleUserStatus(userId, true);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user-id/{userId}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long userId) {
        UserResponseDto user = userManagementService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/sub/{cognitoSub}")
    public ResponseEntity<UserResponseDto> getUserByCognitoSub(@PathVariable String cognitoSub) {
        UserResponseDto user = userManagementService.getUserByCognitoSub(UUID.fromString(cognitoSub));
        return ResponseEntity.ok(user);
    }

    @PreAuthorize("hasRole('HR')")
    @PutMapping("/role")
    public ResponseEntity<Void> updateUserRole(@Valid @RequestBody UpdateUserRoleDto updateUserDto) {
        log.info("Updating user with ID: {}", updateUserDto.userId());
        userManagementService.updateUserRole(updateUserDto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        if (!userContextService.isRequestingUserSelfCheckByUserId(userId)) {
            log.warn("Unauthorized update attempt by user ID: {}", userContextService.getRequestingUser().getId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        log.info("Deleting user with ID: {}", userId);
        userManagementService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user-types")
    public ResponseEntity<List<String>> getUserTypes() {
        List<String> userTypes = userManagementService.getUserTypes();
        return ResponseEntity.ok(userTypes);
    }

    @GetMapping("/project/{projectId}/members")
    public ResponseEntity<List<UserResponseDto>> getProjectMembers(@PathVariable Long projectId) {
        log.info("Getting project members for project: {}", projectId);
        List<UserResponseDto> members = userManagementService.getProjectMembers(projectId);
        return ResponseEntity.ok(members);
    }

    @PostMapping("/batch")
    public ResponseEntity<List<UserResponseDto>> getUsersByIds(@RequestBody List<Long> userIds) {
        log.info("Getting users by IDs: {}", userIds);
        List<UserResponseDto> users = userManagementService.getUsersByIds(userIds);
        return ResponseEntity.ok(users);
    }

    @PreAuthorize("hasRole('HR')")
    @GetMapping("/")
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        List<UserResponseDto> users = userManagementService.getAllUsers();
        return ResponseEntity.ok(users);
    }
}
