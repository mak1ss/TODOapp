package com.diachuk.todoapp.util.security;


import antlr.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class TokenManager {
    public boolean verifyToken(String sourceToken){
        UserToken sourcePayload = extractTokenPayload(sourceToken);
        if(sourcePayload == null) return false;
        String tokenFromSourcePayload = createToken(sourcePayload);
        return tokenFromSourcePayload.equals(sourceToken);
    }

    public String createToken(UserToken payload){
        String id = String.valueOf(payload.getId());
        String name = payload.getName();
        String timeOfCreation = String.valueOf(payload.getTimeOfCreation().getTime());
        String signature = String.join("@", id, name, timeOfCreation);
        return String.join("@", signature, createKey(signature,timeOfCreation));
    }

    private String createKey(String signature, String timeOfCreation){
        String cleanSignature = String.join("", signature.split("@"));
          return (cleanSignature.charAt(Character.getNumericValue(timeOfCreation.charAt(4))) + ""
                + cleanSignature.charAt(Character.getNumericValue(timeOfCreation.charAt(7)))
                + cleanSignature.charAt(Character.getNumericValue(timeOfCreation.charAt(5)))
                + cleanSignature.charAt(Character.getNumericValue(timeOfCreation.charAt(timeOfCreation.length()-1)))
                + cleanSignature.charAt(Character.getNumericValue(timeOfCreation.charAt(0))));
    }

    public UserToken extractTokenPayload(String token){
        String[] userTokenParts = token.split("@");
        int userId;
        try {
            userId = Integer.parseInt(userTokenParts[0]);
        } catch(NumberFormatException ex){
            return null;
        }
        String name = userTokenParts[1];
        Date date = new Date(Long.parseLong(userTokenParts[2]));
        return new UserToken(userId, name, date);
    }
}
