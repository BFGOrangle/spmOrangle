package com.spmorangle.crm.departmentmgmt.controller;

import com.spmorangle.crm.departmentmgmt.dto.CreateDepartmentReqDto;
import com.spmorangle.crm.departmentmgmt.dto.MoveDepartmentReqDto;
import com.spmorangle.crm.departmentmgmt.dto.UpdateDepartmentReqDto;
import com.spmorangle.crm.departmentmgmt.service.DepartmentAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/department-mgmt")
@PreAuthorize("hasRole('HR')")
@RequiredArgsConstructor
public class DepartmentMgmtController {

    private final DepartmentAdminService departmentAdminService;

    @PostMapping("/")
    public ResponseEntity<Void> createDepartment(@RequestBody @Valid CreateDepartmentReqDto req) {
        departmentAdminService.createDepartment(req);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/")
    public ResponseEntity<Void> updateDepartment(@RequestBody @Valid UpdateDepartmentReqDto req) {
        departmentAdminService.updateDepartment(req);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/move")
    public ResponseEntity<Void> moveDepartment(@RequestBody @Valid MoveDepartmentReqDto req) {
        departmentAdminService.moveDepartment(req);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        departmentAdminService.deleteDepartment(id);
        return ResponseEntity.ok().build();
    }

}
