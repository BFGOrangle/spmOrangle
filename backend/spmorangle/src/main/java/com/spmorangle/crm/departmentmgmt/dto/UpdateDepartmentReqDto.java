package com.spmorangle.crm.departmentmgmt.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateDepartmentReqDto {
    Long id;
    String newName;
}
