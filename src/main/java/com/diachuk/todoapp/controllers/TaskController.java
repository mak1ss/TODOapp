package com.diachuk.todoapp.controllers;


import com.diachuk.todoapp.dto.TaskDTO;
import com.diachuk.todoapp.entities.Task;
import com.diachuk.todoapp.services.interfaces.TaskService;
import com.diachuk.todoapp.util.security.annotations.IndicateUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Set;

@Controller
@RequestMapping(path = "/tasks")
public class TaskController {
    private TaskService service;
    private int userId;

    @GetMapping("/")
    @IndicateUser
    public String getAll(Authentication auth, Model model){
        Set<TaskDTO> tasks;
        tasks = service.getUserTasks(userId);
        if(tasks.isEmpty()){
            model.addAttribute("emptyList", true);
        } else {
            model.addAttribute("tasks", tasks);
        }
        return "tasks_main";
    }

    @PostMapping ("/new")
    @IndicateUser
    public String addTask(@ModelAttribute(name = "taskName") String taskName, @ModelAttribute(name = "task")String taskContent, Authentication auth, Model model){
        Task task = new Task();
        task.setTaskName(taskName);
        task.setTask(taskContent);
        task.setCreationDate(new Date());
        try{
            service.addNewTask(task, userId);
        } catch (DataIntegrityViolationException ex){
            return "redirect:/tasks/create?not_unique=true";
        }
        return "redirect:/tasks/";
    }

    @GetMapping("/create")
    public String createTaskPage(@RequestParam(required = false, name = "not_unique") String notUnique, Model model){
        if(notUnique != null){
            model.addAttribute("not_unique", true);
        }
        return "create_task";
    }

    @GetMapping("/update/{taskId}")
    @IndicateUser
    public String updateTaskPage(Authentication auth , @PathVariable("taskId")int taskId, Model model,
                                 @RequestParam(required = false, name = "success") String success,
                                 @RequestParam(required = false, name="not_unique") String notUnique){
        if(success != null){
            model.addAttribute("success", true);
        }
        if(notUnique != null){
            model.addAttribute("not_unique", true);
        }
        TaskDTO currentTask;
        try {
            currentTask = service.getById(taskId, userId);
            model.addAttribute("task", currentTask);
        } catch (NoSuchElementException ex) {
            return "redirect:/user/access-denied";
        }
        return "task_update";
    }

    @PutMapping("/perform_update/{taskId}")
    @IndicateUser
    public String update(@PathVariable int taskId, @RequestParam String taskName, @RequestParam String taskContent, Authentication auth, Model model){
        Task task = new Task();
        task.setTask(taskContent);
        task.setTaskName(taskName);
        task.setId(taskId);
        try {
            service.updateTask(task, userId);
            return String.format("redirect:/tasks/update/%d?success=true", taskId);
        } catch(DataIntegrityViolationException ex){
            return String.format("redirect:/tasks/update/%d?not_unique=true", taskId);
        }
    }

    @DeleteMapping("/delete/{taskId}")
    @IndicateUser
    public String delete(@PathVariable int taskId, Authentication auth){
        try{
            service.delete(taskId, userId);
        } catch (NoSuchElementException | IllegalAccessException ex){
            return "redirect:/user/access-denied";
        }
        return "redirect:/tasks/";
    }

    @GetMapping("/search")
    @IndicateUser
    public ResponseEntity<Set<TaskDTO>> searchTasks(@RequestParam String taskName, HttpServletRequest request){
        return ResponseEntity.status(HttpStatus.OK).body(service.getTaskByName(taskName, userId));
    }

    /** Exceptions Handling **/

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<String> onMissingTaskWithName(IllegalArgumentException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ClassUtils.getShortName(IllegalArgumentException.class)
                + " : " + ex.getLocalizedMessage());
    }

    @ExceptionHandler({DataIntegrityViolationException.class})
    public ResponseEntity<String> onInvalidParams(DataIntegrityViolationException ex){
        return ResponseEntity.status(HttpStatus.valueOf(418)).body(ClassUtils.getShortName(DataIntegrityViolationException.class)
                + " : " + ex.getLocalizedMessage());
    }

    @ExceptionHandler({NoSuchElementException.class})
    public ResponseEntity<String> onMissingEntityWithId(NoSuchElementException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ClassUtils.getShortName(NoSuchElementException.class)
                + " : " + ex.getLocalizedMessage());
    }

    @ExceptionHandler({SQLException.class})
    public ResponseEntity<String> onSQLProblems(SQLException ex){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ClassUtils.getShortName(SQLException.class)
                + " : " + ex.getSQLState()
                + "; " + ex.getLocalizedMessage());
    }


    /** Bean Injection **/

    @Autowired
    public void setService(TaskService service) {
        this.service = service;
    }

}
