package com.spmorangle.crm.usermanagement.controller;

import com.spmorangle.common.service.UserContextService;
import com.spmorangle.crm.usermanagement.dto.CreateUserDto;
import com.spmorangle.crm.usermanagement.dto.UpdateUserDto;
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

    @PostMapping
    public ResponseEntity<Void> createUser(@Valid @RequestBody CreateUserDto createUserDto) {
        log.info("Creating staff member with email: {}", createUserDto.email());
        userManagementService.createUser(createUserDto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user-id/{userId}")
    public ResponseEntity<UserResponseDto> getStaffByStaffId(@PathVariable Long userId) {
        UserResponseDto user = userManagementService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/sub/{cognitoSub}")
    public ResponseEntity<UserResponseDto> getStaffByStaffCognitoSub(@PathVariable String cognitoSub) {
        UserResponseDto user = userManagementService.getUserByCognitoSub(UUID.fromString(cognitoSub));
        return ResponseEntity.ok(user);
    }

    @PutMapping
    public ResponseEntity<Void> updateUser(@Valid @RequestBody UpdateUserDto updateUserDto) {
        if (!userContextService.isRequestingUserSelfCheckByUserId(updateUserDto.id())) {
            log.warn("Unauthorized update attempt by user ID: {}", userContextService.getRequestingUser().getId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        log.info("Updating staff member with ID: {}", updateUserDto.id());
        userManagementService.updateUser(updateUserDto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        if (!userContextService.isRequestingUserSelfCheckByUserId(userId)) {
            log.warn("Unauthorized update attempt by user ID: {}", userContextService.getRequestingUser().getId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        log.info("Deleting staff member with ID: {}", userId);
        userManagementService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user-types")
    public ResponseEntity<List<String>> getUserTypes() {
        List<String> userTypes = userManagementService.getUserTypes();
        return ResponseEntity.ok(userTypes);
    }
}
