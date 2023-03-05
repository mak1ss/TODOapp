package com.diachuk.todoapp.util;

import com.diachuk.todoapp.dto.BusinessUserDTO;
import com.diachuk.todoapp.dto.TaskDTO;
import com.diachuk.todoapp.dto.UserDTO;
import com.diachuk.todoapp.entities.BusinessUser;
import com.diachuk.todoapp.entities.Task;
import com.diachuk.todoapp.entities.User;
import com.diachuk.todoapp.entities.security.UserRole;
import com.diachuk.todoapp.services.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class Converter {

    private UserRepository repository;

    public UserDTO userToDto(User user) {
        int id = user.getId();
        String name = user.getName();
        String pass = user.getPassword();
        String status = user.getStatus();
        List<TaskDTO> tasks = user.getTasks().stream().map(this::taskToDto).collect(Collectors.toList());
        List<String> roles = user.getRoles().stream().map(userRole -> userRole.getRole()).collect(Collectors.toList());

        if (repository.checkIfBusiness(user.getId()).contains("BusinessUser")) {
            BusinessUser bu = (BusinessUser) user;
            BusinessUserDTO buDto = new BusinessUserDTO();
            buDto.setFriends(bu.getFriends().stream().map(this::businessUserToDto).collect(Collectors.toSet()));
            buDto.setName(name);
            buDto.setId(id);
            buDto.setStatus(status);
            buDto.setPassword(pass);
            buDto.setTasks(tasks);
            buDto.setRole(roles);
            return buDto;
        }
        return new UserDTO(id, name, pass, status, roles, tasks);
    }

    public TaskDTO taskToDto(Task task) {
        return new TaskDTO(task.getId(), task.getTaskName(), task.getTask(), task.getCreationDate(), task.getExpirationDate(), task.getUser());
    }

    private BusinessUserDTO businessUserToDto(BusinessUser businessUser) {
        BusinessUserDTO dto = new BusinessUserDTO();
        dto.setId(businessUser.getId());
        dto.setName(businessUser.getName());
        dto.setStatus(businessUser.getStatus());
        return dto;
    }

    public User dtoToUser(UserDTO dto) {
        User user = new User();
        user.setId(dto.getId());
        user.setName(dto.getName());
        user.setPassword(dto.getPassword());
        user.setStatus(dto.getStatus());
        user.setTasks(dto.getTasks().stream().map(this::dtoToTask).collect(Collectors.toList()));
        return user;
    }

    public Task dtoToTask(TaskDTO dto) {
        Task task = new Task();
        task.setId(dto.getId());
        task.setTaskName(dto.getTaskName());
        task.setTask(dto.getTask());
        User user = new User();
        user.setId(dto.getId());
        task.setUser(user);
        task.setCreationDate(dto.getCreationDate());
        task.setExpirationDate(dto.getExpirationDate());
        return task;
    }


    public Converter(@Autowired UserRepository repository) {
        this.repository = repository;
    }
}
