package com.spmorangle.crm.departmentmgmt.service;

import com.spmorangle.crm.departmentmgmt.dto.CreateDepartmentReqDto;
import com.spmorangle.crm.departmentmgmt.dto.DepartmentDto;
import com.spmorangle.crm.departmentmgmt.dto.MoveDepartmentReqDto;
import com.spmorangle.crm.departmentmgmt.dto.UpdateDepartmentReqDto;

public interface DepartmentAdminService {

    DepartmentDto createDepartment(CreateDepartmentReqDto req);

    void updateDepartment(UpdateDepartmentReqDto req);

    void moveDepartment(MoveDepartmentReqDto req);

    void deleteDepartment(Long id);
}