package com.spmorangle.crm.departmentmgmt.service.impl;

import com.spmorangle.crm.departmentmgmt.DepartmentConverter;
import com.spmorangle.crm.departmentmgmt.dto.CreateDepartmentReqDto;
import com.spmorangle.crm.departmentmgmt.dto.DepartmentDto;
import com.spmorangle.crm.departmentmgmt.dto.MoveDepartmentReqDto;
import com.spmorangle.crm.departmentmgmt.dto.UpdateDepartmentReqDto;
import com.spmorangle.crm.departmentmgmt.model.Department;
import com.spmorangle.crm.departmentmgmt.repository.DepartmentRepository;
import com.spmorangle.crm.departmentmgmt.service.DepartmentAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DepartmentAdminServiceImpl implements DepartmentAdminService {

    private final DepartmentRepository departmentRepository;

    public DepartmentDto getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Department not found with id: " + id));
        return DepartmentConverter.convert(department);
    }

    public void createDepartment(CreateDepartmentReqDto req) {
        Department department = new Department();
        department.setName(req.getName());
        department.setParentId(req.getParentId());
        departmentRepository.save(department);
    }

    public void updateDepartment(UpdateDepartmentReqDto req) {
        Department originalDepartment = departmentRepository.findById(req.getId())
            .orElseThrow(() -> new IllegalArgumentException("Department not found with id: " + req.getId()));
        originalDepartment.setName(req.getNewName());
    }

    public void moveDepartment(MoveDepartmentReqDto req) {
        Department department = departmentRepository.findById(req.getDepartmentId())
            .orElseThrow(() -> new IllegalArgumentException("Department not found with id: " + req.getDepartmentId()));
        department.setParentId(req.getNewParentId());
    }

    public void deleteDepartment(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new IllegalArgumentException("Department not found with id: " + id);
        }
        departmentRepository.deleteById(id);
    }
}
