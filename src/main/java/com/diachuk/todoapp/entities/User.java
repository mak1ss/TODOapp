package com.diachuk.todoapp.entities;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.NamedNativeQuery;
import javax.persistence.Table;

import com.diachuk.todoapp.entities.security.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.*;

import java.util.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="_users")
@NamedNativeQuery(name = "User.updateUser", query = "UPDATE _users as u SET  u.name = ?1 , u.status = ?2, u.password = ?3 WHERE u.id = ?4")
@NamedNativeQuery(name = "User.updateToBusinessUser", query = "UPDATE _users as u SET u.dtype = 'BusinessUser' WHERE u.id = ?")
@NamedNativeQuery(name = "User.checkIfBusiness", query = "SELECT u.dtype FROM _users u WHERE u.id = ?")
@NamedNativeQuery(name = "User.changePassword", query = "UPDATE somedb._users u SET u.password = ?1 WHERE u.id = ?2")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column
    private String status;

    @Column(nullable = false)
    private String password;


    @LazyCollection(value = LazyCollectionOption.FALSE)
    @CollectionTable(name = "_users_roles",  joinColumns = @JoinColumn(name = "user_id"), uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "roles_id"}))
    @OneToMany
    private List<UserRole> roles = new ArrayList<>();

    @OneToMany(cascade= CascadeType.ALL, mappedBy="user", fetch = FetchType.EAGER)
    private List<Task> tasks = new ArrayList<>();


    public void addTask(Task task, boolean isUserSetted) {
        if(isUserSetted){
            tasks.add(task);
        } else {
            task.setUser(this, false);
        }

    }

    public void addRole(UserRole userRole){
        this.roles.add(userRole);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id && name.equals(user.name) && Objects.equals(status, user.status) && password.equals(user.password) && roles.equals(user.roles) && tasks.equals(user.tasks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, status, password, roles, tasks.size());
    }
}
