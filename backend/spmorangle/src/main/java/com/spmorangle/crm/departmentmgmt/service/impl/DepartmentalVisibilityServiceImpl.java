package com.spmorangle.crm.departmentmgmt.service.impl;

import com.spmorangle.crm.departmentmgmt.service.DepartmentalVisibilityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;

/**
 * STUB IMPLEMENTATION - Temporary placeholder for DepartmentalVisibilityService.
 * This allows the application to compile and run while the real implementation is being developed.
 *
 * TODO: Replace this stub with the actual implementation that:
 * - Determines which departments are visible based on hierarchy
 * - Checks ABAC rules (user_managed_departments, user_subordinate_departments)
 * - Implements proper visibility logic for tasks/projects
 */
@Slf4j
@Service
public class DepartmentalVisibilityServiceImpl implements DepartmentalVisibilityService {

    @Override
    public Set<Long> visibilityDepartmentsForAssignedDept(Long leafDept) {
        log.warn("STUB: DepartmentalVisibilityService.visibilityDepartmentsForAssignedDept({}) - returning empty set. Replace with real implementation.", leafDept);
        log.warn("STUB: No department filtering will be applied - all tasks/projects visible to members");
        return Collections.emptySet();
    }

    @Override
    public boolean canUserSeeTask(Set<Long> userDepartments, Long leafDept) {
        log.warn("STUB: DepartmentalVisibilityService.canUserSeeTask(userDepts={}, leafDept={}) - returning true (no filtering). Replace with real implementation.", userDepartments, leafDept);
        // Return true to allow all tasks to be visible (safe fallback - maintains current behavior)
        return true;
    }

    @Override
    public Set<Long> getChildDepartmentIds(Long leafDept) {
        log.warn("STUB: DepartmentalVisibilityService.getChildDepartmentIds({}) - returning empty set. Replace with real implementation.", leafDept);
        return Collections.emptySet();
    }
}
