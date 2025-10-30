package com.spmorangle.crm.departmentmgmt.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateDepartmentReqDto {
    String name;
    Long parentId;
}
