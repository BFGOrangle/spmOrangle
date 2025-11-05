package com.spmorangle.crm.departmentmgmt.controller;

import com.spmorangle.common.model.User;
import com.spmorangle.common.service.UserContextService;
import com.spmorangle.crm.departmentmgmt.dto.DepartmentDashboardResponseDto;
import com.spmorangle.crm.departmentmgmt.service.DepartmentDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/dashboard/department")
@RequiredArgsConstructor
public class DepartmentDashboardController {

    private final DepartmentDashboardService departmentDashboardService;
    private final UserContextService userContextService;

    @GetMapping
    public ResponseEntity<DepartmentDashboardResponseDto> getDepartmentDashboard() {
        User user = userContextService.getRequestingUser();
        log.info("Fetching department dashboard for user {}", user.getId());
        DepartmentDashboardResponseDto dashboard = departmentDashboardService.getDepartmentDashboard(user);
        return ResponseEntity.ok(dashboard);
    }
}
