package com.spmorangle.crm.fileupload.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class CreateFileDTO {

    @NotNull
    private final Long id;

    @NotNull(message = "Task ID is required")
    private final Long taskId;

    @NotNull(message = "Project ID is required")
    private final Long projectId;

    @NotNull
    private final String fileUrl;

    @NotNull
    private final Long createdBy;
}
