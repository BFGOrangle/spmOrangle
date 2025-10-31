package com.spmorangle.crm.notification.service;

import com.spmorangle.crm.taskmanagement.model.Task;
import com.spmorangle.crm.taskmanagement.model.TaskAssignee;

import java.util.List;

public interface PreDueTaskEmailService {
    void sendPreDueTaskEmail(Task task, TaskAssignee assignee, int hoursUntilDue);
    void sendMultiplePreDueTasksEmail(List<Task> tasks, TaskAssignee assignee);
}