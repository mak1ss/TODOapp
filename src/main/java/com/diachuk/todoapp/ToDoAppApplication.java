package com.diachuk.todoapp;

import com.diachuk.todoapp.entities.BusinessUser;
import com.diachuk.todoapp.entities.User;
import com.diachuk.todoapp.services.repositories.UserRepository;
import com.diachuk.todoapp.util.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.util.ArrayList;
import java.util.HashSet;

@SpringBootApplication
@EnableAspectJAutoProxy
public class ToDoAppApplication {

   /* @Autowired
    static UserRepository repository;*/

    public static void main(String[] args) {
        SpringApplication.run(ToDoAppApplication.class, args);


        /*repository.save(new User())
        Converter converter = new Converter();
        BusinessUser bu = new BusinessUser();
        bu.setId(1);
        bu.setName("adha");
        bu.setStatus("status");
        bu.setPassword("pass");
        bu.setTasks(new ArrayList<>());
        bu.setFriends(new HashSet<>());
        System.out.println(converter.userToDto(bu));*/
    }

}
