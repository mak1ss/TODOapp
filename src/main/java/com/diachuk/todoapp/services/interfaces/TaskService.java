package com.diachuk.todoapp.services.interfaces;

import com.diachuk.todoapp.dto.TaskDTO;
import com.diachuk.todoapp.entities.Task;


import java.util.Set;

public interface TaskService {

    Set<TaskDTO> getTaskByName(String name, int userId);

    Set<TaskDTO> getUserTasks(int id);

    TaskDTO addNewTask(Task task, int userId);

    TaskDTO updateTask(Task task, int userId);

    String delete(int taskId, int userId) throws IllegalAccessException;

    TaskDTO getById(int taskId, int userId);


}
