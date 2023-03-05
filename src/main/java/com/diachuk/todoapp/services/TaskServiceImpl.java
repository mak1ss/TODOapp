package com.diachuk.todoapp.services;

import com.diachuk.todoapp.dto.TaskDTO;
import com.diachuk.todoapp.entities.Task;
import com.diachuk.todoapp.entities.User;

import com.diachuk.todoapp.services.interfaces.TaskService;
import com.diachuk.todoapp.services.repositories.TaskRepository;
import com.diachuk.todoapp.services.repositories.UserRepository;
import com.diachuk.todoapp.util.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TaskServiceImpl implements TaskService {

    private TaskRepository taskRepository;
    private UserRepository userRepository;

    private Converter converter;

    public TaskServiceImpl(@Autowired TaskRepository taskRepository, @Autowired UserRepository userRepository, @Autowired Converter converter) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.converter = converter;
    }


    @Override
    public Set<TaskDTO> getTaskByName(String name, int userId) {
        if (userRepository.findById(userId).isPresent()) {
            Optional<Set<Task>> resultSet = taskRepository.findTaskByTaskNameContainingIgnoreCaseAndUserId(name, userId);
            if (resultSet.isPresent() && resultSet.get().size() != 0) {
                return resultSet.get().stream().map(task -> converter.taskToDto(task)).collect(Collectors.toSet());
            } else {
                throw new IllegalArgumentException("We're didn't found any tasks with name like " + name);
            }
        } else {
            throw new NoSuchElementException("User with id " + userId + " doesn't exist");
        }
    }

    @Override
    public Set<TaskDTO> getUserTasks(int userId) {
        Optional<Set<Task>> resultSet = taskRepository.findTasksByUserId(userId);

        if (resultSet.isPresent()) {
            return resultSet.get().stream().sorted().map(task -> converter.taskToDto(task)).collect(Collectors.toSet());
        } else {
            throw new NoSuchElementException("User with id " + userId + " doesn't exist");
        }

    }


    @Override
    public TaskDTO addNewTask(Task task, int userId) {
        Optional<User> checkIfUserExist = userRepository.findById(userId);
        if (checkIfUserExist.isPresent()) {
            if (!taskRepository.findTaskByTaskNameIgnoreCaseAndUserId(task.getTaskName(), userId).isPresent()) {
                task.setUser(checkIfUserExist.get(), false);
                return converter.taskToDto(taskRepository.save(task));
            } else {
                throw new DataIntegrityViolationException("Name of your task " + task.getTaskName() + " is not unique");
            }
        } else {
            throw new NoSuchElementException("User with id " + userId + " doesn't exist");
        }
    }

    @Override
    public TaskDTO updateTask(Task sourceTask, int userId) {
        Optional<User> checkIfUserExist = userRepository.findById(userId);
        Optional<Task> checkIfTaskExist = taskRepository.findById(sourceTask.getId());
        if (checkIfUserExist.isPresent()) {
            if (checkIfTaskExist.isPresent()) {
                Optional<Task> taskByName = taskRepository.findTaskByTaskNameIgnoreCaseAndUserId(sourceTask.getTaskName(), userId);
                if (taskByName.isPresent() && taskByName.get().getId() != sourceTask.getId()) {
                    throw new DataIntegrityViolationException("Name of your task \"" + sourceTask.getTaskName() + "\" is not unique");
                }
                taskRepository.updateTask(sourceTask.getTask(), sourceTask.getTaskName(), sourceTask.getId(), userId);
            } else {
                throw new NoSuchElementException("Task with id " + sourceTask.getId() + " doesn't exist");
            }
            return converter.taskToDto(taskRepository.findById(sourceTask.getId()).get());
        } else {
            throw new NoSuchElementException("User with id " + userId + " doesn't exist");
        }
    }

    @Override
    public String delete(int taskId, int userId) throws IllegalAccessException {
        Optional<User> checkIfUserExist = userRepository.findById(userId);
        Optional<Task> checkIfTaskExist = taskRepository.findById(taskId);
        if (checkIfUserExist.isPresent() && checkIfTaskExist.isPresent()) {
            if (checkIfTaskExist.get().getUser() == checkIfUserExist.get().getId()) {
                taskRepository.deleteTaskByIdAndUserId(taskId, userId);
                return "Your task with id " + taskId + " was successfully deleted";
            } else {
                throw new IllegalAccessException();
            }
        } else {
            String errorMessage = checkIfTaskExist.isPresent() ? "User with id " + userId + " doesn't exist" : "Task with id " + taskId + " doesn't exist";
            throw new NoSuchElementException(errorMessage);
        }
    }


    @Override
    public TaskDTO getById(int taskId, int userId) {
        Optional<Task> checkIfTaskExist = taskRepository.findTaskByIdAndUserId(taskId, userId);
        if (checkIfTaskExist.isPresent()) {
            return converter.taskToDto(checkIfTaskExist.get());
        } else {
            String errorMessage = "Task with id " + taskId + " doesn't exist";
            throw new NoSuchElementException(errorMessage);
        }
    }
}
