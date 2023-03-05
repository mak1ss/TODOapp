package com.diachuk.todoapp.util.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.function.Function;

@Component
public class UserAuthentication {
    @Autowired
    private TokenManager tokenManager;

    public <R> ResponseEntity<R> authenticateAndPerform(HttpServletRequest userRequest, Function<Integer, ResponseEntity<R>> function){
        String token = userRequest.getHeader("token");
        if(token != null) {
            if (!tokenManager.verifyToken(token)) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            UserToken payload = tokenManager.extractTokenPayload(token);
            return function.apply(payload.getId());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
}
