package com.diachuk.todoapp.dto;

import com.diachuk.todoapp.entities.Task;
import com.diachuk.todoapp.entities.security.UserRole;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private int id;
    private String name;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    private String status;

    @JsonIgnore
    private List<String> role;

    private List<TaskDTO> tasks = new ArrayList<>();

    public void addTask(TaskDTO task, boolean isUserSetted) {
        if(isUserSetted){
            tasks.add(task);
        } else {
            task.setUser(this, false);
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDTO userDTO = (UserDTO) o;
        return id == userDTO.id && name.equals(userDTO.name) && password.equals(userDTO.password) && Objects.equals(status, userDTO.status) && tasks.equals(userDTO.tasks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, password, status, tasks.size());
    }

    @Override
    public String toString() {
        return "UserDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", password='" + password + '\'' +
                ", status='" + status + '\'' +
                ", tasks=" + tasks +
                '}';
    }
}
