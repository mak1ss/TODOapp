package com.diachuk.todoapp.aspects;

import com.diachuk.todoapp.entities.security.UserPrincipal;
import com.diachuk.todoapp.util.security.UserAuthentication;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;

@Aspect
@Component
public class SecurityAspect {

//    @Autowired
//    private UserAuthentication authentication;

    @Pointcut("execution(@com.diachuk.todoapp.util.security.annotations.IndicateUser * com.diachuk.todoapp.controllers.*.* (..))")
    public void authenticationPointcut(){
    }

    @Around(value = "authenticationPointcut()")
    public Object authenticationAdvice(ProceedingJoinPoint jp){
        Authentication auth = extractAuthentication(jp);
        if (auth == null) throw new RuntimeException("Method annotated with @DetermineUser must have Authentication in it's args");
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        try {
            Field userIdField = jp.getTarget().getClass().getDeclaredField("userId");
            userIdField.setAccessible(true);
            userIdField.setInt(jp.getTarget(), principal.getUser().getId());

            Object result = jp.proceed();

            userIdField.setInt(jp.getTarget(), 0);
            userIdField.setAccessible(false);
            return result;
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Class with method annotated by @DetermineUser must have an userId field");
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }


    private Authentication extractAuthentication(ProceedingJoinPoint jp){
        Optional<Object> auth = Arrays.stream(jp.getArgs()).filter(arg -> Authentication.class.isAssignableFrom(arg.getClass())).findFirst();
        return (Authentication) auth.orElse(null);
    }
}
