package com.digital5.service;

import org.springframework.stereotype.Service;

@Service
public class JWTService {

    public boolean verifyJWT(String token) {
        String[] splitToken = token.split("\\.");
        return false;
    }

}
