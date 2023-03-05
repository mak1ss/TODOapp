package com.diachuk.todoapp.services;

import com.diachuk.todoapp.dto.UserDTO;
import com.diachuk.todoapp.entities.User;
import com.diachuk.todoapp.services.interfaces.UserService;
import com.diachuk.todoapp.services.repositories.RoleRepository;
import com.diachuk.todoapp.services.repositories.UserRepository;
import com.diachuk.todoapp.util.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PreFilter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final Converter converter;
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository repository, Converter converter, RoleRepository roleRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = repository;
        this.converter = converter;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }



    @Override
    public UserDTO getById(int id) {
        Optional<User> result = userRepository.findUserById(id);
        if (result.isPresent()) {
            return converter.userToDto(result.get());
        } else {
            throw new NoSuchElementException("User with id " + id + " doesn't exist");
        }
    }

    @Override
    public List<UserDTO> getAllUsers() {
        List<User> result = userRepository.findAll();
        if (result.size() == 0) {
            throw new EmptyResultDataAccessException("No one user was found, add at least ", 1);
        } return result.stream().map(converter::userToDto).collect(Collectors.toList());
    }

    @Override
    public UserDTO createUser(User user) {
        if(userRepository.findUserByName(user.getName()).isPresent()){
            throw new DataIntegrityViolationException("User with name " + user.getName() + " is already exist");
        }
        user.addRole(roleRepository.getRoleUser());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return converter.userToDto(userRepository.save(user));
    }

    @Override
    public String deleteUser(int id) {
        if (!userRepository.existsById(id)) {
            throw new NoSuchElementException("No user was found with id :" + id);
        }
        userRepository.deleteById(id);
        return "User with id " + id + " was successfully deleted";
    }

    @Override
    public UserDTO updateUser(User user) {
        if(!userRepository.existsById(user.getId())){
            throw new NoSuchElementException("No user was found with id :" + user.getId());
        }
        Optional<User> checkIfUserExist = userRepository.findUserByName(user.getName());
        if(checkIfUserExist.isPresent() && (checkIfUserExist.get().getId() != user.getId())){
            throw new DataIntegrityViolationException("User with name " + user.getName() + " is already exist");
        }
        try {
            passwordEncoder.upgradeEncoding(user.getPassword());
        } catch (Exception ex){
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        userRepository.updateUser(user.getName(), user.getStatus(), user.getPassword(), user.getId());
        return getById(user.getId());
    }

    @Override
    public String updateToBusinessUser(int id) {
        if (userRepository.existsById(id)) {
            if(!userRepository.checkIfBusiness(id).contains("BusinessUser")) {
                userRepository.updateToBusinessUser(id);
                return "User with id " + id + " was successfully updated";
            } else {
               throw new DataIntegrityViolationException("User with id " + id + " is already updated to Business account");
            }
        } else {
            throw new NoSuchElementException("User with id " + id + " doesn't exist");
        }
    }

    @Override
    public UserDTO getByNameAndPassword(String name, String password) {
        User foundUser = userRepository.findUserByNameAndPassword(name, password);
        if (foundUser == null) return null;
        return converter.userToDto(foundUser);
    }

    @PostFilter("!filterObject.role.contains('ROLE_ADMIN')")
    @Override
    public List<UserDTO> getByName(String name) {
        Optional<List<User>> users = userRepository.findUsersByNameContainingIgnoreCase(name);
        if(!users.isPresent() || users.get().isEmpty()) throw new NoSuchElementException();
        return users.get().stream().map(converter :: userToDto).collect(Collectors.toList());
    }

}
