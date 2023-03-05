package com.diachuk.todoapp.services;

import com.diachuk.todoapp.dto.TaskDTO;
import com.diachuk.todoapp.entities.Task;
import com.diachuk.todoapp.entities.User;
import com.diachuk.todoapp.services.interfaces.TaskService;
import org.assertj.core.util.Lists;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.NoSuchElementException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringJUnit4ClassRunner.class)
@Deprecated
public class TaskServiceTest {
    private ApplicationContext context;
    private TaskService taskService;

    private int userId;

    private final String TASK_NAME = "test taskName";
    private final String TASK_CONTENT = "test task content";

    @Before
    public void init() {
        this.context = new ClassPathXmlApplicationContext("testConfig.xml");
        this.taskService = context.getBean(TaskServiceImpl.class);

        User user = new User();
        user.setName("name");
        user.setPassword("password");
        userId = context.getBean(UserServiceImpl.class).createUser(user).getId();
    }

    @After
    public void cleaningTasks() {
        context.getBean(UserServiceImpl.class).deleteUser(userId);

    }

    private TaskDTO addTaskToDb(int userId, String taskName, String taskContent){
        Task current = new Task();
        current.setTaskName(taskName);
        current.setTask(taskContent);

        return taskService.addNewTask(current, userId);
    }

    @Test
    public void createTaskTest() {
        Task current = new Task();
        current.setTaskName(TASK_NAME);
        current.setTask(TASK_CONTENT);

        TaskDTO actual = taskService.addNewTask(current, userId);


        assertTrue(current.getTaskName().equals(actual.getTaskName()) && current.getTask().equals(actual.getTask()));
        assertNotEquals(0, actual.getId());


        assertThrows(NoSuchElementException.class, () -> taskService.addNewTask(current, -1));
        assertThrows(DataIntegrityViolationException.class, () -> taskService.addNewTask(current, userId));
    }

    @Test
    public void tasksByNameTest(){
        TaskDTO current = addTaskToDb(userId, TASK_NAME, TASK_CONTENT);

        TaskDTO result = taskService.getTaskByName(current.getTaskName(), userId).stream().findFirst().get();
        assertEquals(current.getTaskName(), result.getTaskName());

        addTaskToDb(userId, "similar taskName", TASK_CONTENT);

        Set<TaskDTO> result2 = taskService.getTaskByName("taskName", userId);

        assertEquals(2, result2.size());
        assertThrows(IllegalArgumentException.class, () -> taskService.getTaskByName(String.valueOf(-1), userId));
        assertThrows(NoSuchElementException.class, () -> taskService.getTaskByName(TASK_NAME, -1 ));
    }


    @Test
    public void getUserTasksTest(){
        TaskDTO firstUserTask = addTaskToDb(userId, TASK_NAME, TASK_CONTENT);
        TaskDTO secondUserTask = addTaskToDb(userId, "name of second task", TASK_CONTENT);


        Set<TaskDTO> result = taskService.getUserTasks(userId);
        assertEquals(2, result.size());
        assertTrue(result.containsAll(Lists.list(firstUserTask, secondUserTask)));

    }

    @Test
    public void deleteTaskTest() throws IllegalAccessException {
        TaskDTO current = addTaskToDb(userId, TASK_NAME, TASK_CONTENT);


        assertThrows(NoSuchElementException.class, () -> taskService.delete(current.getId(), -1));

        String result = taskService.delete(current.getId(), userId);
        assertTrue(result.contains("successfully deleted"));
        assertEquals(0, taskService.getUserTasks(userId).size());

        assertThrows(NoSuchElementException.class, () -> taskService.delete(-1, userId));
        assertThrows(IllegalArgumentException.class, () -> taskService.getTaskByName(TASK_NAME, userId));

    }

    @Test
    public void updateTaskTest(){
        TaskDTO current = addTaskToDb(userId, TASK_NAME, TASK_CONTENT);

        Task updatingTask = new Task();
        updatingTask.setId(current.getId());
        updatingTask.setTaskName("updated task name");
        updatingTask.setTask("updated task content");

        assertThrows(NoSuchElementException.class, () -> taskService.updateTask(updatingTask, -1));

        TaskDTO updatedTask = taskService.updateTask(updatingTask, userId);

        assertNotEquals(updatedTask, current);
    }
}
