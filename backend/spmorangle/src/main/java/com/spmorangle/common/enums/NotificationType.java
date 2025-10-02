package com.spmorangle.common.enums;

public enum NotificationType {
    // Comment events
    MENTION,
    COMMENT_REPLY,
    
    // Task events  
    TASK_ASSIGNED,
    TASK_COMPLETED,
    TASK_DEADLINE_APPROACHING,
    
    // Project events
    PROJECT_INVITE,
    PROJECT_MEMBER_JOINED,
    PROJECT_DEADLINE_APPROACHING,
    
    // User events
    USER_REGISTERED,
    PASSWORD_RESET_REQUESTED,
    
    // System events
    SYSTEM_MAINTENANCE,
    SECURITY_ALERT
}

