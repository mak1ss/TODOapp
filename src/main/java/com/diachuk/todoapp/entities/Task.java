package com.diachuk.todoapp.entities;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import java.io.File;
import java.util.Comparator;
import java.util.Date;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="todo_list")
@NamedQuery(name = "Task.updateTask", query = "UPDATE Task as task SET task.task = ?1, task.taskName = ?2 WHERE task.id = ?3 AND task.user.id = ?4")
public class Task implements Comparable<Task> {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private int id;

    @Column(name="name", unique = true, nullable = false)
    private String taskName;


    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Type(type="date")
    @Column(name="expiration_date")
    private Date expirationDate;

    @Column(name="task", nullable = false)
    private String task;

//    @Column(name = "image")
//    private File image;
    @ManyToOne
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", taskName='" + taskName + '\'' +
                ", task='" + task + '\'' +
                ", user=" + user +
                '}';
    }

    public void setUser(User user, boolean isTaskSet){
        if(!isTaskSet){
            this.user = user;
            user.addTask(this, true);
        } else {
            this.user = user;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task1 = (Task) o;
        return id == task1.id /*&& taskName.equals(task1.taskName) && task.equals(task1.task) && user.equals(task1.user)*/;
    }

    public int getUser(){
        return user.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, taskName, task);
    }

    @Override
    public int compareTo(Task o) {
        return this.id - o.getId();
    }
}
