package com.spmorangle.crm.taskmanagement.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
public class TaskAssigneeCK implements Serializable {
    private long taskId;
    private long userId;
    private long assignedId;

    public TaskAssigneeCK(long taskId, long userId, long assignedId){
        this.taskId = taskId;
        this.userId = userId;
        this.assignedId = assignedId;
    }

    @Override
    public boolean equals(Object o){
        if (this == o){
            return true;
        }
        if (o == null || getClass() != o.getClass()){
            return false;
        }
        TaskAssigneeCK that = (TaskAssigneeCK) o;
        return Objects.equals(assignedId, that.assignedId) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(taskId, that.taskId);
    }

    @Override
    public int hashCode(){
        return Objects.hash(taskId, userId, assignedId);
    }
}
