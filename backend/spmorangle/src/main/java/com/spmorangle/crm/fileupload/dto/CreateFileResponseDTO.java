package com.spmorangle.crm.fileupload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
@AllArgsConstructor
public class CreateFileResponseDTO {
    private final Long id;
    private final Long taskId;
    private final Long projectId;
    private final String fileUrl;
    private final OffsetDateTime createdAt;
    private final Long createdBy;

}
