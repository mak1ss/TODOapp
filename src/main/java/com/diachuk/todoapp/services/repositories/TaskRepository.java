package com.diachuk.todoapp.services.repositories;

import com.diachuk.todoapp.entities.Task;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Repository
@Transactional
public interface TaskRepository extends CrudRepository<Task, Integer> {
    Optional<Set<Task>> findTaskByTaskNameContainingIgnoreCaseAndUserId(String taskName, int id);

    @Query("SELECT task FROM Task as task WHERE task.user.id = ?1")
    Optional<Set<Task>> findTasksByUserId(int id);

    Optional<Task> findTaskByTaskNameIgnoreCaseAndUserId(String taskName, int userId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    void updateTask(String task, String taskName, int taskId, int userId);


    @Query("DELETE FROM Task task WHERE task.id = ?1 AND task.user.id = ?2")
    @Modifying
    void deleteTaskByIdAndUserId(int taskId, int userId);


    Optional<Task> findTaskByIdAndUserId(int taskId, int userId);
}
