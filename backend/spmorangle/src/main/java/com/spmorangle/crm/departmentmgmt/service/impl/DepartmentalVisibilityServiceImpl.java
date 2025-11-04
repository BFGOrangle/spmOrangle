package com.spmorangle.crm.departmentmgmt.service.impl;

import com.spmorangle.common.model.User;
import com.spmorangle.common.repository.UserRepository;
import com.spmorangle.crm.departmentmgmt.dto.DepartmentDto;
import com.spmorangle.crm.departmentmgmt.service.DepartmentQueryService;
import com.spmorangle.crm.departmentmgmt.service.DepartmentalVisibilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentalVisibilityServiceImpl implements DepartmentalVisibilityService {

    private final DepartmentQueryService departmentQueryService;
    private final UserRepository userRepository;

    // @Override
    // public Set<Long> visibleDepartmentsForAssignedDept(Long deptId) {
    //     Set<Long> visibleDeptIds = new HashSet<>();
    //     visibleDeptIds.add(deptId);

    //     Long currentDeptId = deptId;
    //     while(true) {
    //         // try {
    //         Long parentDept = departmentQueryService.getParentDepartment(currentDeptId, false).map(DepartmentDto::getId).orElse(null);

    //         // If parentDept is null, we've reached the root department - stop traversing
    //         if (parentDept == null) {
    //             break;
    //         }

    //         visibleDeptIds.add(parentDept);
    //         currentDeptId = parentDept;
    //     //     } catch (IllegalArgumentException e) {
    //     //         // Reached root department (no parent), stop traversing
    //     //         break;
    //     //     }
    //     }
    //     return visibleDeptIds;
    // }

        @Override
    public Set<Long> visibleDepartmentsForAssignedDept(Long deptId) {
        log.info("Calculating visible departments for deptId: {}", deptId);

        Set<Long> visibleDeptIds = new HashSet<>();
        visibleDeptIds.add(deptId);

        // DOWNWARD TRAVERSAL - Get all child departments recursively
        // This allows managers to see tasks from their subordinates' departments
        addAllChildDepartments(deptId, visibleDeptIds);

        log.info("Department {} can see {} departments: {}", deptId, visibleDeptIds.size(), visibleDeptIds);
        return visibleDeptIds;
    }

    /**
     * Recursively add all child departments (downward traversal)
     * This allows managers to see tasks from their subordinates' departments
     *
     * @param parentDeptId The parent department to get children for
     * @param visibleDeptIds The set to add child department IDs to
     */
    private void addAllChildDepartments(Long parentDeptId, Set<Long> visibleDeptIds) {
        // Get immediate children of this department
        List<DepartmentDto> children = departmentQueryService.getSubDepartments(parentDeptId);

        if (!children.isEmpty()) {
            log.debug("Department {} has {} child departments: {}",
                     parentDeptId, children.size(), children.stream().map(DepartmentDto::getId).toList());
        }

        for (DepartmentDto child : children) {
            // Avoid infinite loops by checking if already added
            if (!visibleDeptIds.contains(child.getId())) {
                visibleDeptIds.add(child.getId());
                log.debug("Added child department {} to visible departments", child.getId());

                // Recursively add children of this child (grandchildren, etc.)
                addAllChildDepartments(child.getId(), visibleDeptIds);
            }
        }
    }

    // Claude says might have bug here as deptId will always be added to the result when you call visibleDepartmentsForAssignedDept
    // Suggested change is to have 2 input. Created a suggested method below this method
    @Override
    public boolean canUserSeeTask(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        Long deptId = user.getDepartmentId();
        if (deptId == null) {
            throw new IllegalArgumentException("User with id: " + userId + " has no assigned department.");
        }
        Set<Long> visibleDepts = visibleDepartmentsForAssignedDept(deptId);
        return visibleDepts.contains(deptId);
    }

    @Override
    public boolean canUserSeeTask(Set<Long> viewingUserVisibleDeptIds, Long taskAssigneeId) {
        // If user doesn't exist, they can't see the task (return false instead of throwing exception)
        User assignee = userRepository.findById(taskAssigneeId).orElse(null);
        if (assignee == null || assignee.getDepartmentId() == null) {
            return false;
        }

        return viewingUserVisibleDeptIds.contains(assignee.getDepartmentId());
    }



}
