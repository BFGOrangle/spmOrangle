package com.spmorangle.crm.taskmanagement.dto;

public record TagDto(
        long id,
        String tagName,
        boolean deleteInd
) {
}
