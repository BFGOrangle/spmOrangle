package com.spmorangle.crm.taskmanagement.service.exception;

public class MaxAssigneesExceededException extends RuntimeException {
    private static final int MAX_ASSIGNEES = 5;

    public MaxAssigneesExceededException(Long taskId, int currentCount) {
        super(String.format(
            "Task %d already has %d assignees. Maximum allowed is %d assignees per task.",
            taskId, currentCount, MAX_ASSIGNEES
        ));
    }

    public MaxAssigneesExceededException(int requestedCount) {
        super(String.format(
            "Cannot assign %d users to a task. Maximum allowed is %d assignees per task.",
            requestedCount, MAX_ASSIGNEES
        ));
    }

    public static int getMaxAssignees() {
        return MAX_ASSIGNEES;
    }
}
