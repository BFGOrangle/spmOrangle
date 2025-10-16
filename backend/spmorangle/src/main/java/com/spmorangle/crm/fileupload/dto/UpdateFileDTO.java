package com.spmorangle.crm.fileupload.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateFileDTO {

    @NotNull(message = "File ID is required")
    private Long id;

    @NotNull(message = "Task ID is required")
    private Long taskId;

    @NotNull(message = "Project ID is required")
    private Long projectId;

    private String fileUrl;

    @NotNull(message = "Updated by user ID is required")
    private Long updatedBy;
}
