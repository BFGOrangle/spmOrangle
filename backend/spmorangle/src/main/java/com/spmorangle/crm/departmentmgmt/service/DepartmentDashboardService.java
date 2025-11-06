package com.spmorangle.crm.departmentmgmt.service;

import com.spmorangle.common.model.User;
import com.spmorangle.crm.departmentmgmt.dto.DepartmentDashboardResponseDto;

public interface DepartmentDashboardService {

    DepartmentDashboardResponseDto getDepartmentDashboard(User user);
}
