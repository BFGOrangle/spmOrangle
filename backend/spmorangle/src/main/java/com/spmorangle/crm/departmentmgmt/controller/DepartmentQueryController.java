package com.spmorangle.crm.departmentmgmt.controller;

import com.spmorangle.crm.departmentmgmt.dto.DepartmentDto;
import com.spmorangle.crm.departmentmgmt.service.DepartmentQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/department-query")
@RequiredArgsConstructor
public class DepartmentQueryController {

    private final DepartmentQueryService departmentQueryService;

    @GetMapping("/{id}/sub-departments")
    public ResponseEntity<List<DepartmentDto>> getSubDepartments(@PathVariable Long id) {
        List<DepartmentDto> subDepartments = departmentQueryService.getChildren(id);
        return ResponseEntity.ok(subDepartments);
    }
}
