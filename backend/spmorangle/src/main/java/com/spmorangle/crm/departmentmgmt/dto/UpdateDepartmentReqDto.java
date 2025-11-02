package com.spmorangle.crm.departmentmgmt.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateDepartmentReqDto {
    @NotNull
    Long id;

    @NotEmpty
    String newName;
}
