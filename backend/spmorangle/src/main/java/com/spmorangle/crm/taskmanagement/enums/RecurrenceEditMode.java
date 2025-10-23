package com.spmorangle.crm.taskmanagement.enums;

public enum RecurrenceEditMode {
    /**
     * Apply changes only to this specific instance
     * This will add an EXDATE to the RRULE and create a new standalone task
     */
    THIS_INSTANCE,

    /**
     * Apply changes to all future instances (including this one)
     * This will update the current task directly
     */
    ALL_FUTURE_INSTANCES,

    /**
     * Apply changes to this instance and all future instances
     * This will cap the original RRULE with UNTIL and create a new recurring task
     */
    THIS_AND_FUTURE_INSTANCES
}
