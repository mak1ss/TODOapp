package com.diachuk.todoapp.controllers;

import com.diachuk.todoapp.dto.UserDTO;
import com.diachuk.todoapp.entities.security.UserPrincipal;
import com.diachuk.todoapp.services.interfaces.UserService;
import com.diachuk.todoapp.util.Converter;
import com.diachuk.todoapp.util.security.annotations.IndicateUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Controller
@RequestMapping(path = "/user")
public class UserController {

    private UserService service;
    private Converter converter;
    private int userId;

    @GetMapping("/")
    public String homePage() {
        return "user_home";
    }

    @GetMapping("/search")
    public ModelAndView userSearch(@ModelAttribute("username") String username, ModelAndView modelAndView){
        List<UserDTO> users;
        try {
            users = service.getByName(username);
            if(users.isEmpty()) throw new NoSuchElementException();
            modelAndView.addObject("search_result", users);
        } catch(NoSuchElementException ex){
            modelAndView.addObject("not_found", username);
        }
        modelAndView.setViewName("forward:/user/");
        return modelAndView;
    }

    @GetMapping("/login")
    public ModelAndView signIn(ModelAndView mv, @RequestParam(required = false) String error, @RequestParam(required = false) String logout, Authentication auth) {
        if(auth != null){
            mv.setViewName("redirect:/user/");
            return mv;
        }
        if (error != null) {
            mv.addObject("error", true);
        }
        if (logout != null) {
            mv.addObject("logout", true);
        }
        mv.setViewName("login");
        return mv;
    }

    @GetMapping("/registration")
    public ModelAndView registration(ModelAndView modelAndView) {
        modelAndView.setViewName("registration");
        return modelAndView;
    }

    @PostMapping("/perform_registration")
    public ModelAndView createUser(@ModelAttribute(name = "username") String username, @ModelAttribute(name = "password") String password, ModelAndView mv) {
        UserDTO user = new UserDTO();
        user.setName(username);
        user.setPassword(password);
        mv.setViewName("index");
        try {
            service.createUser(converter.dtoToUser(user));
        } catch (DataIntegrityViolationException ex) {
            mv.addObject("not_unique_name", true);
            mv.setViewName("registration");
        }
        return mv;
    }


    @GetMapping("/profile")
    public String openProfile(Authentication authentication, Model model) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        model.addAttribute("currentUser", principal.getUser());
        return "user_profile";
    }

    @PutMapping("/update")
    @IndicateUser
    public ModelAndView updateUser(@RequestParam String username,
                                   @RequestParam(required = false) String password,
                                   @RequestParam(required = false) String userStatus, Authentication auth, ModelAndView modelAndView) {
        UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
        if (password == null || password.isEmpty()) {
            password = userPrincipal.getPassword();
            System.out.println(password);
        }

        UserDTO user = new UserDTO();
        user.setName(username);
        user.setPassword(password);
        user.setStatus(userStatus);
        user.setId(userId);

        modelAndView.setViewName("user_profile");
        try {
            user = service.updateUser(converter.dtoToUser(user));
        } catch (DataIntegrityViolationException ex) {
            modelAndView.addObject("error", true);
            return modelAndView;
        }

        userPrincipal.getUser().setName(user.getName());
        userPrincipal.getUser().setPassword(user.getPassword());
        userPrincipal.getUser().setStatus(user.getStatus());
        modelAndView.addObject("currentUser", user);
        modelAndView.addObject("success", true);
        return modelAndView;
    }

    @PutMapping("/businessUpdate/{userId}")
    public ResponseEntity<String> updateToBusinessAccount(@PathVariable int userId) {
        return new ResponseEntity<>(service.updateToBusinessUser(userId), HttpStatus.OK);
    }

    @GetMapping("/access-denied")
    public String accessDenied(){
        return "access_denied";
    }


    /**
     * Administration endpoints
     **/
    @PreAuthorize("hasAuthority('ROLE_ADMIN') and isFullyAuthenticated()")
    @DeleteMapping("/admin/delete")
    public ModelAndView deleteUser(@RequestParam int userId, ModelAndView model) {
        try{
            service.deleteUser(userId);
            model.addObject("success_delete", true);
        } catch(NoSuchElementException ex){
            model.addObject("error", userId);
        }
        return getAll(model);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') and isFullyAuthenticated()")
    @GetMapping("/admin")
    public ModelAndView getAll(ModelAndView modelAndView) {
        List<UserDTO> users = service.getAllUsers();
        modelAndView.addObject("allUsers", users);
        modelAndView.addObject("adminList", users
                .stream().filter(user -> user.getRole().stream()
                        .anyMatch(userRole -> userRole.equals("ROLE_ADMIN")))
                .collect(Collectors.toList()));
        modelAndView.setViewName("admin_page");
        return modelAndView;
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/admin/get")
    public ModelAndView getUserByName(@ModelAttribute(name="username") String username, ModelAndView modelAndView) {
        modelAndView.setViewName("forward:/user/admin");
        List<UserDTO> users ;
        try{
            users =  service.getByName(username);
            modelAndView.addObject("search_result", users);
        } catch (NoSuchElementException ex){
            modelAndView.addObject("not_found", username);
        }
        return modelAndView;
    }

    /**
     * Exceptions Handling
     **/


    @ExceptionHandler({NoSuchElementException.class})
    public ResponseEntity<String> onMissingUserWithId(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ClassUtils.getShortName(NoSuchElementException.class)
                + " : " + ex.getLocalizedMessage());
    }

    @ExceptionHandler({DataIntegrityViolationException.class})
    public ResponseEntity<String> onNotUniqueUserName(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.valueOf(418)).body(ClassUtils.getShortName(DataIntegrityViolationException.class)
                + " : " + ex.getLocalizedMessage());
    }

    @ExceptionHandler({EmptyResultDataAccessException.class})
    public ResponseEntity<String> onEmptyTable(EmptyResultDataAccessException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ClassUtils.getShortName(EmptyResultDataAccessException.class)
                + " : " + ex.getLocalizedMessage());
    }

    @ExceptionHandler({SQLException.class})
    public ResponseEntity<String> onSQLProblems(SQLException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ClassUtils.getShortName(SQLException.class)
                + " : something went wrong at " + ex.getSQLState()
                + "; " + ex.getLocalizedMessage());
    }

    /**
     * Bean Injection
     **/

    @Autowired
    public void setService(UserService service) {
        this.service = service;
    }

    @Autowired
    public void setConverter(Converter converter) {
        this.converter = converter;
    }

}
