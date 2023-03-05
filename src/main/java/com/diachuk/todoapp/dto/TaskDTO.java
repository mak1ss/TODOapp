package com.diachuk.todoapp.dto;


import com.diachuk.todoapp.entities.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
public class TaskDTO {
    private int id;
    private String taskName;
    private String task;

    private Date creationDate;
    private Date expirationDate;
    private int user;

    public TaskDTO (){

    }
    public void setUser(UserDTO user, boolean isTaskSet){
        if(!isTaskSet){
            this.user = user.getId();
            user.addTask(this, true);
        } else {
            this.user = user.getId();
        }
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskDTO taskDTO = (TaskDTO) o;
        return id == taskDTO.id && taskName.equals(taskDTO.taskName) && task.equals(taskDTO.task) && user == (taskDTO.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, taskName, task);
    }

    @Override
    public String toString() {
        return "TaskDTO{" +
                "id=" + id +
                ", taskName='" + taskName + '\'' +
                ", task='" + task + '\'' +
                ", user=" + user +
                '}';
    }
}
