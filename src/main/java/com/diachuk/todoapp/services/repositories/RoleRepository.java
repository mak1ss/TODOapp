package com.diachuk.todoapp.services.repositories;

import com.diachuk.todoapp.entities.User;
import com.diachuk.todoapp.entities.security.UserRole;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface RoleRepository extends CrudRepository<UserRole, Integer> {

    List<String> getUserRoles(int userId);
//    @Query("select new UserRole (r.role) from UserRole r join User.roles as ur on r.id = ur.id where User.id = ?1")
//    List<String> findUserRolesByUserId(int userId);

    @Query("SELECT r FROM UserRole r WHERE r.role = 'ROLE_USER'")
    UserRole getRoleUser();
}
