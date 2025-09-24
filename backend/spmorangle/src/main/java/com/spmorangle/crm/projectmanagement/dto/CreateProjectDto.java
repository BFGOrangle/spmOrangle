package com.spmorangle.crm.projectmanagement.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CreateProjectDto {
    
    @NotNull(message = "Project name is required")
    private final String name;
    
    private final String description;
}
