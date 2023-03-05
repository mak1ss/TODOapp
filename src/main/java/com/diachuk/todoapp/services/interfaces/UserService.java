package com.diachuk.todoapp.services.interfaces;

import com.diachuk.todoapp.dto.UserDTO;
import com.diachuk.todoapp.entities.User;

import java.util.List;

public interface UserService {

    UserDTO getById(int id);

    List<UserDTO> getAllUsers();

    UserDTO createUser(User user);

    String deleteUser(int id);

    UserDTO updateUser(User user);

    String updateToBusinessUser(int id);

    UserDTO getByNameAndPassword(String name, String password);

    List<UserDTO> getByName(String name);
}
