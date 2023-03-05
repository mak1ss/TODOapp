package com.diachuk.todoapp.services;

import com.diachuk.todoapp.dto.UserDTO;
import com.diachuk.todoapp.entities.User;
import com.diachuk.todoapp.services.interfaces.UserService;

import com.diachuk.todoapp.services.repositories.UserRepository;
import com.diachuk.todoapp.util.Converter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.NoSuchElementException;

@RunWith(SpringJUnit4ClassRunner.class)
@Deprecated
public class UserServiceTest {
    private UserService service;

    private UserRepository repository;
    private ApplicationContext context;
    private Converter converter;
    private final String NAME = "name";
    private final String PASSWORD = "password";

    @Before
    public void init() {
        this.context = new ClassPathXmlApplicationContext("testConfig.xml");
        this.service = context.getBean(UserService.class);
        this.converter = context.getBean(Converter.class);
        this.repository = context.getBean("userRepository", UserRepository.class);
    }

    @After
    public void cleaningDb() {
        try {
            service.getAllUsers().forEach(user -> service.deleteUser(user.getId()));
        } catch (EmptyResultDataAccessException e){
            System.out.println("Data base is already cleaned");
        }
    }

    public UserDTO addUserToDb(){
        User current = new User();
        current.setName(NAME);
        current.setPassword(PASSWORD);
        return service.createUser(current);
    }

    @Test
    public void createUserTest() {
        UserDTO actual = addUserToDb();
        assertNotNull(actual);
        assertTrue(NAME.equals(actual.getName()) && PASSWORD.equals(actual.getPassword()));
        assertNotEquals(0, actual.getId());

    }

    @Test
    public void getUserByIdTest() {
        UserDTO actual = addUserToDb();
        UserDTO result = service.getById(actual.getId());

        assertEquals(actual, result);
    }

    @Test(expected = NoSuchElementException.class)
    public void tryingToGetUserByWrongId_andThen_getNoSuchElementException() {
        addUserToDb();
        service.getById(-1);
    }


    @Test(expected = DataIntegrityViolationException.class)
    public void creatingUserWithNonUniqueName_andThen_getDataIntegrityViolationException() {
        addUserToDb();
        addUserToDb();
    }

    @Test
    public void updateUserTest() {
        UserDTO current = addUserToDb();

        User updatingUser = new User();

        updatingUser.setId(current.getId());
        updatingUser.setName("updated name");
        updatingUser.setPassword("updated password");

        UserDTO updatedUser = service.updateUser(updatingUser);

        assertEquals(converter.userToDto(updatingUser), updatedUser);
        assertEquals(updatedUser.getName(), updatingUser.getName());
        assertEquals(updatedUser.getPassword(), updatingUser.getPassword());
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void updatingUserWithNonUniqueName_andThen_getDataIntegrityViolationException() {
        UserDTO current = addUserToDb();

        User updatingUser = new User();
        updatingUser.setId(current.getId());
        updatingUser.setName(current.getName());
        updatingUser.setPassword(current.getPassword());

        service.updateUser(updatingUser);
    }

    @Test(expected = NoSuchElementException.class)
    public void updatingUserWithWrongId_andThen_getNoSuchElementException() {
        addUserToDb();

        User updatingUser = new User();

        updatingUser.setId(-1);
        updatingUser.setName("changed test name");
        updatingUser.setPassword("changed test password");
        service.updateUser(updatingUser);
    }

    @Test
    public void deleteUserTest() {
        int createdUserId = addUserToDb().getId();
        service.deleteUser(createdUserId);

        assertThrows(EmptyResultDataAccessException.class, () -> service.getAllUsers());
        assertThrows(NoSuchElementException.class, () -> service.deleteUser(createdUserId));
    }


    @Test
    public void updateToBusinessUserTest(){
        int createdUserId = addUserToDb().getId();

        assertThrows(NoSuchElementException.class, () -> service.updateToBusinessUser(-1));
        service.updateToBusinessUser(createdUserId);
        assertEquals("BusinessUser", repository.checkIfBusiness(createdUserId));
        assertThrows(DataIntegrityViolationException.class, () -> service.updateToBusinessUser(createdUserId));

    }


    @Test
    public void findUserByNameAndPasswordTest(){
        UserDTO createdUser = addUserToDb();
        UserDTO foundUser = service.getByNameAndPassword(createdUser.getName(), createdUser.getPassword());
        assertNotNull(foundUser);
        assertEquals(createdUser, foundUser);
        assertNull(service.getByNameAndPassword("wrong name", "wrong password"));

    }
}
