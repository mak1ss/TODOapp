package com.diachuk.todoapp.services.repositories;

import com.diachuk.todoapp.entities.User;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NamedNativeQuery;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface UserRepository extends CrudRepository<User, Integer> {

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    void updateUser(String name, String status, String password, int id );
    User findUserByNameAndPassword(String name, String password);
    Optional<User> findUserById(int id);
    @Modifying
    void updateToBusinessUser(int id);

    List<User> findAll();
    String checkIfBusiness(int userId);

    Optional<List<User>> findUsersByNameContainingIgnoreCase(String userName);

    Optional<User> findUserByName(String username);

    int changePassword(String newPassword, int userId);
}
