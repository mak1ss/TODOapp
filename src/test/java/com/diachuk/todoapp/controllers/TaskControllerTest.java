package com.diachuk.todoapp.controllers;

import com.diachuk.todoapp.dto.TaskDTO;
import com.diachuk.todoapp.dto.UserDTO;
import com.diachuk.todoapp.entities.Task;
import com.diachuk.todoapp.entities.User;
import com.diachuk.todoapp.services.interfaces.TaskService;
import com.diachuk.todoapp.services.interfaces.UserService;
import com.diachuk.todoapp.util.security.TokenManager;
import com.diachuk.todoapp.util.security.UserToken;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@RunWith(SpringRunner.class)
@WebAppConfiguration
@ContextConfiguration("classpath:testConfig.xml")
@Deprecated
public class TaskControllerTest {
    @Autowired
    private WebApplicationContext context;
    @Autowired
    private UserService userService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private TokenManager tokenManager;
    private String token;
    private MockMvc mockMvc;

    private TaskDTO addTaskToDb(String taskName) {
        Task task = new Task();
        task.setTaskName(taskName);
        task.setTask("test taskContent");
        int userId = Character.getNumericValue(token.charAt(0));
        return this.taskService.addNewTask(task, userId);
    }

    @Before
    public void init() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        User user = new User();
        user.setName("userName");
        user.setPassword("userPassword");
        UserDTO createdUser = this.userService.createUser(user);

