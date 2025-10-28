package com.spmorangle.crm.notification.service;

import com.spmorangle.crm.taskmanagement.model.Task;
import com.spmorangle.crm.taskmanagement.model.TaskAssignee;
import java.util.List;

public interface OverdueTaskEmailService {
    void sendOverdueTaskEmail(Task task, TaskAssignee assignee);
    void sendMultipleOverdueTasksEmail(List<Task> tasks, TaskAssignee assignee);
}