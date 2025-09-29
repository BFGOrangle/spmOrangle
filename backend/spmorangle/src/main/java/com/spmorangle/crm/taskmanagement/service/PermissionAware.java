package com.spmorangle.crm.taskmanagement.service;

public interface PermissionAware {
    boolean canRead(Long userId, Long resourceId);
    boolean canWrite(Long userId, Long resourceId);
    boolean canModerate(Long userId, Long resourceId);
}