        this.token = tokenManager.createToken(new UserToken(createdUser.getId(), createdUser.getName(), new Date()));
    }

    @After
    public void cleanDb() {
        try {
            this.userService.getAllUsers().forEach(user -> userService.deleteUser(user.getId()));
        } catch (EmptyResultDataAccessException ex) {
            System.out.println("Data base is already empty");
        }
    }

    @Test
    public void addTaskTest() throws Exception {
        String taskJsonBody = "{\"taskName\" : \"test taskName\"," +
                "\"task\" : \"test taskContent\"}";
        int userId = Character.getNumericValue(token.charAt(0));

        this.mockMvc.perform(post("/tasks/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .header("token", token)
                        .content(taskJsonBody))
                .andDo(print())
                .andExpect(status().is(HttpStatus.CREATED.value()))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.taskName").value("test taskName"))
                .andExpect(jsonPath("$.task").value("test taskContent"))
                .andExpect(jsonPath("$.user").value(userId));

        /*Trying to enter invalid token in headers*/
        this.mockMvc.perform(post("/tasks/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .header("token", "invalid token")
                        .content(taskJsonBody))
                .andDo(print())
                .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    public void addTaskWithNotUniqueNameTest() throws Exception {
        TaskDTO createdTask = addTaskToDb("test taskName");
        String taskJsonBody = "{\"taskName\" : \"" + createdTask.getTaskName() + "\"," +
                "\"task\" : \"" + createdTask.getTask() + "\"}";

        String exceptionMessage = this.mockMvc.perform(post("/tasks/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .header("token", token)
                        .content(taskJsonBody))
                .andDo(print())
                .andExpect(status().is(418))
                .andReturn().getResolvedException().getMessage();

        assertTrue(exceptionMessage.contains("Name of your task " + createdTask.getTaskName() + " is not unique"));
    }

    @Test
    public void getAllTasksTest() throws Exception {
        TaskDTO firstCreatedTask = addTaskToDb("first test taskName");

        this.mockMvc.perform(get("/tasks/all")
                        .header("token", token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id").value(firstCreatedTask.getId()))
                .andExpect(jsonPath("$.[0].taskName").value(firstCreatedTask.getTaskName()))
                .andExpect(jsonPath("$.[0].task").value(firstCreatedTask.getTask()))
                .andExpect(jsonPath("$.[0].user").value(firstCreatedTask.getUser()));

        addTaskToDb("second test taskName");
        this.mockMvc.perform(get("/tasks/all")
                        .header("token", token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(2)));

        /*Trying to enter invalid token in headers*/
        this.mockMvc.perform(get("/tasks/all")
                        .header("token", "invalid token"))
                .andDo(print())
                .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    public void updateTaskTest() throws Exception {
        TaskDTO createdTask = addTaskToDb("test taskName");
        String updatingTaskJsonBody = "{\"id\" : " + createdTask.getId() + ", " +
                "\"taskName\" : \"updated taskName\"," +
                "\"task\" : \"updated taskContent\"}";

        this.mockMvc.perform(put("/tasks/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .header("token", token)
                        .content(updatingTaskJsonBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdTask.getId()))
                .andExpect(jsonPath("$.taskName").value("updated taskName"))
                .andExpect(jsonPath("$.task").value("updated taskContent"));

        /*Trying to enter invalid token in headers*/
        this.mockMvc.perform(put("/tasks/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .header("token", "invalid token")
                        .content(updatingTaskJsonBody))
                .andDo(print())
                .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    public void updateTaskWithWrongParamsTest() throws Exception {
        String updatingTaskJsonBody = "{\"id\" : 1, " +
                "\"taskName\" : \"updated taskName\"," +
                "\"task\" : \"updated taskContent\"}";

        /*Trying to update task with wrong id*/
        String exceptionMessage = this.mockMvc.perform(put("/tasks/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .header("token", token)
                        .content(updatingTaskJsonBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResolvedException().getMessage();

        assertTrue(exceptionMessage.contains("Task with id 1 doesn't exist"));

        TaskDTO firstTask = addTaskToDb("test taskName");
        TaskDTO secondTask = addTaskToDb("second test taskName");

        String updatingTaskJsonBody1 = "{\"id\" : " + secondTask.getId() + "," +
                "\"taskName\" : \"" + firstTask.getTaskName() + "\"," +
                "\"task\" : \"updated taskContent\"}";

        /*Trying to update task with not unique name*/
        exceptionMessage = this.mockMvc.perform(put("/tasks/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .header("token", token)
                        .content(updatingTaskJsonBody1))
                .andDo(print())
                .andExpect(status().is(HttpStatus.I_AM_A_TEAPOT.value()))
                .andReturn().getResolvedException().getLocalizedMessage();

        assertTrue(exceptionMessage.contains("Name of your task \"" + firstTask.getTaskName() + "\" is not unique"));
    }

    @Test
    public void deleteTaskTest() throws Exception {
        TaskDTO createdTask = addTaskToDb("test taskName");

        this.mockMvc.perform(delete("/tasks/delete/{taskId}", createdTask.getId())
                        .header("token", token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("Your task with id " + createdTask.getId() + " was successfully deleted"));

        /*Trying to delete already deleted task*/
        String exceptionMessage = this.mockMvc.perform(delete("/tasks/delete/{taskId}", createdTask.getId())
                        .header("token", token))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResolvedException().getLocalizedMessage();

        assertTrue(exceptionMessage.contains("Task with id " + createdTask.getId() + " doesn't exist"));

        /*Trying to enter invalid token in headers*/
        this.mockMvc.perform(delete("/tasks/delete/{taskId}", createdTask.getId())
                        .header("token", "invalid token"))
                .andDo(print())
                .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    public void searchTasksTest() throws Exception {
        TaskDTO firstTask = addTaskToDb("test taskName");


        this.mockMvc.perform(get("/tasks/search")
                .header("token", token)
                .param("taskName", "taskName"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id").value(firstTask.getId()))
                .andExpect(jsonPath("$.[0].taskName").value(firstTask.getTaskName()))
                .andExpect(jsonPath("$.[0].task").value(firstTask.getTask()))
                .andExpect(jsonPath("$.[0].user").value(firstTask.getUser()));

        /*Creating another task with similar name*/
        addTaskToDb("similar taskName");

        this.mockMvc.perform(get("/tasks/search")
                        .header("token", token)
                        .param("taskName", "taskName"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(2)));

        /*Trying to enter invalid token in headers*/
        this.mockMvc.perform(get("/tasks/search")
                        .header("token", "invalid token")
                        .param("taskName", "taskName"))
                .andDo(print())
                .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));

        /*Trying to enter task name that doesn't exist*/
        String exceptionMessage = this.mockMvc.perform(get("/tasks/search")
                        .header("token", token)
                        .param("taskName", "12345"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResolvedException().getLocalizedMessage();

        assertTrue(exceptionMessage.contains("We're didn't found any tasks with name like 12345"));
    }
}
