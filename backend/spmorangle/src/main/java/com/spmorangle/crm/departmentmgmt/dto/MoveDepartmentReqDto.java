package com.spmorangle.crm.departmentmgmt.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MoveDepartmentReqDto {
    @NotNull
    Long departmentId;

    @NotNull
    Long newParentId;
}