package com.spmorangle.crm.departmentmgmt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateDepartmentReqDto {
    @NotBlank
    String name;

    @NotNull
    Long parentId;
}
