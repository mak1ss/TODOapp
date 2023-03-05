package com.diachuk.todoapp.controllers;

import com.diachuk.todoapp.dto.UserDTO;
import com.diachuk.todoapp.entities.User;
import com.diachuk.todoapp.services.interfaces.UserService;
import com.diachuk.todoapp.util.security.TokenManager;
import com.diachuk.todoapp.util.security.UserToken;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:testConfig.xml")
@WebAppConfiguration
@Deprecated
public class UserControllerTest {
    private final String USER_NAME = "test user name";
    private final String USER_PASSWORD = "test user password";
    private final String JSON_USER_BODY = "{ \"name\" : \"test user name\", " +
            "  \"password\" : \"test user password\"}";
    @Autowired
    private WebApplicationContext context;
    @Autowired
    private TokenManager manager;
    @Autowired
    private UserService service;
    private MockMvc mockMvc;

    private UserDTO addUserToDb(String name, String password) {
        User user = new User();
        user.setName(name);
        user.setPassword(password);
        return service.createUser(user);
    }

    private String createUserAndGetToken() {
        UserDTO result = addUserToDb(USER_NAME, USER_PASSWORD);
        return manager.createToken(new UserToken(result.getId(), result.getName(), new Date()));
    }

    @Before
    public void init() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
    }

    @After
    public void cleaningDb() {
        try {
            service.getAllUsers().forEach(user -> service.deleteUser(user.getId()));
        } catch (EmptyResultDataAccessException e) {
            System.out.println("Data base is already cleaned");
        }
    }

    @Test
    public void applicationContextTest() {
        ServletContext servletContext = context.getServletContext();

        assertNotNull(servletContext);
        assertTrue(servletContext instanceof MockServletContext);
        assertNotNull(context.getBean("userController"));
    }

    @Test
    public void createUserTest() throws Exception {
        this.mockMvc.perform(post("/user/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(JSON_USER_BODY))
                .andDo(print())
                .andExpect(status().is(HttpStatus.CREATED.value()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("name").value("test user name"))
                .andExpect(jsonPath("id").isNotEmpty());

        String exceptionMessage = this.mockMvc.perform(post("/user/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(JSON_USER_BODY))
                .andDo(print())
                .andExpect(status().is(418))
                .andReturn().getResolvedException().getMessage();

        assertEquals("User with name test user name is already exist", exceptionMessage);
    }

    @Test
    public void authorizationTest() throws Exception {
        addUserToDb(USER_NAME, USER_PASSWORD);

        String token = this.mockMvc.perform(post("/user/authentication")
                        .queryParam("name", "test user name")
                        .queryParam("password", "test user password")
                        .characterEncoding("UTF-8"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        System.out.println(token);
        assertTrue(manager.verifyToken(token));

        this.mockMvc.perform(post("/user/authentication")
                        .queryParam("name", "invalidName")
                        .queryParam("password", "invalidPassword")
                        .characterEncoding("UTF-8"))
                .andDo(print())
                .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    public void getAllUsersTest() throws Exception {
        String exceptionMessage = this.mockMvc.perform(get("/user/all"))
                .andDo(print())
                .andExpect(status().is(HttpStatus.CONFLICT.value()))
                .andReturn().getResolvedException().getMessage();
        assertEquals("No one user was found, add at least ", exceptionMessage);

        addUserToDb(USER_NAME, USER_PASSWORD);

        this.mockMvc.perform(get("/user/all"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.*", hasSize(1)));
    }

    @Test
    public void getUserByIdTest() throws Exception {
        UserDTO createdUser = addUserToDb(USER_NAME, USER_PASSWORD);

        this.mockMvc.perform(get("/user/{userId}", createdUser.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(createdUser.getId())))
                .andExpect(jsonPath("$.name", is(createdUser.getName())));


        String exceptionMessage = this.mockMvc.perform(get("/user/{userId}", 2))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResolvedException().getMessage();


        assertEquals("User with id 2 doesn't exist", exceptionMessage);
    }

    @Test
    public void updateUserTest() throws Exception {
        String token = createUserAndGetToken();
        String updatedUserJsonBody = "{\"name\" : \"updated userName\"," +
                "\"password\" : \"updatedPassword\"}";
        int createdUserId = Integer.parseInt(token.split("@")[0]);
        this.mockMvc.perform(put("/user/update")
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedUserJsonBody)
                        .header("token", token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(createdUserId))
                .andExpect(jsonPath("$.name").value("updated userName"));

        this.mockMvc.perform(put("/user/update")
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedUserJsonBody)
                        .header("token", "invalidToken"))
                .andDo(print())
                .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    public void updateUserWithNonUniqueNameTest() throws Exception {
        String token = createUserAndGetToken();
        UserDTO secondUser = addUserToDb("name of second user", USER_PASSWORD);
        String notUniqueUserNameJsonBody = "{\"name\" : \"" + secondUser.getName() + "\" ," +
                "\"password\" : \"updatedPassword\"}";
        String exceptionMessage = this.mockMvc.perform(put("/user/update")
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(notUniqueUserNameJsonBody)
                        .header("token", token))
                .andDo(print())
                .andExpect(status().is(418))
                .andReturn().getResolvedException().getMessage();

        assertTrue(exceptionMessage.contains("User with name " + secondUser.getName() + " is already exist"));
    }

    /*@Test
    public void updateUserWithWrongId() throws Exception {
        String token = createUserAndGetToken();
        String wrongUserIdJsonBody = "{\"id\" : 10," +
                "\"name\" : \"updated name\"," +
                "\"password\" : \"updated password\"}";
        String exceptionMessage1 = this.mockMvc.perform(put("/user/update")
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(wrongUserIdJsonBody)
                        .header("token", token))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResolvedException().getLocalizedMessage();

        assertEquals("No user was found with id :-1", exceptionMessage1);
    }*/

    @Test
    public void businessUpdateTest() throws Exception {
        UserDTO createdUser = addUserToDb(USER_NAME, USER_PASSWORD);
        String response = this.mockMvc.perform(put("/user/businessUpdate/{userId}", createdUser.getId())
                        .characterEncoding("UTF-8"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertEquals(response, "User with id " + createdUser.getId() + " was successfully updated");
    }

    @Test
    public void businessUpdateWithWrongIdTest() throws Exception {
        String exceptionMessage = this.mockMvc.perform(put("/user/businessUpdate/{userId}", 1))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResolvedException().getMessage();

        assertEquals(exceptionMessage, "User with id 1 doesn't exist");
    }

    @Test
    public void businessUpdateToAlreadyUpdatedUserTest() throws Exception {
        UserDTO createdUser = addUserToDb(USER_NAME, USER_PASSWORD);
        service.updateToBusinessUser(createdUser.getId());

        String exceptionMessage = this.mockMvc.perform(put("/user/businessUpdate/{userId}", createdUser.getId()))
                .andDo(print())
                .andExpect(status().is(418))
                .andReturn().getResolvedException().getMessage();

        assertEquals(exceptionMessage, "User with id " + createdUser.getId() + " is already updated to Business account");
    }

    @Test
    public void deleteUserTest() throws Exception {
        String token = createUserAndGetToken();
        int userId = Integer.parseInt(token.split("@")[0]);

        String response = this.mockMvc.perform(delete("/user/delete")
                        .header("token", token))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertEquals(response, "User with id " + userId + " was successfully deleted");

        /*Trying to delete already deleted user*/
        String secondResponse = this.mockMvc.perform(delete("/user/delete")
                        .header("token", token))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        assertTrue(secondResponse.contains("No user was found with id :" + userId));

        /*Trying to enter invalid token in headers*/
        this.mockMvc.perform(delete("/user/delete")
                        .header("token", "invalid token"))
                .andDo(print())
                .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
    }

}
