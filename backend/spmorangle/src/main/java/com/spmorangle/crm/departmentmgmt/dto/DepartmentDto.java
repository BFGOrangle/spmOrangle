package com.spmorangle.crm.departmentmgmt.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DepartmentDto {
    Long id;
    String name;
    Long parentId;
}