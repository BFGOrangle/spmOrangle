package com.spmorangle.crm.departmentmgmt.service.impl;

import com.spmorangle.common.model.User;
import com.spmorangle.common.repository.UserRepository;
import com.spmorangle.crm.departmentmgmt.service.DepartmentQueryService;
import com.spmorangle.crm.departmentmgmt.service.DepartmentalVisibilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DepartmentalVisibilityServiceImpl implements DepartmentalVisibilityService {

    private final DepartmentQueryService departmentQueryService;
    private final UserRepository userRepository;

    @Override
    public Set<Long> visibleDepartmentsForAssignedDept(Long deptId) {
        Set<Long> visibleDeptIds = new HashSet<>();
        visibleDeptIds.add(deptId);

        Long currentDeptId = deptId;
        while(departmentQueryService.getParentDepartment(currentDeptId, false) != null) {
            Long parentDept = departmentQueryService.getParentDepartment(currentDeptId, false).getId();
            visibleDeptIds.add(parentDept);

            currentDeptId = parentDept;
        }
        return visibleDeptIds;
    }

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

}
