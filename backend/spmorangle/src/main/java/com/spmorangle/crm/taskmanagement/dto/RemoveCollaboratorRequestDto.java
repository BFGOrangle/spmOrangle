package com.spmorangle.crm.taskmanagement.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RemoveCollaboratorRequestDto {

    @NotNull
    @Min(1)
    private Long taskId;

    @NotNull
    @Min(1)
    private Long collaboratorId;
}
