package com.spmorangle.crm.fileupload.dto;

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
public class DeleteFileResponseDTO {
    
    private Long id;
    private String message;
    private boolean success;
}
