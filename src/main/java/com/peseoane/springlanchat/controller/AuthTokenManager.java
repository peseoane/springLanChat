package com.peseoane.springlanchat.controller;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.SessionAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import com.peseoane.springlanchat.model.*;
@Component
public class AuthTokenManager {

    private final Map<String, User> tokens = new HashMap<>();

    public String generateAuthToken(User user) {
        String authToken = UUID.randomUUID().toString();
        tokens.put(authToken, user);
        return authToken;
    }

    public boolean isValidAuthToken(String authToken) {
        return tokens.containsKey(authToken);
    }

    public User getUserByAuthToken(String authToken) {
        return tokens.get(authToken);
    }

    public void invalidateAuthToken(String authToken) {
        tokens.remove(authToken);
    }
}
